package org.trackdev.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.trackdev.api.controller.exceptions.ControllerException;
import org.trackdev.api.dto.*;
import org.trackdev.api.entity.Comment;
import org.trackdev.api.entity.PointsReview;
import org.trackdev.api.entity.ProfileAttribute;
import org.trackdev.api.entity.Task;
import org.trackdev.api.entity.TaskAttributeValue;
import org.trackdev.api.entity.TaskStatus;
import org.trackdev.api.entity.TaskType;
import org.trackdev.api.entity.prchanges.PullRequestChange;
import org.trackdev.api.entity.taskchanges.TaskChange;
import org.trackdev.api.mapper.CommentMapper;
import org.trackdev.api.mapper.ProfileMapper;
import org.trackdev.api.mapper.TaskAttributeValueMapper;
import org.trackdev.api.mapper.TaskChangeMapper;
import org.trackdev.api.mapper.TaskMapper;
import org.trackdev.api.model.CreateCommentRequest;
import org.trackdev.api.model.MergePatchTask;
import org.trackdev.api.model.response.StatusListResponse;
import org.trackdev.api.model.response.TaskTypesResponse;
import org.trackdev.api.service.AccessChecker;
import org.trackdev.api.service.CommentService;
import org.trackdev.api.service.PointsReviewService;
import org.trackdev.api.service.PullRequestService;
import org.trackdev.api.service.TaskChangeService;
import org.trackdev.api.service.TaskService;
import org.trackdev.api.utils.ErrorConstants;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.security.Principal;
import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "6. Tasks")
@RestController
@RequestMapping(path = "/tasks")
public class TaskController extends CrudController<Task, TaskService> {

    @Autowired
    TaskChangeService taskChangeService;

    @Autowired
    TaskChangeMapper taskChangeMapper;

    @Autowired
    AccessChecker accessChecker;

    @Autowired
    CommentService commentService;

    @Autowired
    PointsReviewService pointsReviewService;

    @Autowired
    PullRequestService pullRequestService;

    @Autowired
    TaskMapper taskMapper;

    @Autowired
    CommentMapper commentMapper;

    @Autowired
    TaskAttributeValueMapper taskAttributeValueMapper;

    @Autowired
    ProfileMapper profileMapper;

    @Operation(summary = "Get information of tasks", description = "Get information of tasks")
    @GetMapping
    public TasksResponseDTO search(Principal principal,
                         @RequestParam(name = "search", required = false) String search) {
        String userId = super.getUserId(principal);
        accessChecker.checkCanViewAllTasks(userId);
        return new TasksResponseDTO(taskMapper.toBasicDTOList(super.search(search)));
    }

    @Operation(summary = "Get information of a specific task", description = "Get information of a specific task")
    @GetMapping(path = "/{id}")
    public TaskDetailDTO getTask(Principal principal, @PathVariable(name = "id") Long id) {
        String userId = super.getUserId(principal);
        // Authorization check is now inside the service method
        Task task = service.getTask(id, userId);
        List<PointsReview> pointsReview = pointsReviewService.getPointsReview(userId);
        TaskDetailDTO dto = taskMapper.toDetailDTO(task);
        dto.setPointsReview(pointsReview);
        
        // Compute all permission flags based on current user context
        dto.setCanEdit(accessChecker.canEditTask(task, userId));
        dto.setCanEditStatus(accessChecker.canEditStatus(task, userId));
        dto.setCanEditSprint(accessChecker.canEditSprint(task, userId));
        dto.setCanEditType(accessChecker.canEditType(task, userId));
        dto.setCanEditEstimation(accessChecker.canEditEstimation(task, userId));
        dto.setCanDelete(accessChecker.canDeleteTask(task, userId));
        dto.setCanSelfAssign(accessChecker.canSelfAssign(task, userId));
        dto.setCanUnassign(accessChecker.canUnassign(task, userId));
        dto.setCanAddSubtask(accessChecker.canAddSubtask(task, userId));
        dto.setCanFreeze(accessChecker.canFreeze(task, userId));
        dto.setCanComment(accessChecker.canComment(task, userId));
        
        return dto;
    }

    @Operation(summary = "Get comments of the task", description = "Get comments of the task")
    @GetMapping(path = "/{id}/comments")
    public CommentsResponseDTO getComments(Principal principal, @PathVariable(name = "id") Long id) {
        String userId = super.getUserId(principal);
        // Authorization check is now inside the service method
        return new CommentsResponseDTO(commentMapper.toDTOList(service.getComments(id, userId)), id);
    }

    @Operation(summary = "Add a comment to the task", description = "Add a new comment to the task discussion. Any project member can add comments.")
    @PostMapping(path = "/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDTO addComment(Principal principal, 
                                  @PathVariable(name = "id") Long id, 
                                  @Valid @RequestBody CreateCommentRequest request) {
        String userId = super.getUserId(principal);
        Comment comment = service.addComment(id, request.getContent(), userId);
        return commentMapper.toDTO(comment);
    }

    @Operation(summary = "Edit a comment", description = "Edit a comment. Students can only edit their own comments. Professors owning the project can edit any comment.")
    @PatchMapping(path = "/{taskId}/comments/{commentId}")
    public CommentDTO editComment(Principal principal,
                                   @PathVariable(name = "taskId") Long taskId,
                                   @PathVariable(name = "commentId") Long commentId,
                                   @Valid @RequestBody CreateCommentRequest request) {
        String userId = super.getUserId(principal);
        // Verify user can view the task first
        service.getTask(taskId, userId);
        Comment comment = commentService.editComment(commentId, request.getContent(), userId);
        return commentMapper.toDTO(comment);
    }

    @Operation(summary = "Delete a comment", description = "Delete a comment. Only professors owning the project can delete comments.")
    @DeleteMapping(path = "/{taskId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(Principal principal,
                               @PathVariable(name = "taskId") Long taskId,
                               @PathVariable(name = "commentId") Long commentId) {
        String userId = super.getUserId(principal);
        // Verify user can view the task first
        service.getTask(taskId, userId);
        commentService.deleteComment(commentId, userId);
    }

    @Operation(summary = "Edit task information", description = "Edit task information")
    @PatchMapping(path = "/{id}")
    public TaskCompleteDTO editTask(Principal principal,
                           @PathVariable(name = "id") Long id,
                           @Valid @RequestBody MergePatchTask taskRequest) {
        if (taskRequest.name != null){
            if (taskRequest.name.get().isEmpty() || taskRequest.name.get().length() > Task.NAME_LENGTH) {
                throw new ControllerException(ErrorConstants.INVALID_TASK_NAME_LENGTH);
            }
        }
        String userId = super.getUserId(principal);
        Task task = service.editTask(id, taskRequest, userId);
        return taskMapper.toCompleteDTO(task);
    }

    @Operation(summary = "Get history of logs of the task", description = "Get history of logs of the task")
    @GetMapping(path = "/{id}/history")
    public HistoryResponseDTO<TaskLogDTO> getHistory(Principal principal,
                                       @PathVariable(name = "id") Long id,
                                       @RequestParam(name = "search", required = false) String search) {
        String userId = super.getUserId(principal);
        // Auth check and data retrieval in a single transaction
        List<TaskChange> history = service.getTaskHistory(id, userId, search);
        List<TaskLogDTO> historyDTO = taskChangeMapper.toDTOList(history);
        return new HistoryResponseDTO<>(historyDTO, id);
    }

    @Operation(summary = "Get PR change history for a task", description = "Get the history of all pull request changes linked to this task")
    @GetMapping(path = "/{id}/pr-history")
    public HistoryResponseDTO<PullRequestChange> getPrHistory(Principal principal, @PathVariable(name = "id") Long id) {
        String userId = super.getUserId(principal);
        // Verify user can view this task
        service.getTask(id, userId);
        List<PullRequestChange> history = pullRequestService.getPullRequestHistoryForTask(id);
        return new HistoryResponseDTO<>(history, id);
    }

    @Operation(summary = "Create task of User Story", description = "Create task of User Story")
    @PostMapping(path = "/{id}/subtasks")
    public IdResponseDTO createSubtask(Principal principal,
                                      @PathVariable(name = "id") Long id,
                                      @Valid @RequestBody NewSubTask request,
                                      BindingResult result) {
        if (result.hasErrors()) {
            throw new ControllerException(ErrorConstants.INVALID_TASK_NAME_LENGTH);
        }
        String userId = super.getUserId(principal);
        Task subtask = service.createSubTask(id, request.name, userId, request.sprintId, request.type);

        return new IdResponseDTO(subtask.getId());
    }

    @Operation(summary = "Delete specific task", description = "Delete specific task")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(Principal principal,
                            @PathVariable(name = "id") Long id) {
        String userId = super.getUserId(principal);
        service.deleteTask(id, userId);
        return okNoContent();
    }

    @Operation(summary = "Self-assign task to current user", description = "Allows a project member to assign an unassigned task to themselves")
    @PostMapping("/{id}/assign")
    public TaskCompleteDTO selfAssignTask(Principal principal, @PathVariable(name = "id") Long id) {
        String userId = super.getUserId(principal);
        Task task = service.selfAssignTask(id, userId);
        return taskMapper.toCompleteDTO(task);
    }

    @Operation(summary = "Unassign task from current user", description = "Allows the assigned user to unassign themselves from a task")
    @DeleteMapping("/{id}/assign")
    public TaskCompleteDTO unassignTask(Principal principal, @PathVariable(name = "id") Long id) {
        String userId = super.getUserId(principal);
        Task task = service.unassignTask(id, userId);
        return taskMapper.toCompleteDTO(task);
    }

    @Operation(summary = "Get list of tasks status", description = "Get list of tasks status")
    @GetMapping("/status")
    public StatusListResponse getListOfStatus() {
        return new StatusListResponse(service.getListOfStatus());
    }

    @Operation(summary = "Get list of US status", description = "Get list of US status")
    @GetMapping("/usstatus")
    public StatusListResponse getListOfUsStatus() {
        return new StatusListResponse(service.getListOfUsStatus());
    }

    @Operation(summary = "Get list of task status", description = "Get list of task status")
    @GetMapping("/taskstatus")
    public StatusListResponse getListOfTaskStatus() {
        return new StatusListResponse(service.getListOfTaskStatus());
    }

    @Operation(summary = "Get types of tasks", description = "Get types of tasks")
    @GetMapping("/types")
    public TaskTypesResponse getListOfTypes() {
        return new TaskTypesResponse(service.getListOfTypes());
    }

    @Operation(summary = "Get recent tasks for current user", description = "Get the 5 most recent tasks where the user is reporter or assignee")
    @GetMapping("/recent")
    public TasksResponseDTO getRecentTasks(Principal principal) {
        String userId = super.getUserId(principal);
        return new TasksResponseDTO(taskMapper.toBasicDTOList(service.getRecentTasks(userId)));
    }

    @Operation(summary = "Get my tasks paginated", description = "Get all tasks where the user is reporter or assignee, with pagination and optional filters")
    @GetMapping("/my")
    public PagedTasksResponseDTO getMyTasks(
            Principal principal,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "type", required = false) TaskType type,
            @RequestParam(name = "status", required = false) TaskStatus status,
            @RequestParam(name = "assigneeId", required = false) String assigneeId,
            @RequestParam(name = "sortOrder", defaultValue = "desc") String sortOrder) {
        String userId = super.getUserId(principal);
        var tasksPage = service.getMyTasks(userId, page, size, type, status, assigneeId, sortOrder);
        return new PagedTasksResponseDTO(
                taskMapper.toBasicDTOList(tasksPage.getContent()),
                tasksPage.getTotalElements(),
                tasksPage.getTotalPages(),
                tasksPage.getNumber(),
                tasksPage.getSize()
        );
    }

    @Operation(summary = "Freeze a task", description = "Freeze a task to prevent any modifications (PROFESSOR only)")
    @PostMapping(path = "/{taskId}/freeze")
    public TaskBasicDTO freezeTask(Principal principal, @PathVariable(name = "taskId") Long taskId) {
        String userId = super.getUserId(principal);
        Task task = service.freezeTask(taskId, userId);
        return taskMapper.toBasicDTO(task);
    }

    @Operation(summary = "Unfreeze a task", description = "Unfreeze a task to allow modifications (PROFESSOR only)")
    @PostMapping(path = "/{taskId}/unfreeze")
    public TaskBasicDTO unfreezeTask(Principal principal, @PathVariable(name = "taskId") Long taskId) {
        String userId = super.getUserId(principal);
        Task task = service.unfreezeTask(taskId, userId);
        return taskMapper.toBasicDTO(task);
    }

    // ==================== Task Attribute Values ====================

    @Operation(summary = "Get attribute values for a task", description = "Get all attribute values set for a task")
    @GetMapping(path = "/{taskId}/attributes")
    public List<TaskAttributeValueDTO> getTaskAttributeValues(
            Principal principal, 
            @PathVariable(name = "taskId") Long taskId) {
        String userId = super.getUserId(principal);
        List<TaskAttributeValue> values = service.getTaskAttributeValues(taskId, userId);
        return taskAttributeValueMapper.toDTOList(values);
    }

    @Operation(summary = "Get available attributes for a task", description = "Get attributes from course profile that can be applied to this task")
    @GetMapping(path = "/{taskId}/available-attributes")
    public List<ProfileAttributeDTO> getAvailableTaskAttributes(
            Principal principal, 
            @PathVariable(name = "taskId") Long taskId) {
        String userId = super.getUserId(principal);
        List<ProfileAttribute> attributes = service.getAvailableTaskAttributes(taskId, userId);
        return profileMapper.attributesToDTO(attributes);
    }

    @Operation(summary = "Set attribute value for a task", description = "Set or update an attribute value for a task (PROFESSOR only)")
    @PutMapping(path = "/{taskId}/attributes/{attributeId}")
    public TaskAttributeValueDTO setTaskAttributeValue(
            Principal principal,
            @PathVariable(name = "taskId") Long taskId,
            @PathVariable(name = "attributeId") Long attributeId,
            @RequestBody SetAttributeValueRequest request) {
        String userId = super.getUserId(principal);
        TaskAttributeValue value = service.setTaskAttributeValue(taskId, attributeId, request.value, userId);
        return taskAttributeValueMapper.toDTO(value);
    }

    @Operation(summary = "Delete attribute value from a task", description = "Remove an attribute value from a task (PROFESSOR only)")
    @DeleteMapping(path = "/{taskId}/attributes/{attributeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTaskAttributeValue(
            Principal principal,
            @PathVariable(name = "taskId") Long taskId,
            @PathVariable(name = "attributeId") Long attributeId) {
        String userId = super.getUserId(principal);
        service.deleteTaskAttributeValue(taskId, attributeId, userId);
    }


    static class NewSubTask {
        @NotBlank
        @Size(
                min = Task.MIN_NAME_LENGTH,
                max = Task.NAME_LENGTH
        )
        public String name;
        
        public Long sprintId;
        
        public TaskType type;
    }

    static class SetAttributeValueRequest {
        public String value;
    }
}
