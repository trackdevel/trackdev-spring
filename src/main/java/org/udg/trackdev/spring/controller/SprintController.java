package org.udg.trackdev.spring.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.udg.trackdev.spring.entity.Sprint;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.service.AccessChecker;
import org.udg.trackdev.spring.service.SprintService;

import java.security.Principal;

@RestController
@RequestMapping(path = "/sprints")
public class SprintController extends CrudController<Sprint, SprintService> {
    @Autowired
    AccessChecker accessChecker;

    @GetMapping(path = "/{id}")
    @JsonView(EntityLevelViews.Basic.class)
    public Sprint getSprint(Principal principal, @PathVariable("id") Long id) {
        String userId = super.getUserId(principal);
        Sprint sprint = service.get(id);
        accessChecker.checkCanViewBacklog(sprint.getBacklog(), userId);
        return sprint;
    }
}
