package org.udg.trackdev.spring.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.service.TaskService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(path = "/tasks")
public class TaskController extends CrudController<Task, TaskService> {
    @GetMapping
    @JsonView(EntityLevelViews.Basic.class)
    public List<Task> search(Principal principal, @RequestParam(value = "search", required = false) String search) {
        String userId = super.getUserId(principal);
        return super.search(search);
    }

    @GetMapping(path = "/{id}")
    @JsonView(EntityLevelViews.Basic.class)
    public Task getTask(@PathVariable("id") Long id) {
        Task task = service.get(id);
        return task;
    }
}
