package org.udg.trackdev.spring.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.entity.Backlog;
import org.udg.trackdev.spring.entity.Sprint;
import org.udg.trackdev.spring.model.IdObjectLong;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.service.AccessChecker;
import org.udg.trackdev.spring.service.BacklogService;
import org.udg.trackdev.spring.service.SprintService;
import org.udg.trackdev.spring.service.TaskService;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.security.Principal;
import java.util.Calendar;
import java.util.List;

@RestController
@RequestMapping(path = "/backlogs")
public class BacklogController extends CrudController<Backlog, BacklogService> {
    @Autowired
    private TaskService taskService;

    @Autowired
    private SprintService sprintService;

    @Autowired
    private AccessChecker accessChecker;

    @GetMapping(path = "/{id}/tasks")
    @JsonView(EntityLevelViews.Basic.class)
    public List<Task> getTasks(Principal principal,
                           @PathVariable("id") Long id,
                           @RequestParam(value = "search", required = false) String search) {
        String userId = getUserId(principal);
        Backlog backlog = service.get(id);
        accessChecker.checkCanViewBacklog(backlog, userId);
        String refinedSearch = super.scopedSearch("backlogId:"+id, search);

        Specification<Task> specification = super.buildSpecificationFromSearch(refinedSearch);
        return taskService.search(specification, Sort.by("rank"));
    }

    @PostMapping(path = "/{id}/tasks")
    public IdObjectLong createTask(Principal principal,
                                   @PathVariable("id") Long id,
                                   @Valid @RequestBody NewTask taskRequest) {
        String userId = getUserId(principal);
        Task createdTask = taskService.createTask(id, taskRequest.name, userId);
        return new IdObjectLong(createdTask.getId());
    }

    @GetMapping(path = "/{id}/sprints")
    @JsonView(EntityLevelViews.Basic.class)
    public List<Sprint> getSprints(Principal principal,
                               @PathVariable("id") Long id) {
        String userId = getUserId(principal);
        Backlog backlog = service.get(id);
        accessChecker.checkCanViewBacklog(backlog, userId);

        Specification<Sprint> specification = super.buildSpecificationFromSearch("backlogId:"+id);
        return sprintService.search(specification, Sort.by("startDate").descending());
    }

    @PostMapping(path = "/{id}/sprints")
    public IdObjectLong createSprint(Principal principal,
                                   @PathVariable("id") Long id,
                                   @Valid @RequestBody NewSprint sprintRequest) {
        String userId = getUserId(principal);
        Sprint createdSprint = sprintService.create(id,
                sprintRequest.name, sprintRequest.startDate.getTime(), sprintRequest.endDate.getTime(),
                userId);
        return new IdObjectLong(createdSprint.getId());
    }

    static class NewTask {
        @NotBlank
        @Size(max = Task.NAME_LENGTH)
        public String name;
    }

    static class NewSprint {
        @NotBlank
        @Size(max = Task.NAME_LENGTH)
        public String name;

        @NotNull
        @FutureOrPresent
        public Calendar startDate;

        @NotNull
        @Future
        public Calendar endDate;
    }
}
