package org.udg.trackdev.spring.controller;

import com.fasterxml.jackson.annotation.JsonView;
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

@RestController
@RequestMapping(path = "/sprints")
public class SprintController extends CrudController<Sprint, SprintService> {
    @Autowired
    AccessChecker accessChecker;

    @Autowired
    SprintChangeService sprintChangeService;

    @GetMapping(path = "/{id}")
    @JsonView(EntityLevelViews.Basic.class)
    public Sprint getSprint(Principal principal, @PathVariable("id") Long id) {
        String userId = super.getUserId(principal);
        Sprint sprint = service.get(id);
        accessChecker.checkCanViewBacklog(sprint.getBacklog(), userId);
        return sprint;
    }

    @PatchMapping(path = "/{id}")
    @JsonView(EntityLevelViews.Basic.class)
    public Sprint editSprint(Principal principal,
                         @PathVariable(name = "id") Long id,
                         @Valid @RequestBody MergePatchSprint sprintRequest) {
        String userId = super.getUserId(principal);
        Sprint modifiedSprint = service.editSprint(id, sprintRequest, userId);
        return modifiedSprint;
    }

    @GetMapping(path = "/{id}/history")
    @JsonView(EntityLevelViews.Basic.class)
    public List<SprintChange> getHistory(Principal principal, @PathVariable("id") Long id,
                                         @RequestParam(value = "search", required = false) String search) {
        String userId = super.getUserId(principal);
        Sprint sprint = service.get(id);
        accessChecker.checkCanViewBacklog(sprint.getBacklog(), userId);

        String refinedSearch = super.scopedSearch("entityId:"+ id, search);
        Specification<SprintChange> specification = super.buildSpecificationFromSearch(refinedSearch);
        return sprintChangeService.search(specification);
    }
}
