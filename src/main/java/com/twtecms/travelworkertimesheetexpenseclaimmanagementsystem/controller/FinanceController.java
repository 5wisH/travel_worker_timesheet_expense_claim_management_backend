package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.controller;

import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao.PaymentDao;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.Claim;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.Payment;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.service.ClaimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/finance")
@PreAuthorize("hasRole('Finance')")
public class FinanceController {

    private static final Set<String> FINANCE_QUEUE_STATUSES = Set.of(
            "Approved",
            "Awaiting Finance",
            "Logistics Verified",
            "Payment Approval",
            "Ready for Payment"
    );

    @Autowired
    private ClaimService claimService;

    @Autowired
    private PaymentDao paymentDao;

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() {
        List<Claim> claims = claimService.getClaims();
        List<Payment> payments = paymentDao.findAll();

        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("totalClaims", claims.size());
        dashboard.put("awaitingFinanceReview", countClaimsByStatus(claims, FINANCE_QUEUE_STATUSES));
        dashboard.put("paymentApprovals", countClaimsByStatus(claims, Set.of("Payment Approval", "Ready for Payment")));
        dashboard.put("financeApproved", countClaimsByStatus(claims, Set.of("Finance Approved")));
        dashboard.put("paidClaims", countClaimsByStatus(claims, Set.of("Paid")));
        dashboard.put("paidThisMonth", totalPaidAmountThisMonth(claims, payments));
        return dashboard;
    }

    @GetMapping("/claims")
    public List<Claim> getClaims() {
        return claimService.getClaims();
    }

    @GetMapping("/claims/queue")
    public List<Claim> getFinanceQueue() {
        return claimService.getClaims()
                .stream()
                .filter(claim -> hasStatus(claim, FINANCE_QUEUE_STATUSES))
                .toList();
    }

    @GetMapping("/claims/approved")
    public List<Claim> getFinanceApprovedClaims() {
        return claimService.getClaims()
                .stream()
                .filter(claim -> hasStatus(claim, Set.of("Finance Approved")))
                .toList();
    }

    @GetMapping("/claims/paid")
    public List<Claim> getPaidClaims() {
        return claimService.getClaims()
                .stream()
                .filter(claim -> hasStatus(claim, Set.of("Paid")))
                .toList();
    }

    @PutMapping("/claims/{claimId}/status")
    public Claim updateClaimStatus(@PathVariable Long claimId, @RequestBody Map<String, String> request) {
        String status = request == null ? null : request.get("status");
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("status is required");
        }

        return claimService.updateClaimStatus(claimId, status);
    }

    @PostMapping("/claims/{claimId}/approve")
    public Claim approveClaim(@PathVariable Long claimId) {
        return claimService.updateClaimStatus(claimId, "Finance Approved");
    }

    @PostMapping("/claims/{claimId}/pay")
    public Payment payClaim(@PathVariable Long claimId, Principal principal) {
        return claimService.payClaim(claimId, principal == null ? "Finance" : principal.getName());
    }

    @GetMapping("/payments")
    public List<Payment> getPayments() {
        return paymentDao.findAll();
    }

    private long countClaimsByStatus(List<Claim> claims, Set<String> statuses) {
        return claims.stream()
                .filter(claim -> hasStatus(claim, statuses))
                .count();
    }

    private boolean hasStatus(Claim claim, Set<String> statuses) {
        String status = claim.getStatus();
        return status != null && statuses.stream().anyMatch(status::equalsIgnoreCase);
    }

    private double totalPaidAmountThisMonth(List<Claim> claims, List<Payment> payments) {
        Set<Long> paidClaimIds = payments.stream()
                .filter(payment -> Boolean.TRUE.equals(payment.getStatus()))
                .map(Payment::getClaimId)
                .collect(java.util.stream.Collectors.toSet());

        return claims.stream()
                .filter(claim -> claim.getClaimId() != null && paidClaimIds.contains(claim.getClaimId()))
                .map(Claim::getTotal_amount)
                .filter(amount -> amount != null)
                .mapToDouble(Double::doubleValue)
                .sum();
    }
}
