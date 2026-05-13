package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity;

import java.util.List;

public class ClaimCalculationResponse {
    private Long claimId;
    private String claimReference;
    private Double totalAmount;
    private List<ClaimDetail> details;

    public ClaimCalculationResponse(Long claimId, String claimReference, Double totalAmount, List<ClaimDetail> details) {
        this.claimId = claimId;
        this.claimReference = claimReference;
        this.totalAmount = totalAmount;
        this.details = details;
    }

    public Long getClaimId() {
        return claimId;
    }

    public void setClaimId(Long claimId) {
        this.claimId = claimId;
    }

    public String getClaimReference() {
        return claimReference;
    }

    public void setClaimReference(String claimReference) {
        this.claimReference = claimReference;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<ClaimDetail> getDetails() {
        return details;
    }

    public void setDetails(List<ClaimDetail> details) {
        this.details = details;
    }
}
