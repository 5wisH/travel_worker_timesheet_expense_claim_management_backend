package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao.ClaimDetailDao;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao.ClaimDao;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao.ClaimImageDao;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao.PaymentDao;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao.UserDao;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.Claim;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.ClaimCalculationResponse;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.ClaimDetail;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.ClaimImage;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.ClaimImageResponse;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.Payment;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service
public class ClaimService {

    @Autowired
    private ClaimDao claimDao;

    @Autowired
    private ClaimImageDao claimImageDao;

    @Autowired
    private ClaimDetailDao claimDetailDao;

    @Autowired
    private PaymentDao paymentDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public Claim saveClaim(Claim claim, MultipartFile[] files, String claimDetailsJson) {
        if (claim.getStatus() == null || claim.getStatus().isBlank() || "true".equalsIgnoreCase(claim.getStatus())) {
            claim.setStatus("Submitted");
        }

        if (claim.getClaimReference() == null || claim.getClaimReference().isBlank()) {
            claim.setClaimReference(generateClaimReference(claim));
        }

        if (files != null) {
            List<String> fileNames = java.util.Arrays.stream(files)
                    .map(MultipartFile::getOriginalFilename)
                    .filter(fileName -> fileName != null && !fileName.isBlank())
                    .toList();
            claim.setClaimImages(fileNames);
        }

        Claim savedClaim = claimDao.save(claim);
        saveClaimImages(savedClaim.getClaimId(), files);
        saveClaimDetails(savedClaim.getClaimId(), claimDetailsJson, files);
        hydrateClaim(savedClaim);
        return savedClaim;
    }

    public List<Claim> getClaims() {
        return claimDao.findAll()
                .stream()
                .peek(this::hydrateClaim)
                .toList();
    }

    public List<Claim> getClaimsByUserId(Long userId) {
        return claimDao.findByUserIdOrderByClaimDateDesc(userId)
                .stream()
                .peek(this::hydrateClaim)
                .peek(claim -> claim.setUserSubmissionCount(countClaimsByUserId(userId)))
                .toList();
    }

    public Claim getClaimById(Long claimId) {
        Claim claim = claimDao.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found with id: " + claimId));
        hydrateClaim(claim);
        return claim;
    }

    public Claim saveClaimRecord(Claim claim) {
        Claim savedClaim = claimDao.save(claim);
        hydrateClaim(savedClaim);
        return savedClaim;
    }

    public void deleteClaim(Long claimId) {
        claimDao.deleteById(claimId);
    }

    public long countClaimsByUserId(Long userId) {
        return claimDao.countByUserId(userId);
    }

    public Claim updateClaimStatus(Long claimId, String status) {
        Claim claim = claimDao.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found with id: " + claimId));
        claim.setStatus(status);
        Claim savedClaim = claimDao.save(claim);
        hydrateClaim(savedClaim);
        return savedClaim;
    }

    public List<ClaimImageResponse> getClaimImages(Long claimId) {
        return claimImageDao.findByClaimId(claimId)
                .stream()
                .map(claimImage -> new ClaimImageResponse(claimId, claimImage))
                .toList();
    }

    public ClaimImage getClaimImage(Long claimId, Long imageId) {
        return claimImageDao.findByClaimIdAndImageId(claimId, imageId)
                .orElseThrow(() -> new RuntimeException("Claim image not found with id: " + imageId));
    }

    public List<ClaimDetail> getClaimDetails(Long claimId) {
        return claimDetailDao.findByClaimId(claimId)
                .stream()
                .peek(this::populateReceiptUrl)
                .toList();
    }

    public ClaimCalculationResponse calculateClaimTotal(Long claimId) {
        Claim claim = claimDao.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found with id: " + claimId));
        List<ClaimDetail> details = getClaimDetails(claimId);
        double totalAmount = details.stream()
                .map(this::getIncludedAmount)
                .mapToDouble(Double::doubleValue)
                .sum();
        claim.setTotal_amount(totalAmount);
        claimDao.save(claim);
        return new ClaimCalculationResponse(claimId, claim.getClaimReference(), claim.getClaimDate(), totalAmount, details);
    }

    public Payment payClaim(Long claimId, String processedBy) {
        Claim claim = claimDao.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found with id: " + claimId));
        if (claim.getTotal_amount() == null || claim.getTotal_amount() <= 0) {
            calculateClaimTotal(claimId);
            claim = claimDao.findById(claimId)
                    .orElseThrow(() -> new RuntimeException("Claim not found with id: " + claimId));
        }

        Payment payment = new Payment();
        payment.setClaimId(claimId);
        payment.setPaymentDate(new Date());
        payment.setReference("PAY-" + (claim.getClaimReference() == null ? claimId : claim.getClaimReference()));
        payment.setStatus(true);
        payment.setProcessedBy(processedBy == null || processedBy.isBlank() ? "Manager" : processedBy);

        claim.setStatus("Paid");
        claimDao.save(claim);
        return paymentDao.save(payment);
    }

    public ClaimDetail getClaimDetailReceipt(Long claimId, Long detailId) {
        return claimDetailDao.findByClaimIdAndClaimDetailId(claimId, detailId)
                .orElseThrow(() -> new RuntimeException("Claim detail receipt not found with id: " + detailId));
    }

    private void saveClaimImages(Long claimId, MultipartFile[] files) {
        if (files == null) {
            return;
        }

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            ClaimImage claimImage = new ClaimImage();
            claimImage.setClaimId(claimId);
            claimImage.setFileName(file.getOriginalFilename());
            claimImage.setContentType(file.getContentType());
            try {
                claimImage.setData(file.getBytes());
            } catch (IOException e) {
                throw new RuntimeException("Could not read uploaded claim image", e);
            }
            claimImageDao.save(claimImage);
        }
    }

    private void saveClaimDetails(Long claimId, String claimDetailsJson, MultipartFile[] files) {
        if (claimDetailsJson == null || claimDetailsJson.isBlank()) {
            return;
        }

        try {
            List<ClaimDetail> claimDetails = readClaimDetails(claimDetailsJson);
            for (ClaimDetail detail : claimDetails) {
                detail.setClaimId(claimId);
                attachMatchingReceipt(detail, files);
                normalizeClaimDetail(detail);
                claimDetailDao.save(detail);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read claim details", e);
        }
    }

    private List<ClaimDetail> readClaimDetails(String claimDetailsJson) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(claimDetailsJson);
        if (jsonNode == null || jsonNode.isNull()) {
            return List.of();
        }

        if (jsonNode.isArray()) {
            return objectMapper.convertValue(jsonNode, new TypeReference<List<ClaimDetail>>() {});
        }

        return List.of(objectMapper.convertValue(jsonNode, ClaimDetail.class));
    }

    private void attachMatchingReceipt(ClaimDetail detail, MultipartFile[] files) throws IOException {
        if (files == null || detail.getReceiptFileName() == null || detail.getReceiptFileName().isBlank()) {
            return;
        }

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            if (detail.getReceiptFileName().equals(file.getOriginalFilename())) {
                detail.setReceiptContentType(file.getContentType());
                detail.setReceiptData(file.getBytes());
                return;
            }
        }
    }

    private void normalizeClaimDetail(ClaimDetail detail) {
        if ("Meals".equalsIgnoreCase(detail.getCategory())) {
            normalizeMealDetail(detail);
            return;
        }

        if ("Distance Travelled".equalsIgnoreCase(detail.getCategory())) {
            detail.setDetailType("Distance");
            double kilometers = detail.getKilometers() == null ? 0 : detail.getKilometers();
            double rate = getDistanceRatePerKm(detail);
            detail.setAllowedAmount(null);
            if (rate > 0) {
                detail.setAmount(kilometers * rate);
            } else if (detail.getAmount() == null) {
                detail.setAmount(0.0);
            }
            detail.setReimbursableAmount(detail.getAmount());
            return;
        }

        if ("Toll Fees".equalsIgnoreCase(detail.getCategory()) || "Other".equalsIgnoreCase(detail.getCategory())) {
            detail.setAllowedAmount(null);
            detail.setReimbursableAmount(detail.getAmount() == null ? 0 : detail.getAmount());
        }
    }

    private void normalizeMealDetail(ClaimDetail detail) {
        if (detail.getReceiptTime() == null) {
            detail.setDetailType("Meals");
            detail.setAllowedAmount(0.0);
            detail.setReimbursableAmount(0.0);
            return;
        }

        int hour = detail.getReceiptTime().getHour();
        if (hour < 12) {
            detail.setDetailType("Breakfast");
            detail.setAllowedAmount(150.0);
        } else if (hour < 16) {
            detail.setDetailType("Lunch");
            detail.setAllowedAmount(190.0);
        } else {
            detail.setDetailType("Supper");
            detail.setAllowedAmount(210.0);
        }

        double amount = detail.getAmount() == null ? 0 : detail.getAmount();
        detail.setReimbursableAmount(amount);
    }

    private Double getIncludedAmount(ClaimDetail detail) {
        if (detail == null) {
            return 0.0;
        }

        if ("Distance Travelled".equalsIgnoreCase(detail.getCategory())) {
            double kilometers = detail.getKilometers() == null ? 0 : detail.getKilometers();
            double rate = getDistanceRatePerKm(detail);
            if (kilometers > 0 && rate > 0) {
                return kilometers * rate;
            }
            if (detail.getReimbursableAmount() != null) {
                return detail.getReimbursableAmount();
            }
            return detail.getAmount() == null ? 0.0 : detail.getAmount();
        }

        if ("Meals".equalsIgnoreCase(detail.getCategory())
                || "Toll Fees".equalsIgnoreCase(detail.getCategory())
                || "Other".equalsIgnoreCase(detail.getCategory())) {
            return detail.getAmount() == null ? 0.0 : detail.getAmount();
        }

        return detail.getReimbursableAmount() == null ? 0.0 : detail.getReimbursableAmount();
    }

    private double getDistanceRatePerKm(ClaimDetail detail) {
        double engineSize = detail.getEngineSizeCc() == null ? 0 : detail.getEngineSizeCc();
        String vehicleType = detail.getVehicleType() == null ? "" : detail.getVehicleType().trim();

        if (engineSize <= 0 || vehicleType.isBlank()) {
            return 0.0;
        }

        if ("Diesel".equalsIgnoreCase(vehicleType)) {
            return getDieselRatePerKm(engineSize);
        }

        return getPetrolRatePerKm(engineSize);
    }

    private double getPetrolRatePerKm(double engineSize) {
        if (engineSize <= 1250) {
            return 3.172;
        }
        if (engineSize <= 1550) {
            return 3.992;
        }
        if (engineSize <= 1750) {
            return 4.337;
        }
        if (engineSize <= 1950) {
            return 4.999;
        }
        if (engineSize <= 2150) {
            return 5.359;
        }
        if (engineSize <= 2500) {
            return 6.068;
        }
        if (engineSize <= 3500) {
            return 7.576;
        }
        return 8.952;
    }

    private double getDieselRatePerKm(double engineSize) {
        if (engineSize <= 1250) {
            return 3.197;
        }
        if (engineSize <= 1550) {
            return 3.828;
        }
        if (engineSize <= 1750) {
            return 4.248;
        }
        if (engineSize <= 1950) {
            return 4.448;
        }
        if (engineSize <= 2150) {
            return 5.196;
        }
        if (engineSize <= 2500) {
            return 5.942;
        }
        return 7.375;
    }

    private void populateReceiptUrl(ClaimDetail detail) {
        if (detail.getClaimDetailId() != null && detail.getReceiptFileName() != null) {
            detail.setReceiptUrl("/claims/" + detail.getClaimId() + "/details/" + detail.getClaimDetailId() + "/receipt");
        }
    }

    private String generateClaimReference(Claim claim) {
        String claimReference;
        int secondOffset = 0;
        do {
            claimReference = generateReference(claim.getClaimDate(), secondOffset++);
        } while (claim.getUserId() != null && claimDao.existsByUserIdAndClaimReference(claim.getUserId(), claimReference));

        return claimReference;
    }

    private String generateReference(Date claimDate, int secondOffset) {
        LocalDate referenceDate = claimDate == null
                ? LocalDate.now()
                : claimDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalTime referenceTime = LocalTime.now().plusSeconds(secondOffset);
        return referenceDate.format(DateTimeFormatter.ofPattern("MMdd"))
                + referenceTime.format(DateTimeFormatter.ofPattern("HHmmss"));
    }

    private void hydrateClaim(Claim claim) {
        populateUserName(claim);
        claim.setClaimDetails(getClaimDetails(claim.getClaimId()));
    }

    private void populateUserName(Claim claim) {
        if (claim.getUserId() == null) {
            claim.setUserName("-");
            return;
        }

        userDao.findById(claim.getUserId()).ifPresentOrElse(
                user -> claim.setUserName(getDisplayName(user)),
                () -> claim.setUserName("-")
        );
        claim.setUserSubmissionCount(countClaimsByUserId(claim.getUserId()));
    }

    private String getDisplayName(User user) {
        if (user.getUserFirstName() != null && !user.getUserFirstName().isBlank()
                && user.getUserLastName() != null && !user.getUserLastName().isBlank()) {
            return user.getUserFirstName() + " " + user.getUserLastName();
        }

        return user.getUserName();
    }
}
