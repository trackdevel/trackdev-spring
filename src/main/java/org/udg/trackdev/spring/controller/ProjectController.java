package org.udg.trackdev.spring.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.controller.exceptions.ControllerException;
import org.udg.trackdev.spring.entity.Project;
import org.udg.trackdev.spring.entity.Sprint;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.service.AccessChecker;
import org.udg.trackdev.spring.service.ProjectService;
import org.udg.trackdev.spring.service.UserService;
import org.udg.trackdev.spring.utils.ErrorConstants;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Size;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

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

    @Operation(summary = "Get all projects", description = "Get all projects")
    @GetMapping
    @JsonView(EntityLevelViews.ProjectWithUser.class)
    public Collection<Project> getProjects(Principal principal) {
        String userId = super.getUserId(principal);
        if(accessChecker.checkCanViewAllProjects(userId)){
            return service.findAll();
        }
        else{
            return userService.get(userId).getProjects();
        }
    }

    @Operation(summary = "Get specific project", description = "Get specific project")
    @GetMapping(path = "/{projectId}")
    @JsonView(EntityLevelViews.ProjectComplete.class)
    public Project getProject(Principal principal, @PathVariable(name = "projectId") Long projectId) {
        String userId = super.getUserId(principal);
        Project project = service.get(projectId);
        accessChecker.checkCanViewProject(project, userId);
        userService.setCurrentProject(userService.get(userId), project);
        return project;
    }

    @Operation(summary = "Edit specific project", description = "Edit specific project")
    @PatchMapping(path = "/{projectId}")
    @JsonView(EntityLevelViews.ProjectWithUser.class)
    public Project editProject(Principal principal,
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
        return service.editProject(projectId, projectRequest.name, projectRequest.members, projectRequest.courseId, projectRequest.qualification, userId);
    }

    @Operation(summary = "Delete specific project", description = "Delete specific project")
    @DeleteMapping(path = "/{projectId}")
    @JsonView(EntityLevelViews.Basic.class)
    public ResponseEntity<Void> deleteProject(Principal principal,
                                        @PathVariable(name = "projectId") Long projectId) {
        String userId = super.getUserId(principal);
        service.deleteProject(projectId, userId);
        return okNoContent();
    }

    @Operation(summary = "Get tasks of specific project", description = "Get tasks of specific project")
    @GetMapping(path = "/{projectId}/tasks")
    @JsonView(EntityLevelViews.Basic.class)
    public Map<String, Object> getProjectTasks(Principal principal,
                                            @PathVariable(name = "projectId") Long projectId) {
        Map<String, Object> response = new HashMap<>();
        String userId = super.getUserId(principal);
        Project project = service.get(projectId);
        accessChecker.checkCanViewProject(project, userId);
        Collection<Task> tasks = service.getProjectTasks(project);
        response.put("tasks", tasks);
        response.put("projectId", projectId);
        return response;
    }

    @Operation(summary = "Create sprint of specific project", description = "Create sprint of specific project")
    @PostMapping(path = "/{projectId}/sprints")
    @JsonView(EntityLevelViews.Basic.class)
    public ResponseEntity<Void> createProjectSprint(Principal principal,
                                       @PathVariable(name = "projectId") Long projectId,
                                       @Valid @RequestBody CreateSprint sprintRequest,
                                                    BindingResult result) {
        if (result.hasErrors()) {
            throw new ControllerException(ErrorConstants.INVALID_SPRINT_NAME_LENGTH);
        }
        String userId = super.getUserId(principal);
        Project project = service.get(projectId);
        accessChecker.checkCanViewProject(project, userId);
        service.createSprint(project, sprintRequest.name, sprintRequest.startDate, sprintRequest.endDate, userId);
        return okNoContent();
    }

    @Operation(summary = "Create US of specific project", description = "Create US of specific project")
    @PostMapping(path = "/{projectId}/tasks")
    @JsonView(EntityLevelViews.Basic.class)
    public Project createTask(Principal principal,
                              @PathVariable(name = "projectId") Long projectId,
                              @Valid @RequestBody  NewTask task) {
        String userId = super.getUserId(principal);
        User user = userService.get(userId);
        Project project = service.get(projectId);
        accessChecker.checkCanViewProject(project, userId);
        return service.createProjectTask(project,task.name, user);

    }

    @Operation(summary = "Get all project sprints of specific project", description = "Get all project sprints of specific project")
    @GetMapping(path = "/{projectId}/sprints")
    public ResponseEntity<List<Map<String, String>>> getProjectSprints(Principal principal,
                                            @PathVariable(name = "projectId") Long projectId) {
        String userId = super.getUserId(principal);
        Project project = service.get(projectId);
        accessChecker.checkCanViewProject(project, userId);
        List<Map<String, String>> customResponse = buildCustomResponse(service.getProjectSprints(project));
        return ResponseEntity.ok().body(customResponse);
    }

    @Operation(summary = "Get users qualification of specific project", description = "Get users qualification of specific project")
    @GetMapping(path = "/{projectId}/qualification")
    public ResponseEntity<Map<String, Map<String,String>>> getProjectRank(Principal principal,
                                            @PathVariable(name = "projectId") Long projectId) {
        User user = userService.get(super.getUserId(principal));
        Project project = service.get(projectId);
        accessChecker.isUserAdmin(user);
        return ResponseEntity.ok().body(service.getProjectRanks(project));
    }

    private List<Map<String, String>> buildCustomResponse(Collection<Sprint> sprints) {
        List<Map<String, String>> customResponse = new ArrayList<>();
        for (Sprint sprint : sprints) {
            Map<String, String> sprintMap = new HashMap<>();
            sprintMap.put("value", sprint.getName());
            sprintMap.put("label", sprint.getName());
            sprintMap.put("id", sprint.getId().toString());
            sprintMap.put("startDate", sprint.getStartDate().toString());
            sprintMap.put("endDate", sprint.getEndDate().toString());
            sprintMap.put("status", sprint.getStatusText());
            customResponse.add(sprintMap);
        }
        return customResponse;
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
        @Max(value = Project.MIN_QUALIFICATION, message = ErrorConstants.INVALID_PRJ_QUALIFICATION)
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