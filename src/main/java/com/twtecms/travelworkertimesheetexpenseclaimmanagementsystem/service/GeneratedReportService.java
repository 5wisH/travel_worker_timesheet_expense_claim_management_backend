package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.service;

import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao.GeneratedReportDao;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao.TimesheetDao;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.Claim;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.GeneratedReport;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.Timesheet;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.User;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@Service
public class GeneratedReportService {

    private static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final GeneratedReportDao generatedReportDao;
    private final ClaimService claimService;
    private final TimesheetDao timesheetDao;

    public GeneratedReportService(
            GeneratedReportDao generatedReportDao,
            ClaimService claimService,
            TimesheetDao timesheetDao
    ) {
        this.generatedReportDao = generatedReportDao;
        this.claimService = claimService;
        this.timesheetDao = timesheetDao;
    }

    public GeneratedReport generateClaimsReport(User generatedBy) {
        List<Claim> claims = claimService.getClaims();
        LocalDateTime generatedAt = LocalDateTime.now();
        String excelContent = buildClaimsWorkbook(claims);
        return saveReport("CLAIMS", "claims-report", generatedBy, generatedAt, claims.size(), excelContent);
    }

    public GeneratedReport generateTimesheetsReport(User generatedBy) {
        List<Timesheet> timesheets = timesheetDao.findAll();
        LocalDateTime generatedAt = LocalDateTime.now();
        String excelContent = buildTimesheetsWorkbook(timesheets);
        return saveReport("TIMESHEETS", "timesheets-report", generatedBy, generatedAt, timesheets.size(), excelContent);
    }

    public List<GeneratedReport> getReports() {
        return generatedReportDao.findAllByOrderByGeneratedAtDesc();
    }

    public GeneratedReport getReport(Long reportId) {
        return generatedReportDao.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Generated report not found with id: " + reportId));
    }

    public GeneratedReport updateReport(Long reportId, GeneratedReport updatedReport) {
        GeneratedReport report = getReport(reportId);
        report.setReportType(updatedReport.getReportType());
        report.setFileName(updatedReport.getFileName());
        report.setGeneratedByUserId(updatedReport.getGeneratedByUserId());
        report.setGeneratedByUserName(updatedReport.getGeneratedByUserName());
        report.setGeneratedAt(updatedReport.getGeneratedAt());
        report.setRecordCount(updatedReport.getRecordCount());
        report.setContentType(updatedReport.getContentType());
        report.setFileContentBase64(updatedReport.getFileContentBase64());
        return generatedReportDao.save(report);
    }

    public void deleteReport(Long reportId) {
        generatedReportDao.deleteById(reportId);
    }

    private GeneratedReport saveReport(
            String reportType,
            String filePrefix,
            User generatedBy,
            LocalDateTime generatedAt,
            int recordCount,
            String fileContentBase64
    ) {
        GeneratedReport report = new GeneratedReport();
        report.setReportType(reportType);
        report.setFileName(filePrefix + "-" + generatedAt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx");
        report.setGeneratedAt(generatedAt);
        report.setGeneratedByUserId(generatedBy != null ? generatedBy.getUserId() : null);
        report.setGeneratedByUserName(generatedBy != null ? generatedBy.getUserName() : null);
        report.setRecordCount(recordCount);
        report.setContentType(EXCEL_CONTENT_TYPE);
        report.setFileContentBase64(fileContentBase64);
        return generatedReportDao.save(report);
    }

    private String buildClaimsWorkbook(List<Claim> claims) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Claims");
            writeHeader(workbook, sheet, "Claim ID", "Reference", "User ID", "User Name", "Claim Date", "Categories", "Status", "Total Amount", "Manager ID");

            int rowIndex = 1;
            for (Claim claim : claims) {
                Row row = sheet.createRow(rowIndex++);
                writeCell(row, 0, claim.getClaimId());
                writeCell(row, 1, claim.getClaimReference());
                writeCell(row, 2, claim.getUserId());
                writeCell(row, 3, claim.getUserName());
                writeCell(row, 4, claim.getClaimDate());
                writeCell(row, 5, claim.getCategories());
                writeCell(row, 6, claim.getStatus());
                writeCell(row, 7, claim.getTotal_amount());
                writeCell(row, 8, claim.getManagerId());
            }

            autosize(sheet, 9);
            return workbookToBase64(workbook);
        } catch (IOException error) {
            throw new RuntimeException("Claims Excel report could not be generated.", error);
        }
    }

    private String buildTimesheetsWorkbook(List<Timesheet> timesheets) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Timesheets");
            writeHeader(workbook, sheet, "Timesheet ID", "User ID", "Work Date", "Start Time", "End Time", "Total Hours", "Location", "Description", "Status");

            int rowIndex = 1;
            for (Timesheet timesheet : timesheets) {
                Row row = sheet.createRow(rowIndex++);
                writeCell(row, 0, timesheet.getTimesheetId());
                writeCell(row, 1, timesheet.getUserId());
                writeCell(row, 2, timesheet.getWorkDate());
                writeCell(row, 3, timesheet.getStartTime());
                writeCell(row, 4, timesheet.getEndTime());
                writeCell(row, 5, timesheet.getTotal_hours());
                writeCell(row, 6, timesheet.getLocation());
                writeCell(row, 7, timesheet.getDescription());
                writeCell(row, 8, timesheet.getStatus());
            }

            autosize(sheet, 9);
            return workbookToBase64(workbook);
        } catch (IOException error) {
            throw new RuntimeException("Timesheets Excel report could not be generated.", error);
        }
    }

    private void writeHeader(Workbook workbook, Sheet sheet, String... headings) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        Row row = sheet.createRow(0);
        for (int column = 0; column < headings.length; column++) {
            row.createCell(column).setCellValue(headings[column]);
            row.getCell(column).setCellStyle(headerStyle);
        }
    }

    private void writeCell(Row row, int column, Object value) {
        if (value instanceof Number number) {
            row.createCell(column).setCellValue(number.doubleValue());
            return;
        }

        row.createCell(column).setCellValue(value == null ? "" : String.valueOf(value));
    }

    private void autosize(Sheet sheet, int columns) {
        for (int column = 0; column < columns; column++) {
            sheet.autoSizeColumn(column);
        }
    }

    private String workbookToBase64(Workbook workbook) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }
}
