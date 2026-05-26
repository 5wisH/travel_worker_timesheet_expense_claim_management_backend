package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.controller;

import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao.BankAccountDetailsDao;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao.ClaimDetailDao;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao.ClaimImageDao;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao.PaymentDao;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao.TimesheetDao;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao.UserDao;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.BankAccountDetails;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.Claim;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.GeneratedReport;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.Timesheet;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.User;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.service.ClaimService;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.service.GeneratedReportService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@PreAuthorize("hasRole('Admin')")
public class AdminController {

    @Autowired
    private ClaimService claimService;

    @Autowired
    private TimesheetDao timesheetDao;

    @Autowired
    private BankAccountDetailsDao bankAccountDetailsDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ClaimDetailDao claimDetailDao;

    @Autowired
    private ClaimImageDao claimImageDao;

    @Autowired
    private PaymentDao paymentDao;

    @Autowired
    private GeneratedReportService generatedReportService;

    @GetMapping({"/admin/claims"})
    public List<Claim> getClaims() {
        return claimService.getClaims();
    }

    @PostMapping({"/admin/claims"})
    public Claim createClaim(@RequestBody Claim claim) {
        return claimService.saveClaim(claim, null, null);
    }

    @PutMapping({"/admin/claims/{claimId}"})
    public Claim updateClaim(@PathVariable Long claimId, @RequestBody Claim updatedClaim) {
        Claim claim = claimService.getClaimById(claimId);
        claim.setUserId(updatedClaim.getUserId());
        claim.setClaimReference(updatedClaim.getClaimReference());
        claim.setClaimDate(updatedClaim.getClaimDate());
        claim.setCategories(updatedClaim.getCategories());
        claim.setStatus(updatedClaim.getStatus());
        claim.setTotal_amount(updatedClaim.getTotal_amount());
        claim.setManagerId(updatedClaim.getManagerId());
        return claimService.saveClaimRecord(claim);
    }

    @DeleteMapping({"/admin/claims/{claimId}"})
    @Transactional
    public void deleteClaim(@PathVariable Long claimId) {
        claimDetailDao.deleteByClaimId(claimId);
        claimImageDao.deleteByClaimId(claimId);
        bankAccountDetailsDao.deleteByClaimId(claimId);
        paymentDao.deleteByClaimId(claimId);
        claimService.deleteClaim(claimId);
    }

    @GetMapping({"/admin/timesheets"})
    public List<Timesheet> getTimesheets() {
        return timesheetDao.findAll();
    }

    @PostMapping({"/admin/timesheets"})
    public Timesheet createTimesheet(@RequestBody Timesheet timesheet) {
        return timesheetDao.save(timesheet);
    }

    @PutMapping({"/admin/timesheets/{timesheetId}"})
    public Timesheet updateTimesheet(@PathVariable Long timesheetId, @RequestBody Timesheet updatedTimesheet) {
        Timesheet timesheet = timesheetDao.findById(timesheetId)
                .orElseThrow(() -> new RuntimeException("Timesheet not found with id: " + timesheetId));
        timesheet.setUserId(updatedTimesheet.getUserId());
        timesheet.setWorkDate(updatedTimesheet.getWorkDate());
        timesheet.setStartTime(updatedTimesheet.getStartTime());
        timesheet.setEndTime(updatedTimesheet.getEndTime());
        timesheet.setTotal_hours(updatedTimesheet.getTotal_hours());
        timesheet.setLocation(updatedTimesheet.getLocation());
        timesheet.setDescription(updatedTimesheet.getDescription());
        timesheet.setStatus(updatedTimesheet.getStatus());
        return timesheetDao.save(timesheet);
    }

    @DeleteMapping({"/admin/timesheets/{timesheetId}"})
    public void deleteTimesheet(@PathVariable Long timesheetId) {
        timesheetDao.deleteById(timesheetId);
    }

    @GetMapping({"/admin/bank-details"})
    public List<BankAccountDetails> getBankDetails() {
        return bankAccountDetailsDao.findAll();
    }

    @PostMapping({"/admin/bank-details"})
    public BankAccountDetails createBankDetails(@RequestBody BankAccountDetails bankAccountDetails) {
        bankAccountDetails.setBankDetailsId(null);
        return bankAccountDetailsDao.save(bankAccountDetails);
    }

    @PutMapping({"/admin/bank-details/{bankDetailsId}"})
    public BankAccountDetails updateBankDetails(@PathVariable Long bankDetailsId, @RequestBody BankAccountDetails updatedBankDetails) {
        BankAccountDetails bankAccountDetails = bankAccountDetailsDao.findById(bankDetailsId)
                .orElseThrow(() -> new RuntimeException("Bank details not found with id: " + bankDetailsId));
        bankAccountDetails.setClaimId(updatedBankDetails.getClaimId());
        bankAccountDetails.setUserId(updatedBankDetails.getUserId());
        bankAccountDetails.setBankName(updatedBankDetails.getBankName());
        bankAccountDetails.setAccountNumber(updatedBankDetails.getAccountNumber());
        bankAccountDetails.setAccountType(updatedBankDetails.getAccountType());
        return bankAccountDetailsDao.save(bankAccountDetails);
    }

    @DeleteMapping({"/admin/bank-details/{bankDetailsId}"})
    public void deleteBankDetails(@PathVariable Long bankDetailsId) {
        bankAccountDetailsDao.deleteById(bankDetailsId);
    }

    @GetMapping({"/admin/users"})
    public List<User> getUsers() {
        return userDao.findAll();
    }

    @GetMapping({"/admin/reports"})
    public List<GeneratedReport> getReports() {
        return generatedReportService.getReports();
    }

    @GetMapping({"/admin/reports/{reportId}"})
    public GeneratedReport getReport(@PathVariable Long reportId) {
        return generatedReportService.getReport(reportId);
    }

    @PostMapping({"/admin/reports/claims"})
    public GeneratedReport generateClaimsReport(Principal principal) {
        return generatedReportService.generateClaimsReport(getPrincipalUser(principal));
    }

    @PostMapping({"/admin/reports/timesheets"})
    public GeneratedReport generateTimesheetsReport(Principal principal) {
        return generatedReportService.generateTimesheetsReport(getPrincipalUser(principal));
    }

    @PutMapping({"/admin/reports/{reportId}"})
    public GeneratedReport updateReport(@PathVariable Long reportId, @RequestBody GeneratedReport updatedReport) {
        return generatedReportService.updateReport(reportId, updatedReport);
    }

    @DeleteMapping({"/admin/reports/{reportId}"})
    public void deleteReport(@PathVariable Long reportId) {
        generatedReportService.deleteReport(reportId);
    }

    private User getPrincipalUser(Principal principal) {
        if (principal == null) {
            return null;
        }

        return userDao.findByUserName(principal.getName());
    }
}
