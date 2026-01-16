package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.configuration.UserType;
import org.trackdev.api.controller.exceptions.EntityNotFound;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.dto.ReportResultDTO;
import org.trackdev.api.entity.*;
import org.trackdev.api.model.MergePatchReport;
import org.trackdev.api.repository.CourseRepository;
import org.trackdev.api.repository.GroupRepository;
import org.trackdev.api.repository.ReportRepository;
import org.trackdev.api.utils.ErrorConstants;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService extends BaseServiceLong<Report, ReportRepository> {

    @Autowired
    UserService userService;

    @Autowired
    AccessChecker accessChecker;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    GroupRepository projectRepository;

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

        // Update course if present (courseId)
        if (reportRequest.courseId != null) {
            Long courseId = reportRequest.courseId.orElse(null);
            if (courseId != null) {
                Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new EntityNotFound("Course", courseId));
                // Verify the professor owns or has access to this course
                if (!course.getOwnerId().equals(userId)) {
                    throw new ServiceException(ErrorConstants.UNAUTHORIZED);
                }
                report.setCourse(course);
            } else {
                report.setCourse(null);
            }
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

    /**
     * Get reports assigned to a specific course.
     * Accessible by professors (course owner) and students enrolled in course projects.
     */
    public List<Report> getReportsForCourse(Long courseId, String userId) {
        User user = userService.get(userId);
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new EntityNotFound("Course", courseId));
        
        // Check if user is professor (owner) or student enrolled in this course
        boolean isProfessor = user.isUserType(UserType.PROFESSOR) && course.getOwnerId().equals(userId);
        boolean isStudent = user.isUserType(UserType.STUDENT) && course.isStudentEnrolled(userId);
        
        if (!isProfessor && !isStudent) {
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
        
        return repo.findByCourseId(courseId);
    }

    /**
     * Get reports available for a project (reports assigned to its course).
     * Accessible by project members and course owner.
     */
    public List<Report> getReportsForProject(Long projectId, String userId) {
        User user = userService.get(userId);
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new EntityNotFound("Project", projectId));
        
        // Check if user is a member of the project or course owner
        boolean isMember = project.isMember(userId);
        boolean isCourseOwner = project.getCourse() != null && 
            project.getCourse().getOwnerId().equals(userId);
        
        if (!isMember && !isCourseOwner) {
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
        
        if (project.getCourse() == null) {
            throw new ServiceException("Project is not associated with a course");
        }
        
        // Return all reports assigned to the project's course
        return repo.findByCourseId(project.getCourse().getId());
    }

    /**
     * Compute the report results for a specific project.
     * Generates a grid with rows x columns and sums the magnitude for each cell.
     */
    @Transactional(readOnly = true)
    public ReportResultDTO computeReportForProject(Long reportId, Long projectId, String userId) {
        User user = userService.get(userId);
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new EntityNotFound("Project", projectId));
        Report report = repo.findById(reportId)
            .orElseThrow(() -> new EntityNotFound("Report", reportId));
        
        // Check access: user must be project member or course owner
        boolean isMember = project.isMember(userId);
        boolean isCourseOwner = project.getCourse() != null && 
            project.getCourse().getOwnerId().equals(userId);
        
        if (!isMember && !isCourseOwner) {
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
        
        // Verify report is assigned to the project's course
        if (report.getCourse() == null || 
            !report.getCourse().getId().equals(project.getCourse().getId())) {
            throw new ServiceException("Report is not assigned to this project's course");
        }
        
        // Verify report configuration is complete
        if (report.getRowType() == null || report.getColumnType() == null ||
            report.getElement() == null || report.getMagnitude() == null) {
            throw new ServiceException("Report configuration is incomplete");
        }
        
        // Build result DTO
        ReportResultDTO result = new ReportResultDTO();
        result.setReportId(report.getId());
        result.setReportName(report.getName());
        result.setProjectId(project.getId());
        result.setProjectName(project.getName());
        result.setRowType(report.getRowType().name());
        result.setColumnType(report.getColumnType().name());
        result.setElement(report.getElement().name());
        result.setMagnitude(report.getMagnitude().name());
        
        // Get project tasks
        Collection<Task> allTasks = project.getTasks();
        if (allTasks == null) {
            allTasks = Collections.emptyList();
        }
        
        // Get row and column entities
        List<ReportResultDTO.AxisHeader> rowHeaders = getAxisHeaders(report.getRowType(), project);
        List<ReportResultDTO.AxisHeader> columnHeaders = getAxisHeaders(report.getColumnType(), project);
        
        result.setRowHeaders(rowHeaders);
        result.setColumnHeaders(columnHeaders);
        
        // Compute grid data
        Map<String, Integer> data = new HashMap<>();
        Map<String, Integer> rowTotals = new HashMap<>();
        Map<String, Integer> columnTotals = new HashMap<>();
        int grandTotal = 0;
        
        // Initialize totals
        for (ReportResultDTO.AxisHeader rowHeader : rowHeaders) {
            rowTotals.put(rowHeader.getId(), 0);
        }
        for (ReportResultDTO.AxisHeader colHeader : columnHeaders) {
            columnTotals.put(colHeader.getId(), 0);
        }
        
        // Process each task
        for (Task task : allTasks) {
            // Get value based on magnitude
            int value = getMagnitudeValue(task, report.getMagnitude());
            if (value == 0) continue;
            
            // Get row identifiers for this task
            List<String> rowIds = getTaskAxisIds(task, report.getRowType());
            // Get column identifiers for this task
            List<String> colIds = getTaskAxisIds(task, report.getColumnType());
            
            // Add value to each cell combination
            for (String rowId : rowIds) {
                for (String colId : colIds) {
                    String key = rowId + ":" + colId;
                    data.merge(key, value, Integer::sum);
                    rowTotals.merge(rowId, value, Integer::sum);
                    columnTotals.merge(colId, value, Integer::sum);
                    grandTotal += value;
                }
            }
        }
        
        result.setData(data);
        result.setRowTotals(rowTotals);
        result.setColumnTotals(columnTotals);
        result.setGrandTotal(grandTotal);
        
        return result;
    }
    
    /**
     * Get axis headers (row or column) based on the axis type.
     */
    private List<ReportResultDTO.AxisHeader> getAxisHeaders(ReportAxisType axisType, Project project) {
        List<ReportResultDTO.AxisHeader> headers = new ArrayList<>();
        
        switch (axisType) {
            case STUDENTS:
                // Get all project members
                Collection<User> members = project.getMembers();
                if (members != null) {
                    for (User member : members) {
                        headers.add(new ReportResultDTO.AxisHeader(member.getId(), member.getUsername()));
                    }
                }
                break;
                
            case SPRINTS:
                // Get all project sprints
                Collection<Sprint> sprints = project.getSprints();
                if (sprints != null) {
                    for (Sprint sprint : sprints) {
                        headers.add(new ReportResultDTO.AxisHeader(sprint.getId().toString(), sprint.getName()));
                    }
                }
                break;
        }
        
        return headers;
    }
    
    /**
     * Get the magnitude value from a task.
     */
    private int getMagnitudeValue(Task task, ReportMagnitude magnitude) {
        switch (magnitude) {
            case ESTIMATION_POINTS:
                Integer points = task.getEstimationPoints();
                return points != null ? points : 0;
                
            case PULL_REQUESTS:
                // Count number of pull requests
                return task.hasPullRequest() ? task.getPullRequests().size() : 0;
                
            default:
                return 0;
        }
    }
    
    /**
     * Get the axis identifiers for a task (which rows/columns it belongs to).
     */
    private List<String> getTaskAxisIds(Task task, ReportAxisType axisType) {
        List<String> ids = new ArrayList<>();
        
        switch (axisType) {
            case STUDENTS:
                // A task belongs to its assignee
                User assignee = task.getAssignee();
                if (assignee != null) {
                    ids.add(assignee.getId());
                }
                break;
                
            case SPRINTS:
                // A task can belong to multiple sprints
                Collection<Sprint> sprints = task.getActiveSprints();
                if (sprints != null) {
                    for (Sprint sprint : sprints) {
                        ids.add(sprint.getId().toString());
                    }
                }
                break;
        }
        
        return ids;
    }
}
