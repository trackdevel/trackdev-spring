package org.udg.trackdev.spring.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.entity.Project;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.service.AccessChecker;
import org.udg.trackdev.spring.service.ProjectService;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.security.Principal;
import java.util.Collection;
import java.util.Optional;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "5. Projects")
@RestController
@RequestMapping("/projects")
public class ProjectController extends BaseController {
    @Autowired
    ProjectService service;

    @Autowired
    AccessChecker accessChecker;

    @GetMapping
    @JsonView(EntityLevelViews.Basic.class)
    public Collection<Project> getProjects(Principal principal) {
        String userId = super.getUserId(principal);
        accessChecker.checkCanViewAllProjects(userId);
        return service.findAll();
    }

    @GetMapping(path = "/{projectId}")
    @JsonView(EntityLevelViews.ProjectComplete.class)
    public Project getProject(Principal principal, @PathVariable(name = "projectId") Long projectId) {
        String userId = super.getUserId(principal);
        Project project = service.get(projectId);
        accessChecker.checkCanViewProject(project, userId);
        return project;
    }

    @PatchMapping(path = "/{projectId}")
    @JsonView(EntityLevelViews.Basic.class)
    public Project editProject(Principal principal,
                               @PathVariable(name = "projectId") Long projectId,
                               @Valid @RequestBody EditProject projectRequest) {
        String userId = super.getUserId(principal);
        Project modifiedProject = service.editProject(projectId, projectRequest.name, projectRequest.members,  projectRequest.current, userId);
        return modifiedProject;
    }

    @DeleteMapping(path = "/{projectId}")
    @JsonView(EntityLevelViews.Basic.class)
    public ResponseEntity deleteProject(Principal principal,
                                        @PathVariable(name = "projectId") Long projectId) {
        String userId = super.getUserId(principal);
        service.deleteProject(projectId, userId);
        return okNoContent();
    }

    //TODO: Modificar a Optionals
    static class EditProject {
        @Size(min = 1, max = Project.NAME_LENGTH)
        public String name;
        public Optional<Boolean> current;
        public Collection<String> members;
    }
}