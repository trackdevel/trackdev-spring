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
import org.trackdev.api.entity.Sprint;
import org.trackdev.api.entity.Task;
import org.trackdev.api.mapper.ProjectMapper;
import org.trackdev.api.mapper.TaskMapper;
import org.trackdev.api.model.response.ProjectQualificationResponse;
import org.trackdev.api.model.response.ProjectSprintsResponse;
import org.trackdev.api.service.AccessChecker;
import org.trackdev.api.service.ProjectService;
import org.trackdev.api.service.UserService;
import org.trackdev.api.utils.ErrorConstants;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.security.Principal;
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
    public ProjectCompleteDTO getProject(Principal principal, @PathVariable Long projectId) {
        String userId = super.getUserId(principal);
        // All operations in a single transaction
        return projectMapper.toCompleteDTO(service.getProjectAndSetCurrent(projectId, userId));
    }

    @Operation(summary = "Edit specific project", description = "Edit specific project")
    @PatchMapping(path = "/{projectId}")
    public ProjectWithMembersDTO editProject(Principal principal,
                               @PathVariable Long projectId,
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
                                        @PathVariable Long projectId) {
        String userId = super.getUserId(principal);
        service.deleteProject(projectId, userId);
        return okNoContent();
    }

    @Operation(summary = "Get tasks of specific project", description = "Get tasks of specific project")
    @GetMapping(path = "/{projectId}/tasks")
    public ProjectTasksResponseDTO getProjectTasks(Principal principal,
                                            @PathVariable Long projectId) {
        String userId = super.getUserId(principal);
        // Authorization check is now inside the service method
        Collection<Task> tasks = service.getProjectTasks(projectId, userId);
        return new ProjectTasksResponseDTO(taskMapper.toBasicDTOCollection(tasks), projectId);
    }

    @Operation(summary = "Create sprint of specific project", description = "Create sprint of specific project")
    @PostMapping(path = "/{projectId}/sprints")
    public IdResponseDTO createProjectSprint(Principal principal,
                                       @PathVariable Long projectId,
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

    @Operation(summary = "Create US of specific project", description = "Create US of specific project")
    @PostMapping(path = "/{projectId}/tasks")
    public IdResponseDTO createTask(Principal principal,
                              @PathVariable Long projectId,
                              @Valid @RequestBody  NewTask task) {
        String userId = super.getUserId(principal);
        // All operations in a single transaction
        Task createdTask = service.createProjectTask(projectId, task.name, userId);
        return new IdResponseDTO(createdTask.getId());
    }

    @Operation(summary = "Get all project sprints of specific project", description = "Get all project sprints of specific project")
    @GetMapping(path = "/{projectId}/sprints")
    public ProjectSprintsResponse getProjectSprints(Principal principal,
                                            @PathVariable Long projectId) {
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
                                            @PathVariable Long projectId) {
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

    static class EditProject {
        @Size(
                min = Project.MIN_NAME_LENGTH,
                max = Project.NAME_LENGTH,
                message = ErrorConstants.INVALID_PRJ_NAME_LENGTH
        )
        public String name;
        public Collection<String> members;
        public Long courseId;
        @Min(value = Project.MIN_QUALIFICATION, message = ErrorConstants.INVALID_PRJ_QUALIFICATION)
        @Max(value = Project.MAX_QUALIFICATION, message = ErrorConstants.INVALID_PRJ_QUALIFICATION)
        public Double qualification;
    }

    static class CreateSprint {
        @Size(min = Sprint.MIN_NAME_LENGTH, max = Sprint.NAME_LENGTH)
        public String name;
        public Date startDate;
        public Date endDate;
    }

    static class NewTask{
        public String name;
    }
}