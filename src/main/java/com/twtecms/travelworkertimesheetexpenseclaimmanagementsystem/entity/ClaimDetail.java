package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.time.LocalTime;

@Entity
@Table(name = "claim_detail")
public class ClaimDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long claimDetailId;

    private Long claimId;
    private String category;
    private String detailType;
    private String description;
    private Double kilometers;
    private String vehicleType;
    private Double engineSizeCc;
    private LocalTime receiptTime;
    private Double amount;
    private Double allowedAmount;
    private Double reimbursableAmount;
    private String receiptFileName;
    private String receiptContentType;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "LONGBLOB")
    private byte[] receiptData;

    @Transient
    private String receiptUrl;

    public Long getClaimDetailId() {
        return claimDetailId;
    }

    public void setClaimDetailId(Long claimDetailId) {
        this.claimDetailId = claimDetailId;
    }

    public Long getClaimId() {
        return claimId;
    }

    public void setClaimId(Long claimId) {
        this.claimId = claimId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDetailType() {
        return detailType;
    }

    public void setDetailType(String detailType) {
        this.detailType = detailType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getKilometers() {
        return kilometers;
    }

    public void setKilometers(Double kilometers) {
        this.kilometers = kilometers;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Double getEngineSizeCc() {
        return engineSizeCc;
    }

    public void setEngineSizeCc(Double engineSizeCc) {
        this.engineSizeCc = engineSizeCc;
    }

    public LocalTime getReceiptTime() {
        return receiptTime;
    }

    public void setReceiptTime(LocalTime receiptTime) {
        this.receiptTime = receiptTime;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getAllowedAmount() {
        return allowedAmount;
    }

    public void setAllowedAmount(Double allowedAmount) {
        this.allowedAmount = allowedAmount;
    }

    public Double getReimbursableAmount() {
        return reimbursableAmount;
    }

    public void setReimbursableAmount(Double reimbursableAmount) {
        this.reimbursableAmount = reimbursableAmount;
    }

    public String getReceiptFileName() {
        return receiptFileName;
    }

    public void setReceiptFileName(String receiptFileName) {
        this.receiptFileName = receiptFileName;
    }

    public String getReceiptContentType() {
        return receiptContentType;
    }

    public void setReceiptContentType(String receiptContentType) {
        this.receiptContentType = receiptContentType;
    }

    public byte[] getReceiptData() {
        return receiptData;
    }

    public void setReceiptData(byte[] receiptData) {
        this.receiptData = receiptData;
    }

    public String getReceiptUrl() {
        return receiptUrl;
    }

    public void setReceiptUrl(String receiptUrl) {
        this.receiptUrl = receiptUrl;
    }
}
