package org.trackdev.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.trackdev.api.controller.exceptions.ControllerException;
import org.trackdev.api.dto.ReportBasicDTO;
import org.trackdev.api.entity.Report;
import org.trackdev.api.mapper.ReportMapper;
import org.trackdev.api.model.MergePatchReport;
import org.trackdev.api.service.ReportService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import java.security.Principal;
import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "10. Reports")
@RestController
@RequestMapping(path = "/reports")
@PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
public class ReportController extends BaseController {

    @Autowired
    ReportService reportService;

    @Autowired
    ReportMapper reportMapper;

    @Operation(summary = "Get all reports", description = "Get all reports (PROFESSOR only)")
    @GetMapping
    public List<ReportBasicDTO> getReports(Principal principal) {
        String userId = super.getUserId(principal);
        List<Report> reports = reportService.getReportsForUser(userId);
        return reportMapper.toBasicDTOList(reports);
    }

    @Operation(summary = "Get a specific report", description = "Get a report by ID (PROFESSOR only)")
    @GetMapping(path = "/{id}")
    public ReportBasicDTO getReport(Principal principal, @PathVariable(name = "id") Long id) {
        String userId = super.getUserId(principal);
        Report report = reportService.getReport(id, userId);
        return reportMapper.toBasicDTO(report);
    }

    @Operation(summary = "Create a new report", description = "Create a new report (PROFESSOR only)")
    @PostMapping
    public ReportBasicDTO createReport(
            Principal principal,
            @Valid @RequestBody CreateReportRequest request) {
        
        // Validate name
        if (request.name == null || request.name.trim().isEmpty()) {
            throw new ControllerException("Report name cannot be empty");
        }
        
        if (request.name.length() > Report.NAME_LENGTH) {
            throw new ControllerException("Report name cannot exceed " + Report.NAME_LENGTH + " characters");
        }
        
        String userId = super.getUserId(principal);
        Report report = reportService.createReport(request.name, userId);
        return reportMapper.toBasicDTO(report);
    }

    @Operation(summary = "Update a report", description = "Update report information (PROFESSOR only)")
    @PatchMapping(path = "/{id}")
    public ReportBasicDTO updateReport(
            Principal principal,
            @PathVariable(name = "id") Long id,
            @Valid @RequestBody MergePatchReport reportRequest) {
        
        // Validate name if present
        if (reportRequest.name != null && reportRequest.name.isPresent()) {
            String name = reportRequest.name.get();
            if (name != null && (name.trim().isEmpty() || name.length() > Report.NAME_LENGTH)) {
                throw new ControllerException("Report name must be between 1 and " + Report.NAME_LENGTH + " characters");
            }
        }
        
        // Validate that rowType and columnType are different (at the request level)
        if (reportRequest.rowType != null && reportRequest.rowType.isPresent() &&
            reportRequest.columnType != null && reportRequest.columnType.isPresent()) {
            if (reportRequest.rowType.get() != null && 
                reportRequest.rowType.get().equals(reportRequest.columnType.get())) {
                throw new ControllerException("Row type and column type must be different");
            }
        }
        
        String userId = super.getUserId(principal);
        Report report = reportService.updateReport(id, reportRequest, userId);
        return reportMapper.toBasicDTO(report);
    }

    @Operation(summary = "Delete a report", description = "Delete a report (PROFESSOR only)")
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteReport(Principal principal, @PathVariable(name = "id") Long id) {
        String userId = super.getUserId(principal);
        reportService.deleteReport(id, userId);
        return ResponseEntity.noContent().build();
    }

    // Request DTO
    public static class CreateReportRequest {
        @NotBlank(message = "Name is required")
        @Size(min = Report.MIN_NAME_LENGTH, max = Report.NAME_LENGTH, 
              message = "Name must be between " + Report.MIN_NAME_LENGTH + " and " + Report.NAME_LENGTH + " characters")
        public String name;
    }
}
