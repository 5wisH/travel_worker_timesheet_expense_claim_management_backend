package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Entity
public class Claim {
    // properties of the Claim class
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long claimId; // auto generate the Id of the claim
    private Long userId; // foreign key that links a claim to the user entity
    private String claimReference; // unique 6 digit claim reference
    private Date claimDate;  // The date the the claim was issued by the employee
    private String categories; // selected claim categories, like Meals, Fuel, Parking
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> claimImages = new ArrayList<>(); // uploaded supporting document names
    private String status; // claim status, for example Submitted, Approved, Rejected
    private Double total_amount; // the amount present for the claim
    private Long managerId;
    @Transient
    private String userName;
    @Transient
    private Long userSubmissionCount;

    public Long getClaimId() {
        return claimId;
    }

    public void setClaimId(Long claimId) {
        this.claimId = claimId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getClaimReference() {
        return claimReference;
    }

    public void setClaimReference(String claimReference) {
        this.claimReference = claimReference;
    }

    public Date getClaimDate() {
        return claimDate;
    }

    public void setClaimDate(Date claimDate) {
        this.claimDate = claimDate;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public List<String> getClaimImages() {
        return claimImages;
    }

    public void setClaimImages(List<String> claimImages) {
        this.claimImages = claimImages;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(Double total_amount) {
        this.total_amount = total_amount;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getUserSubmissionCount() {
        return userSubmissionCount;
    }

    public void setUserSubmissionCount(Long userSubmissionCount) {
        this.userSubmissionCount = userSubmissionCount;
    }
}
