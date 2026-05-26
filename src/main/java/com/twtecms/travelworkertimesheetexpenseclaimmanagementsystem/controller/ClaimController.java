package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.controller;

import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.Claim;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.ClaimCalculationResponse;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.ClaimDetail;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.ClaimImage;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.ClaimImageResponse;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.Payment;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.service.ClaimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
public class ClaimController {

    @Autowired
    private ClaimService claimService;

    @PostMapping({"/claims/save"})
    public Claim saveClaim(
            @RequestPart("claim") Claim claim,
            @RequestPart(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "details", required = false) String claimDetailsJson,
            @RequestParam(value = "documentMetadata", required = false) String documentMetadataJson) {
        return claimService.saveClaim(claim, files, claimDetailsJson, documentMetadataJson);
    }

    @PostMapping({"/claims"})
    public Claim createClaim(@RequestBody Claim claim) {
        Date now = new Date();

        if (claim.getClaimDate() == null) {
            claim.setClaimDate(now);
        }
        if (claim.getDateCaptured() == null) {
            claim.setDateCaptured(now);
        }
        if (claim.getStatus() == null || claim.getStatus().isBlank()) {
            claim.setStatus("Submitted");
        }
        if (claim.getTotal_amount() == null) {
            claim.setTotal_amount(claim.getAmount());
        }

        return claimService.saveClaimRecord(claim);
    }

    @GetMapping({"/claims"})
    public List<Claim> getClaims() {
        return claimService.getClaims();
    }

    @GetMapping({"/claims/user/{userId}"})
    public List<Claim> getClaimsByUserId(@PathVariable Long userId) {
        return claimService.getClaimsByUserId(userId);
    }

    @GetMapping({"/claims/user/{userId}/count"})
    public Map<String, Long> countClaimsByUserId(@PathVariable Long userId) {
        return Map.of("userSubmissionCount", claimService.countClaimsByUserId(userId));
    }

    @GetMapping({"/claims/{claimId}/images"})
    public List<ClaimImageResponse> getClaimImages(@PathVariable Long claimId) {
        return claimService.getClaimImages(claimId);
    }

    @GetMapping({"/claims/{claimId}/details"})
    public List<ClaimDetail> getClaimDetails(@PathVariable Long claimId) {
        return claimService.getClaimDetails(claimId);
    }

    @GetMapping({"/claims/{claimId}/calculate"})
    public ClaimCalculationResponse calculateClaimTotal(@PathVariable Long claimId) {
        return claimService.calculateClaimTotal(claimId);
    }

    @PostMapping({"/claims/{claimId}/pay"})
    public Payment payClaim(@PathVariable Long claimId, @RequestBody Map<String, String> request) {
        return claimService.payClaim(claimId, request.get("processedBy"));
    }

    @GetMapping({"/claims/{claimId}/details/{detailId}/receipt"})
    public ResponseEntity<byte[]> getClaimDetailReceipt(@PathVariable Long claimId, @PathVariable Long detailId) {
        ClaimDetail claimDetail = claimService.getClaimDetailReceipt(claimId, detailId);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (claimDetail.getReceiptContentType() != null && !claimDetail.getReceiptContentType().isBlank()) {
            mediaType = MediaType.parseMediaType(claimDetail.getReceiptContentType());
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + claimDetail.getReceiptFileName() + "\"")
                .body(claimDetail.getReceiptData());
    }

    @GetMapping({"/claims/{claimId}/images/{imageId}"})
    public ResponseEntity<byte[]> getClaimImage(@PathVariable Long claimId, @PathVariable Long imageId) {
        ClaimImage claimImage = claimService.getClaimImage(claimId, imageId);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (claimImage.getContentType() != null && !claimImage.getContentType().isBlank()) {
            mediaType = MediaType.parseMediaType(claimImage.getContentType());
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + claimImage.getFileName() + "\"")
                .body(claimImage.getData());
    }

    @PutMapping({"/claims/{claimId}/status"})
    public Claim updateClaimStatus(@PathVariable Long claimId, @RequestBody Map<String, String> request) {
        return claimService.updateClaimStatus(claimId, request.get("status"));
    }
}
