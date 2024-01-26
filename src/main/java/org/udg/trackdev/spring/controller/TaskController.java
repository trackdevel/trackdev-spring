package org.udg.trackdev.spring.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.controller.exceptions.ControllerException;
import org.udg.trackdev.spring.entity.Comment;
import org.udg.trackdev.spring.entity.PointsReview;
import org.udg.trackdev.spring.entity.Sprint;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.entity.taskchanges.TaskChange;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.model.IdObjectLong;
import org.udg.trackdev.spring.model.MergePatchTask;
import org.udg.trackdev.spring.model.TaskWithPointsReview;
import org.udg.trackdev.spring.service.AccessChecker;
import org.udg.trackdev.spring.service.PointsReviewService;
import org.udg.trackdev.spring.service.TaskChangeService;
import org.udg.trackdev.spring.service.TaskService;
import org.udg.trackdev.spring.utils.ErrorConstants;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "6. Tasks")
@RestController
@RequestMapping(path = "/tasks")
public class TaskController extends CrudController<Task, TaskService> {

    @Autowired
    TaskChangeService taskChangeService;

    @Autowired
    AccessChecker accessChecker;

    @Autowired
    PointsReviewService pointsReviewService;

    @Operation(summary = "Get information of tasks", description = "Get information of tasks")
    @GetMapping
    @JsonView(EntityLevelViews.Basic.class)
    public List<Task> search(Principal principal,
                         @RequestParam(value = "search", required = false) String search) {
        String userId = super.getUserId(principal);
        accessChecker.checkCanViewAllTasks(userId);
        return super.search(search);
    }

    @Operation(summary = "Get information of a specific task", description = "Get information of a specific task")
    @GetMapping(path = "/{id}")
    @JsonView(EntityLevelViews.TaskWithProjectMembers.class)
    public TaskWithPointsReview getTask(Principal principal, @PathVariable("id") Long id) {
        String userId = super.getUserId(principal);
        Task task = service.get(id);
        accessChecker.checkCanViewProject(task.getProject(), userId);
        List<PointsReview> pointsReview = pointsReviewService.getPointsReview(userId);
        return new TaskWithPointsReview(task, pointsReview);
    }

    @Operation(summary = "Get comments of the task", description = "Get comments of the task")
    @GetMapping(path = "/{id}/comments")
    @JsonView(EntityLevelViews.Basic.class)
    public Collection<Comment> getComments(Principal principal, @PathVariable("id") Long id) {
        String userId = super.getUserId(principal);
        Task task = service.get(id);
        accessChecker.checkCanViewProject(task.getProject(), userId);
        return service.getComments(id);
    }

    @Operation(summary = "Edit task information", description = "Edit task information")
    @PatchMapping(path = "/{id}")
    @JsonView(EntityLevelViews.TaskComplete.class)
    public Task editTask(Principal principal,
                           @PathVariable(name = "id") Long id,
                           @Valid @RequestBody MergePatchTask taskRequest) {
        if (taskRequest.name.isPresent()){
            if (taskRequest.name.get().isEmpty() || taskRequest.name.get().length() > Task.NAME_LENGTH) {
                throw new ControllerException(ErrorConstants.INVALID_TASK_NAME_LENGTH);
            }
        }
        String userId = super.getUserId(principal);
        return service.editTask(id, taskRequest, userId);
    }

    @Operation(summary = "Get history of logs of the task", description = "Get history of logs of the task")
    @GetMapping(path = "/{id}/history")
    @JsonView(EntityLevelViews.Basic.class)
    public List<TaskChange> getHistory(Principal principal,
                                       @PathVariable(name = "id") Long id,
                                       @RequestParam(value = "search", required = false) String search) {
        String refinedSearch = super.scopedSearch("entityId:"+ id, search);
        Specification<TaskChange> specification = super.buildSpecificationFromSearch(refinedSearch);
        return taskChangeService.search(specification);
    }

    @Operation(summary = "Create task of User Story", description = "Create task of User Story")
    @PostMapping(path = "/{id}/subtasks")
    public IdObjectLong createSubtask(Principal principal,
                                      @PathVariable(name = "id") Long id,
                                      @Valid @RequestBody NewSubTask request,
                                      BindingResult result) {
        if (result.hasErrors()) {
            throw new ControllerException(ErrorConstants.INVALID_TASK_NAME_LENGTH);
        }
        String userId = super.getUserId(principal);
        Task subtask = service.createSubTask(id, request.name, userId);

        return new IdObjectLong(subtask.getId());
    }

    @Operation(summary = "Delete especific task", description = "Delete especific task")
    @DeleteMapping("/{id}")
    public void deleteTasks(Principal principal,
                            @PathVariable(name = "id") Long id) {
        String userId = super.getUserId(principal);
        service.deleteTask(id, userId);
    }

    @Operation(summary = "Get list of tasks status", description = "Get list of tasks status")
    @GetMapping("/status")
    public Map<String,String > getListOfStatus() {
        return service.getListOfStatus();
    }

    @Operation(summary = "Get list of US status", description = "Get list of US status")
    @GetMapping("/usstatus")
    public Map<String,String > getListOfUsStatus() {
        return service.getListOfUsStatus();
    }

    @Operation(summary = "Get list of task status", description = "Get list of task status")
    @GetMapping("/taskstatus")
    public Map<String,String > getListOfTaskStatus() {
        return service.getListOfTaskStatus();
    }

    @Operation(summary = "Get types of tasks", description = "Get types of tasks")
    @GetMapping("/types")
    public Map<String,String> getListOfTypes() {
        return service.getListOfTypes();
    }

    static class NewSubTask {
        @NotBlank
        @Size(
                min = Task.MIN_NAME_LENGTH,
                max = Task.NAME_LENGTH
        )
        public String name;
    }
}
