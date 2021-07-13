package org.udg.trackdev.spring.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.entity.Backlog;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.entity.taskchanges.TaskChange;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.service.AccessChecker;
import org.udg.trackdev.spring.service.BacklogService;
import org.udg.trackdev.spring.service.TaskChangeService;
import org.udg.trackdev.spring.service.TaskService;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(path = "/tasks")
public class TaskController extends CrudController<Task, TaskService> {
    @Autowired
    BacklogService backlogService;

    @Autowired
    TaskChangeService taskChangeService;

    @Autowired
    AccessChecker accessChecker;

    @GetMapping
    @JsonView(EntityLevelViews.Basic.class)
    public List<Task> search(Principal principal,
                         @RequestParam(value = "backlogId", required = false) Long backlogId,
                         @RequestParam(value = "search", required = false) String search) {
        String userId = super.getUserId(principal);
        String refinedSearch = buildRefinedSearch(backlogId, search, userId);
        return super.search(refinedSearch);
    }

    @GetMapping(path = "/{id}")
    @JsonView(EntityLevelViews.Basic.class)
    public Task getTask(Principal principal, @PathVariable("id") Long id) {
        String userId = super.getUserId(principal);
        Task task = service.get(id);
        accessChecker.checkCanViewBacklog(task.getBacklog(), userId);
        return task;
    }

    @PatchMapping(path = "/{id}")
    @JsonView(EntityLevelViews.Basic.class)
    public Task editTask(Principal principal,
                           @PathVariable(name = "id") Long id,
                           @Valid @RequestBody EditTask taskRequest) {
        String userId = super.getUserId(principal);
        Task modifiedTask = service.editTask(id, taskRequest.name, taskRequest.assignee, userId);
        return modifiedTask;
    }

    @GetMapping(path = "/{id}/history")
    @JsonView(EntityLevelViews.Basic.class)
    public List<TaskChange> getHistory(Principal principal,
                                       @PathVariable(name = "id") Long id) {
        String userId = super.getUserId(principal);
        Task task = service.get(id);
        accessChecker.checkCanViewBacklog(task.getBacklog(), userId);

        return taskChangeService.findTaskChangesByTask(id);
    }

    private String buildRefinedSearch(Long backlogId, String search, String userId) {
        String refinedSearch = search;
        if(backlogId != null) {
            Backlog backlog = backlogService.get(backlogId);
            accessChecker.checkCanViewBacklog(backlog, userId);
            refinedSearch = super.scopedSearch("backlogId:"+ backlogId, search);
        } else {
            accessChecker.checkCanViewAllTasks(userId);
        }
        return refinedSearch;
    }

    static class EditTask {
        @Size(max = Task.NAME_LENGTH)
        public String name;

        public String assignee;
    }
}
