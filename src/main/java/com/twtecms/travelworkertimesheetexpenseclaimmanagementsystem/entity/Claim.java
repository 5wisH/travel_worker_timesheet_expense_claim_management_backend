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
    private String claimReference; // unique 10 digit claim reference
    private Date claimDate;  // The date the the claim was issued by the employee
    private String capturedBy;
    private Date dateCaptured;
    private Double advanceTaken;
    private Double amount;
    private String categories; // selected claim categories, like Meals, Fuel, Parking
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> claimImages = new ArrayList<>(); // uploaded supporting document names
    private String status; // claim status, for example Submitted, Approved, Rejected
    private Double total_amount; // the amount present for the claim
    private Long managerId;
    private String claimDescription;
    @Column(name = "claim_rank")
    private String rank;
    private String departureDate;
    private String arrivalDateTime;
    private Double dateNumberOfDays;
    private String departureTime;
    private String arrivalTime;
    private Double timeNumberOfDays;
    private Double numberOfHours;
    private String privateMotorClaimedBy;
    private String privateMotorDepartmentOf;
    private String privateMotorRank;
    @Column(length = 1000)
    private String privateMotorAddress;
    private String privateMotorMonth;
    private String privateMotorAccountClaimNo;
    private String privateMotorHeadquarters;
    private String privateMotorMakeAndModel;
    private String privateMotorCategory;
    private String privateMotorYearOfManufacture;
    private String privateMotorVehicleType;
    private String privateMotorRegistrationNumber;
    private String privateMotorEngineSweptVolumeGroup;
    @Transient
    private String userName;
    @Transient
    private Long userSubmissionCount;
    @Transient
    private List<ClaimDetail> claimDetails = new ArrayList<>();

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

    public String getCapturedBy() {
        return capturedBy;
    }

    public void setCapturedBy(String capturedBy) {
        this.capturedBy = capturedBy;
    }

    public Date getDateCaptured() {
        return dateCaptured;
    }

    public void setDateCaptured(Date dateCaptured) {
        this.dateCaptured = dateCaptured;
    }

    public Double getAdvanceTaken() {
        return advanceTaken;
    }

    public void setAdvanceTaken(Double advanceTaken) {
        this.advanceTaken = advanceTaken;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
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

    public String getClaimDescription() {
        return claimDescription;
    }

    public void setClaimDescription(String claimDescription) {
        this.claimDescription = claimDescription;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(String departureDate) {
        this.departureDate = departureDate;
    }

    public String getArrivalDateTime() {
        return arrivalDateTime;
    }

    public void setArrivalDateTime(String arrivalDateTime) {
        this.arrivalDateTime = arrivalDateTime;
    }

    public Double getDateNumberOfDays() {
        return dateNumberOfDays;
    }

    public void setDateNumberOfDays(Double dateNumberOfDays) {
        this.dateNumberOfDays = dateNumberOfDays;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Double getTimeNumberOfDays() {
        return timeNumberOfDays;
    }

    public void setTimeNumberOfDays(Double timeNumberOfDays) {
        this.timeNumberOfDays = timeNumberOfDays;
    }

    public Double getNumberOfHours() {
        return numberOfHours;
    }

    public void setNumberOfHours(Double numberOfHours) {
        this.numberOfHours = numberOfHours;
    }

    public String getPrivateMotorClaimedBy() {
        return privateMotorClaimedBy;
    }

    public void setPrivateMotorClaimedBy(String privateMotorClaimedBy) {
        this.privateMotorClaimedBy = privateMotorClaimedBy;
    }

    public String getPrivateMotorDepartmentOf() {
        return privateMotorDepartmentOf;
    }

    public void setPrivateMotorDepartmentOf(String privateMotorDepartmentOf) {
        this.privateMotorDepartmentOf = privateMotorDepartmentOf;
    }

    public String getPrivateMotorRank() {
        return privateMotorRank;
    }

    public void setPrivateMotorRank(String privateMotorRank) {
        this.privateMotorRank = privateMotorRank;
    }

    public String getPrivateMotorAddress() {
        return privateMotorAddress;
    }

    public void setPrivateMotorAddress(String privateMotorAddress) {
        this.privateMotorAddress = privateMotorAddress;
    }

    public String getPrivateMotorMonth() {
        return privateMotorMonth;
    }

    public void setPrivateMotorMonth(String privateMotorMonth) {
        this.privateMotorMonth = privateMotorMonth;
    }

    public String getPrivateMotorAccountClaimNo() {
        return privateMotorAccountClaimNo;
    }

    public void setPrivateMotorAccountClaimNo(String privateMotorAccountClaimNo) {
        this.privateMotorAccountClaimNo = privateMotorAccountClaimNo;
    }

    public String getPrivateMotorHeadquarters() {
        return privateMotorHeadquarters;
    }

    public void setPrivateMotorHeadquarters(String privateMotorHeadquarters) {
        this.privateMotorHeadquarters = privateMotorHeadquarters;
    }

    public String getPrivateMotorMakeAndModel() {
        return privateMotorMakeAndModel;
    }

    public void setPrivateMotorMakeAndModel(String privateMotorMakeAndModel) {
        this.privateMotorMakeAndModel = privateMotorMakeAndModel;
    }

    public String getPrivateMotorCategory() {
        return privateMotorCategory;
    }

    public void setPrivateMotorCategory(String privateMotorCategory) {
        this.privateMotorCategory = privateMotorCategory;
    }

    public String getPrivateMotorYearOfManufacture() {
        return privateMotorYearOfManufacture;
    }

    public void setPrivateMotorYearOfManufacture(String privateMotorYearOfManufacture) {
        this.privateMotorYearOfManufacture = privateMotorYearOfManufacture;
    }

    public String getPrivateMotorVehicleType() {
        return privateMotorVehicleType;
    }

    public void setPrivateMotorVehicleType(String privateMotorVehicleType) {
        this.privateMotorVehicleType = privateMotorVehicleType;
    }

    public String getPrivateMotorRegistrationNumber() {
        return privateMotorRegistrationNumber;
    }

    public void setPrivateMotorRegistrationNumber(String privateMotorRegistrationNumber) {
        this.privateMotorRegistrationNumber = privateMotorRegistrationNumber;
    }

    public String getPrivateMotorEngineSweptVolumeGroup() {
        return privateMotorEngineSweptVolumeGroup;
    }

    public void setPrivateMotorEngineSweptVolumeGroup(String privateMotorEngineSweptVolumeGroup) {
        this.privateMotorEngineSweptVolumeGroup = privateMotorEngineSweptVolumeGroup;
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

    public List<ClaimDetail> getClaimDetails() {
        return claimDetails;
    }

    public void setClaimDetails(List<ClaimDetail> claimDetails) {
        this.claimDetails = claimDetails;
    }

}
