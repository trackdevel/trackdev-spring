package org.udg.trackdev.spring.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.entity.Comment;
import org.udg.trackdev.spring.entity.PointsReview;
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

    @GetMapping
    @JsonView(EntityLevelViews.Basic.class)
    public List<Task> search(Principal principal,
                         @RequestParam(value = "search", required = false) String search) {
        String userId = super.getUserId(principal);
        accessChecker.checkCanViewAllTasks(userId);
        return super.search(search);
    }

    @GetMapping(path = "/{id}")
    @JsonView(EntityLevelViews.TaskWithProjectMembers.class)
    public TaskWithPointsReview getTask(Principal principal, @PathVariable("id") Long id) {
        String userId = super.getUserId(principal);
        Task task = service.get(id);
        accessChecker.checkCanViewProject(task.getProject(), userId);
        List<PointsReview> pointsReview = pointsReviewService.getPointsReview(userId);
        return new TaskWithPointsReview(task, pointsReview);
    }

    /** POTSER NECESARI PER REFRESCAR DISCUSSIONS **/
    @GetMapping(path = "/{id}/comments")
    @JsonView(EntityLevelViews.Basic.class)
    public Collection<Comment> getComments(Principal principal, @PathVariable("id") Long id) {
        String userId = super.getUserId(principal);
        Task task = service.get(id);
        accessChecker.checkCanViewProject(task.getProject(), userId);
        return service.getComments(id);
    }
    /***/

    @PatchMapping(path = "/{id}")
    @JsonView(EntityLevelViews.TaskComplete.class)
    public Task editTask(Principal principal,
                           @PathVariable(name = "id") Long id,
                           @Valid @RequestBody MergePatchTask taskRequest) {
        String userId = super.getUserId(principal);
        return service.editTask(id, taskRequest, userId);
    }

    @GetMapping(path = "/{id}/history")
    @JsonView(EntityLevelViews.Basic.class)
    public List<TaskChange> getHistory(Principal principal,
                                       @PathVariable(name = "id") Long id,
                                       @RequestParam(value = "search", required = false) String search) {
        String refinedSearch = super.scopedSearch("entityId:"+ id, search);
        Specification<TaskChange> specification = super.buildSpecificationFromSearch(refinedSearch);
        return taskChangeService.search(specification);
    }

    @PostMapping(path = "/{id}/subtasks")
    public IdObjectLong createSubtask(Principal principal,
                                      @PathVariable(name = "id") Long id,
                                      @Valid @RequestBody NewSubTask request) {
        String userId = super.getUserId(principal);
        Task subtask = service.createSubTask(id, request.name, userId);

        return new IdObjectLong(subtask.getId());
    }

    @DeleteMapping("/{id}")
    public void deleteTasks(Principal principal,
                            @PathVariable(name = "id") Long id) {
        String userId = super.getUserId(principal);
        service.deleteTask(id, userId);
    }

    @GetMapping("/status")
    public Map<String,String > getListOfStatus() {
        return service.getListOfStatus();
    }

    @GetMapping("/types")
    public Map<String,String> getListOfTypes() {
        return service.getListOfTypes();
    }

    static class NewSubTask {
        @NotBlank
        @Size(max = Task.NAME_LENGTH)
        public String name;
    }
}
