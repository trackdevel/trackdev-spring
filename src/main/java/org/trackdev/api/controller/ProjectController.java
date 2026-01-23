package org.trackdev.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.trackdev.api.controller.exceptions.ControllerException;
import org.trackdev.api.dto.*;
import org.trackdev.api.entity.Project;
import org.trackdev.api.entity.Report;
import org.trackdev.api.entity.Sprint;
import org.trackdev.api.entity.Task;
import org.trackdev.api.entity.TaskStatus;
import org.trackdev.api.entity.TaskType;
import org.trackdev.api.mapper.ProjectMapper;
import org.trackdev.api.mapper.ReportMapper;
import org.trackdev.api.mapper.TaskMapper;
import org.trackdev.api.model.response.ProjectQualificationResponse;
import org.trackdev.api.model.response.ProjectSprintsResponse;
import org.trackdev.api.service.AccessChecker;
import org.trackdev.api.service.ProjectService;
import org.trackdev.api.service.ReportService;
import org.trackdev.api.service.UserService;
import org.trackdev.api.utils.ErrorConstants;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.security.Principal;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for project management.
 * Projects belong to courses and contain tasks and sprints.
 */
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "5. Projects")
@RestController
@RequestMapping("/projects")
public class ProjectController extends BaseController {
    @Autowired
    ProjectService service;

    @Autowired
    UserService userService;

    @Autowired
    AccessChecker accessChecker;

    @Autowired
    ProjectMapper projectMapper;

    @Autowired
    TaskMapper taskMapper;

    @Autowired
    ReportMapper reportMapper;

    @Autowired
    ReportService reportService;

    @Operation(summary = "Get all projects", description = "Get all projects")
    @GetMapping
    public ProjectsResponseDTO getProjects(Principal principal) {
        String userId = super.getUserId(principal);
        if(accessChecker.checkCanViewAllProjects(userId)){
            return new ProjectsResponseDTO(projectMapper.toWithMembersDTOList(service.findAll()));
        }
        else{
            return new ProjectsResponseDTO(projectMapper.toWithMembersDTOList(service.getProjectsForUser(userId)));
        }
    }

    @Operation(summary = "Get specific project", description = "Get specific project")
    @GetMapping(path = "/{projectId}")
    public ProjectCompleteDTO getProject(Principal principal, @PathVariable(name = "projectId") Long projectId) {
        String userId = super.getUserId(principal);
        // All operations in a single transaction
        return projectMapper.toCompleteDTO(service.getProjectAndSetCurrent(projectId, userId));
    }

    @Operation(summary = "Edit specific project", description = "Edit specific project")
    @PatchMapping(path = "/{projectId}")
    public ProjectWithMembersDTO editProject(Principal principal,
                               @PathVariable(name = "projectId") Long projectId,
                               @Valid @RequestBody EditProject projectRequest,
                               BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            throw new ControllerException(String.join(". ", errors));
        }
        String userId = super.getUserId(principal);
        return projectMapper.toWithMembersDTO(service.editProject(projectId, projectRequest.name, projectRequest.members, projectRequest.courseId, projectRequest.qualification, userId));
    }

    @Operation(summary = "Delete specific project", description = "Delete specific project")
    @DeleteMapping(path = "/{projectId}")
    public ResponseEntity<Void> deleteProject(Principal principal,
                                        @PathVariable(name = "projectId") Long projectId) {
        String userId = super.getUserId(principal);
        service.deleteProject(projectId, userId);
        return okNoContent();
    }

    @Operation(summary = "Get all tasks of specific project", description = "Get all tasks of specific project including subtasks")
    @GetMapping(path = "/{projectId}/tasks")
    public ProjectTasksResponseDTO getProjectTasks(Principal principal,
                                            @PathVariable(name = "projectId") Long projectId) {
        String userId = super.getUserId(principal);
        // Authorization check is now inside the service method
        Collection<Task> tasks = service.getAllProjectTasks(projectId, userId);
        return new ProjectTasksResponseDTO(taskMapper.toBasicDTOCollection(tasks), projectId);
    }

    @Operation(summary = "Create sprint of specific project", description = "Create sprint of specific project")
    @PostMapping(path = "/{projectId}/sprints")
    public IdResponseDTO createProjectSprint(Principal principal,
                                       @PathVariable(name = "projectId") Long projectId,
                                       @Valid @RequestBody CreateSprint sprintRequest,
                                                    BindingResult result) {
        if (result.hasErrors()) {
            throw new ControllerException(ErrorConstants.INVALID_SPRINT_NAME_LENGTH);
        }
        String userId = super.getUserId(principal);
        // All operations in a single transaction
        Sprint createdSprint = service.createSprint(projectId, sprintRequest.name, sprintRequest.startDate, sprintRequest.endDate, userId);
        return new IdResponseDTO(createdSprint.getId());
    }

    @Operation(summary = "Create task in specific project", description = "Create a task in the project backlog")
    @PostMapping(path = "/{projectId}/tasks")
    public IdResponseDTO createTask(Principal principal,
                              @PathVariable(name = "projectId") Long projectId,
                              @Valid @RequestBody  NewTask task) {
        String userId = super.getUserId(principal);
        // All operations in a single transaction
        Task createdTask = service.createProjectTask(projectId, task.name, task.description, task.type, task.assigneeId, userId);
        return new IdResponseDTO(createdTask.getId());
    }

    @Operation(summary = "Get all project sprints of specific project", description = "Get all project sprints of specific project")
    @GetMapping(path = "/{projectId}/sprints")
    public ProjectSprintsResponse getProjectSprints(Principal principal,
                                            @PathVariable(name = "projectId") Long projectId) {
        String userId = super.getUserId(principal);
        // Authorization check is now inside the service method
        Collection<Sprint> sprints = service.getProjectSprints(projectId, userId);
        List<ProjectSprintsResponse.SprintSummary> sprintSummaries = buildSprintSummaries(sprints);
        return new ProjectSprintsResponse(sprintSummaries, projectId);
    }

    @Operation(summary = "Get users qualification of specific project", description = "Get users qualification of specific project - admin only")
    @GetMapping(path = "/{projectId}/qualification")
    @PreAuthorize("hasRole('ADMIN')")
    public ProjectQualificationResponse getProjectRank(Principal principal,
                                            @PathVariable(name = "projectId") Long projectId) {
        String userId = super.getUserId(principal);
        // All operations in a single transaction - admin check via @PreAuthorize
        Map<String, Map<String, String>> ranks = service.getProjectRanksForAdmin(projectId, userId);
        Map<String, ProjectQualificationResponse.UserQualification> qualifications = new HashMap<>();
        for (Map.Entry<String, Map<String, String>> entry : ranks.entrySet()) {
            qualifications.put(entry.getKey(), new ProjectQualificationResponse.UserQualification(entry.getValue()));
        }
        return new ProjectQualificationResponse(projectId, qualifications);
    }

    private List<ProjectSprintsResponse.SprintSummary> buildSprintSummaries(Collection<Sprint> sprints) {
        List<ProjectSprintsResponse.SprintSummary> summaries = new ArrayList<>();
        for (Sprint sprint : sprints) {
            summaries.add(new ProjectSprintsResponse.SprintSummary(
                sprint.getId(),
                sprint.getName(),
                sprint.getStartDate().toString(),
                sprint.getEndDate().toString(),
                sprint.getStatusText()
            ));
        }
        return summaries;
    }

    // ============== Project Reports Endpoints ==============

    @Operation(summary = "Get available reports for a project", description = "Get reports assigned to the project's course")
    @GetMapping(path = "/{projectId}/reports")
    public List<ReportBasicDTO> getProjectReports(Principal principal,
                                                   @PathVariable(name = "projectId") Long projectId) {
        String userId = super.getUserId(principal);
        List<Report> reports = reportService.getReportsForProject(projectId, userId);
        return reportMapper.toBasicDTOList(reports);
    }

    @Operation(summary = "Compute a report for a project", description = "Compute the report results for a specific project. Optionally filter by task status (comma-separated).")
    @GetMapping(path = "/{projectId}/reports/{reportId}/compute")
    public ReportResultDTO computeReport(Principal principal,
                                         @PathVariable(name = "projectId") Long projectId,
                                         @PathVariable(name = "reportId") Long reportId,
                                         @RequestParam(name = "status", required = false) String statusParam) {
        String userId = super.getUserId(principal);
        
        // Parse comma-separated status values
        List<TaskStatus> statusFilters = null;
        if (statusParam != null && !statusParam.trim().isEmpty()) {
            statusFilters = Arrays.stream(statusParam.split(","))
                    .map(String::trim)
                    .map(TaskStatus::valueOf)
                    .collect(Collectors.toList());
        }
        
        return reportService.computeReportForProject(reportId, projectId, userId, statusFilters);
    }
    @Operation(summary = "Apply a sprint pattern to a project", description = "Apply a sprint pattern to a project, creating sprints from the pattern items (professors only)")
    @PostMapping(path = "/{projectId}/apply-pattern/{patternId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ProjectCompleteDTO applySprintPattern(
            Principal principal,
            @PathVariable(name = "projectId") Long projectId,
            @PathVariable(name = "patternId") Long patternId) {
        String userId = super.getUserId(principal);
        Project project = service.applySprintPattern(projectId, patternId, userId);
        return projectMapper.toCompleteDTO(project);
    }

    static class EditProject {
        @Size(
                min = Project.MIN_NAME_LENGTH,
                max = Project.NAME_LENGTH,
                message = ErrorConstants.INVALID_PRJ_NAME_LENGTH
        )
        public String name;
        /** User IDs of project members */
        public Collection<String> members;
        public Long courseId;
        @Min(value = Project.MIN_QUALIFICATION, message = ErrorConstants.INVALID_PRJ_QUALIFICATION)
        @Max(value = Project.MAX_QUALIFICATION, message = ErrorConstants.INVALID_PRJ_QUALIFICATION)
        public Double qualification;
    }

    static class CreateSprint {
        @Size(min = Sprint.MIN_NAME_LENGTH, max = Sprint.NAME_LENGTH)
        public String name;
        public ZonedDateTime startDate;
        public ZonedDateTime endDate;
    }

    static class NewTask{
        @Size(min = Task.MIN_NAME_LENGTH, max = Task.NAME_LENGTH)
        public String name;
        public String description;
        public TaskType type;
        public String assigneeId;
    }
}