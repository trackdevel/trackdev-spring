package org.udg.trackdev.spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.entity.IdObjectLong;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.service.TaskService;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.security.Principal;

@RestController
@RequestMapping(path = "/backlogs")
public class BacklogController extends BaseController {
    @Autowired
    private TaskService taskService;

    @PostMapping(path = "/{id}/tasks")
    public IdObjectLong createTask(Principal principal,
                                   @PathVariable("id") Long id,
                                   @Valid @RequestBody NewTask taskRequest) {
        String userId = getUserId(principal);
        Task createdTask = taskService.createTask(id, taskRequest.name, userId);
        return new IdObjectLong(createdTask.getId());
    }

    static class NewTask {
        @NotBlank
        @Size(max = Task.NAME_LENGTH)
        public String name;
    }
}
