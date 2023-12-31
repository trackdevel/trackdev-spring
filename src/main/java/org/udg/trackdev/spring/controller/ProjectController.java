package org.udg.trackdev.spring.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.entity.Project;
import org.udg.trackdev.spring.entity.Sprint;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.service.AccessChecker;
import org.udg.trackdev.spring.service.ProjectService;
import org.udg.trackdev.spring.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.security.Principal;
import java.util.*;

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

    @GetMapping(path = "/{projectId}")
    @JsonView(EntityLevelViews.ProjectComplete.class)
    public Project getProject(Principal principal, @PathVariable(name = "projectId") Long projectId) {
        String userId = super.getUserId(principal);
        Project project = service.get(projectId);
        accessChecker.checkCanViewProject(project, userId);
        userService.setCurrentProject(userService.get(userId), project);
        return project;
    }

    @PatchMapping(path = "/{projectId}")
    @JsonView(EntityLevelViews.ProjectWithUser.class)
    public Project editProject(Principal principal,
                               @PathVariable(name = "projectId") Long projectId,
                               @Valid @RequestBody EditProject projectRequest) {
        String userId = super.getUserId(principal);
        return service.editProject(projectId, projectRequest.name, projectRequest.members, projectRequest.courseId, userId);
    }

    @DeleteMapping(path = "/{projectId}")
    @JsonView(EntityLevelViews.Basic.class)
    public ResponseEntity<Void> deleteProject(Principal principal,
                                        @PathVariable(name = "projectId") Long projectId) {
        String userId = super.getUserId(principal);
        service.deleteProject(projectId, userId);
        return okNoContent();
    }

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

    @PostMapping(path = "/{projectId}/sprints")
    @JsonView(EntityLevelViews.Basic.class)
    public ResponseEntity<Void> createProjectSprint(Principal principal,
                                       @PathVariable(name = "projectId") Long projectId,
                                       @Valid @RequestBody CreateSprint sprintRequest) {
        String userId = super.getUserId(principal);
        Project project = service.get(projectId);
        accessChecker.checkCanViewProject(project, userId);
        service.createSprint(project, sprintRequest.name, sprintRequest.startDate, sprintRequest.endDate, userId);
        return okNoContent();
    }

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

    @GetMapping(path = "/{projectId}/sprints")
    public ResponseEntity<List<Map<String, String>>> getProjectSprints(Principal principal,
                                            @PathVariable(name = "projectId") Long projectId) {
        String userId = super.getUserId(principal);
        Project project = service.get(projectId);
        accessChecker.checkCanViewProject(project, userId);
        List<Map<String, String>> customResponse = buildCustomResponse(service.getProjectSprints(project));
        return ResponseEntity.ok().body(customResponse);
    }

    private List<Map<String, String>> buildCustomResponse(Collection<Sprint> sprints) {
        List<Map<String, String>> customResponse = new ArrayList<>();
        for (Sprint sprint : sprints) {
            Map<String, String> sprintMap = new HashMap<>();
            sprintMap.put("value", sprint.getName());
            sprintMap.put("label", sprint.getName());
            customResponse.add(sprintMap);
        }
        return customResponse;
    }

    static class EditProject {
        @Size(min = 1, max = Project.NAME_LENGTH)
        public String name;
        public Collection<String> members;
        public Long courseId;
    }

    static class CreateSprint {
        @Size(min = 1, max = Sprint.NAME_LENGTH)
        public String name;
        public Date startDate;
        public Date endDate;
    }

    static class NewTask{
        public String name;
    }
}