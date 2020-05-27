package org.udg.trackdev.spring.controller;

import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.service.TaskService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(path = "/tasks")
@ResponseBody
public class TaskController extends CrudController<Task, TaskService> {
    @GetMapping
    public List<Task> search(Principal principal, @RequestParam(value = "search", required = false) String search) {
        String username = principal.getName();
        return super.search(search);
    }

}
