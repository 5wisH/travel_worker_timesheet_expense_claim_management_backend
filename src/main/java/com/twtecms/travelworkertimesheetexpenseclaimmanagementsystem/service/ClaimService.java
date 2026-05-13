package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao.ClaimDetailDao;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao.ClaimDao;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao.ClaimImageDao;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao.UserDao;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.Claim;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.ClaimCalculationResponse;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.ClaimDetail;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.ClaimImage;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.ClaimImageResponse;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ClaimService {

    @Autowired
    private ClaimDao claimDao;

    @Autowired
    private ClaimImageDao claimImageDao;

    @Autowired
    private ClaimDetailDao claimDetailDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ObjectMapper objectMapper;

    private static final double DISTANCE_RATE_PER_KM = 5.00;

    public Claim saveClaim(Claim claim, MultipartFile[] files, String claimDetailsJson) {
        if (claim.getStatus() == null || claim.getStatus().isBlank() || "true".equalsIgnoreCase(claim.getStatus())) {
            claim.setStatus("Submitted");
        }

        if (claim.getClaimReference() == null || claim.getClaimReference().isBlank()) {
            claim.setClaimReference(generateClaimReference(claim.getUserId()));
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
        populateUserName(savedClaim);
        return savedClaim;
    }

    public List<Claim> getClaims() {
        return claimDao.findAll()
                .stream()
                .peek(this::populateUserName)
                .toList();
    }

    public List<Claim> getClaimsByUserId(Long userId) {
        return claimDao.findByUserIdOrderByClaimDateDesc(userId)
                .stream()
                .peek(this::populateUserName)
                .peek(claim -> claim.setUserSubmissionCount(countClaimsByUserId(userId)))
                .toList();
    }

    public long countClaimsByUserId(Long userId) {
        return claimDao.countByUserId(userId);
    }

    public Claim updateClaimStatus(Long claimId, String status) {
        Claim claim = claimDao.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found with id: " + claimId));
        claim.setStatus(status);
        Claim savedClaim = claimDao.save(claim);
        populateUserName(savedClaim);
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
                .map(ClaimDetail::getReimbursableAmount)
                .filter(amount -> amount != null)
                .mapToDouble(Double::doubleValue)
                .sum();
        claim.setTotal_amount(totalAmount);
        claimDao.save(claim);
        return new ClaimCalculationResponse(claimId, claim.getClaimReference(), totalAmount, details);
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
            List<ClaimDetail> claimDetails = objectMapper.readValue(claimDetailsJson, new TypeReference<List<ClaimDetail>>() {});
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
            detail.setAllowedAmount(null);
            detail.setAmount(kilometers * DISTANCE_RATE_PER_KM);
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
        detail.setReimbursableAmount(Math.min(amount, detail.getAllowedAmount()));
    }

    private void populateReceiptUrl(ClaimDetail detail) {
        if (detail.getClaimDetailId() != null && detail.getReceiptFileName() != null) {
            detail.setReceiptUrl("/claims/" + detail.getClaimId() + "/details/" + detail.getClaimDetailId() + "/receipt");
        }
    }

    private String generateClaimReference(Long userId) {
        String claimReference;
        do {
            claimReference = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        } while (userId != null && claimDao.existsByUserIdAndClaimReference(userId, claimReference));

        return claimReference;
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
