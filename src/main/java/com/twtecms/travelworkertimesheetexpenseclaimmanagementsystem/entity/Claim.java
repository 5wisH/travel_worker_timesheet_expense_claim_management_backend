package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity;

import jakarta.persistence.*;

import java.util.Date;


@Entity
public class Claim {
    // properties of the Claim class
    @Id
    private Long claimId; // auto generate the Id of the claim
    private Long userId; // foreign key that links a claim to the user entity
    private Date claimDate;  // The date the the claim was issued by the employee
    private Boolean status; // flag to check whether the claim was lodged
    private Double total_amount; // the amount present for the claim
    private Long managerId;


}
