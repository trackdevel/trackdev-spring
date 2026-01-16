package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.configuration.UserType;
import org.trackdev.api.controller.exceptions.EntityNotFound;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.Report;
import org.trackdev.api.entity.User;
import org.trackdev.api.model.MergePatchReport;
import org.trackdev.api.repository.ReportRepository;
import org.trackdev.api.utils.ErrorConstants;

import java.util.List;

@Service
public class ReportService extends BaseServiceLong<Report, ReportRepository> {

    @Autowired
    UserService userService;

    @Autowired
    AccessChecker accessChecker;

    @Transactional
    public Report createReport(String name, String userId) {
        User user = userService.get(userId);
        
        // Only PROFESSORS can create reports
        if (!user.isUserType(UserType.PROFESSOR)) {
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
        
        if (name == null || name.trim().isEmpty()) {
            throw new ServiceException("Report name cannot be empty");
        }
        
        if (name.length() > Report.NAME_LENGTH) {
            throw new ServiceException("Report name cannot exceed " + Report.NAME_LENGTH + " characters");
        }
        
        Report report = new Report(name.trim(), user);
        repo.save(report);
        
        return report;
    }

    public List<Report> getReportsForUser(String userId) {
        User user = userService.get(userId);
        
        // Only PROFESSORS can view reports
        if (!user.isUserType(UserType.PROFESSOR)) {
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
        
        // For now, return all reports - in the future, you might want to filter by workspace
        return repo.findAll();
    }

    public Report getReport(Long reportId, String userId) {
        User user = userService.get(userId);
        
        // Only PROFESSORS can view reports
        if (!user.isUserType(UserType.PROFESSOR)) {
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
        
        Report report = repo.findById(reportId)
            .orElseThrow(() -> new EntityNotFound("Report", reportId));
        
        return report;
    }

    @Transactional
    public Report updateReport(Long reportId, MergePatchReport reportRequest, String userId) {
        User user = userService.get(userId);
        
        // Only PROFESSORS can update reports
        if (!user.isUserType(UserType.PROFESSOR)) {
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
        
        Report report = repo.findById(reportId)
            .orElseThrow(() -> new EntityNotFound("Report", reportId));
        
        // Update name if present
        if (reportRequest.name != null && reportRequest.name.isPresent()) {
            String name = reportRequest.name.get();
            if (name != null) {
                if (name.trim().isEmpty()) {
                    throw new ServiceException("Report name cannot be empty");
                }
                if (name.length() > Report.NAME_LENGTH) {
                    throw new ServiceException("Report name cannot exceed " + Report.NAME_LENGTH + " characters");
                }
                report.setName(name.trim());
            }
        }
        
        // Update rowType if present
        if (reportRequest.rowType != null) {
            report.setRowType(reportRequest.rowType.orElse(null));
        }
        
        // Update columnType if present
        if (reportRequest.columnType != null) {
            report.setColumnType(reportRequest.columnType.orElse(null));
        }
        
        // Update element if present
        if (reportRequest.element != null) {
            report.setElement(reportRequest.element.orElse(null));
        }
        
        // Update magnitude if present
        if (reportRequest.magnitude != null) {
            report.setMagnitude(reportRequest.magnitude.orElse(null));
        }
        
        // Validate that final rowType and columnType are different (if both are set)
        if (report.getRowType() != null && report.getColumnType() != null &&
            report.getRowType().equals(report.getColumnType())) {
            throw new ServiceException("Row type and column type must be different");
        }
        
        repo.save(report);
        return report;
    }

    @Transactional
    public void deleteReport(Long reportId, String userId) {
        User user = userService.get(userId);
        
        // Only PROFESSORS can delete reports
        if (!user.isUserType(UserType.PROFESSOR)) {
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
        
        Report report = repo.findById(reportId)
            .orElseThrow(() -> new EntityNotFound("Report", reportId));
        
        repo.delete(report);
    }
}
