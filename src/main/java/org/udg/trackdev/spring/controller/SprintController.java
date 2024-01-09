package org.udg.trackdev.spring.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.entity.Sprint;
import org.udg.trackdev.spring.entity.sprintchanges.SprintChange;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.model.MergePatchSprint;
import org.udg.trackdev.spring.service.AccessChecker;
import org.udg.trackdev.spring.service.SprintChangeService;
import org.udg.trackdev.spring.service.SprintService;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "7. Sprints")
@RestController
@RequestMapping(path = "/sprints")
public class SprintController extends CrudController<Sprint, SprintService> {
    @Autowired
    AccessChecker accessChecker;

    @Autowired
    SprintChangeService sprintChangeService;

    @Operation(summary = "Get all sprints", description = "Get all sprints")
    @GetMapping
    @JsonView(EntityLevelViews.Basic.class)
    public List<Sprint> getSprints(Principal principal) {
        String userId = super.getUserId(principal);
        accessChecker.checkCanViewAllProjects(userId);
        return service.findAll();
    }

    @Operation(summary = "Get specific sprint", description = "Get specific sprint")
    @GetMapping(path = "/{id}")
    @JsonView(EntityLevelViews.Basic.class)
    public Sprint getSprint(Principal principal, @PathVariable("id") Long id) {
        String userId = super.getUserId(principal);
        Sprint sprint = service.get(id);
        accessChecker.checkCanViewProject(sprint.getProject(), userId);
        return sprint;
    }

    @Operation(summary = "Edit specific sprint", description = "Edit specific sprint")
    @PatchMapping(path = "/{id}")
    @JsonView(EntityLevelViews.Basic.class)
    public Sprint editSprint(Principal principal,
                         @PathVariable(name = "id") Long id,
                         @Valid @RequestBody MergePatchSprint sprintRequest) {
        String userId = super.getUserId(principal);
        return service.editSprint(id, sprintRequest, userId);
    }

    @Operation(summary = "Get history of logs of the sprint", description = "Get history of logs of the sprint")
    @GetMapping(path = "/{id}/history")
    @JsonView(EntityLevelViews.Basic.class)
    public List<SprintChange> getHistory(Principal principal, @PathVariable("id") Long id,
                                         @RequestParam(value = "search", required = false) String search) {
        String refinedSearch = super.scopedSearch("entityId:"+ id, search);
        Specification<SprintChange> specification = super.buildSpecificationFromSearch(refinedSearch);
        return sprintChangeService.search(specification);
    }

    @Operation(summary = "Delete specific sprint", description = "Delete specific sprint")
    @DeleteMapping(path = "/{id}")
    public void deleteSprint(Principal principal, @PathVariable("id") Long id) {
        String userId = super.getUserId(principal);
        Sprint sprint = service.get(id);
        accessChecker.checkCanViewProject(sprint.getProject(), userId);
        service.deleteSprint(id);
    }

}
