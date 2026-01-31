package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.configuration.UserType;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.*;
import org.trackdev.api.entity.taskchanges.*;
import org.trackdev.api.model.MergePatchTask;
import org.trackdev.api.query.CriteriaParser;
import org.trackdev.api.query.GenericSpecificationsBuilder;
import org.trackdev.api.query.SearchSpecification;
import org.trackdev.api.repository.ProfileAttributeRepository;
import org.trackdev.api.repository.TaskAttributeValueRepository;
import org.trackdev.api.repository.TaskRepository;
import org.trackdev.api.utils.ErrorConstants;
import org.trackdev.api.utils.HtmlSanitizer;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService extends BaseServiceLong<Task, TaskRepository> {

    @Autowired
    ProjectService projectService;

    @Autowired
    UserService userService;

    @Autowired
    TaskChangeService taskChangeService;

    @Autowired
    SprintService sprintService;

    @Autowired
    CommentService commentService;

    @Autowired
    AccessChecker accessChecker;

    @Autowired
    PointsReviewService pointsReviewService;

    @Autowired
    I18nService i18nService;

    @Autowired
    ActivityService activityService;

    @Autowired
    TaskAttributeValueRepository taskAttributeValueRepository;

    @Autowired
    ProfileAttributeRepository profileAttributeRepository;

    @Transactional
    public Task createTask(Long projectId, String name, String userId) {
        Project project = projectService.get(projectId);
        User user = userService.get(userId);
        accessChecker.checkCanViewProject(project, userId);
        // Sanitize task name to prevent XSS attacks
        String sanitizedName = HtmlSanitizer.sanitize(name);
        Task task = new Task(sanitizedName, user);
        task.setType(TaskType.USER_STORY);
        project.addTask(task);  // This sets project, taskNumber, and taskKey
        this.repo.save(task);

        // Record activity for task creation
        activityService.recordActivity(ActivityType.TASK_CREATED, user, task);

        return task;
    }

    /**
     * Self-assign a task to the current user.
     * Any project member can self-assign an unassigned task.
     */
    @Transactional
    public Task selfAssignTask(Long taskId, String userId) {
        Task task = get(taskId);
        User user = userService.get(userId);
        accessChecker.checkCanSelfAssignTask(task, userId);
        
        String oldValue = task.getAssignee() != null ? task.getAssignee().getUsername() : null;
        task.setAssignee(user);
        repo.save(task);
        
        // Record the change
        TaskChange change = new TaskAssigneeChange(user, task, oldValue, user.getUsername());
        taskChangeService.store(change);
        
        // Record activity
        activityService.recordActivity(ActivityType.TASK_ASSIGNED, user, task.getProject(), task, 
                null, oldValue, user.getUsername());
        
        return task;
    }

    /**
     * Unassign the current user from a task.
     * Only the assigned user or professor/admin can unassign.
     */
    @Transactional
    public Task unassignTask(Long taskId, String userId) {
        Task task = get(taskId);
        User user = userService.get(userId);
        
        // Only the assigned user can unassign themselves (or course owner/subject owner/admin)
        if (!accessChecker.isTaskAssignee(task, userId)) {
            // Check if course owner, subject owner, or admin
            Course course = task.getProject().getCourse();
            Subject subject = course.getSubject();
            boolean isCourseOwner = course.getOwnerId().equals(userId);
            boolean isSubjectOwner = subject.getOwnerId().equals(userId);
            boolean isAdmin = user.isUserType(org.trackdev.api.configuration.UserType.ADMIN);
            
            if (!isCourseOwner && !isSubjectOwner && !isAdmin) {
                throw new ServiceException(ErrorConstants.UNAUTHORIZED);
            }
        }
        
        String oldValue = task.getAssignee() != null ? task.getAssignee().getUsername() : null;
        task.setAssignee(null);
        repo.save(task);
        
        // Record the change
        TaskChange change = new TaskAssigneeChange(user, task, oldValue, null);
        taskChangeService.store(change);
        
        // Record activity
        activityService.recordActivity(ActivityType.TASK_UNASSIGNED, user, task.getProject(), task, 
                null, oldValue, null);
        
        return task;
    }

    @Transactional
    public Task createSubTask(Long taskId, String name, String userId, Long sprintId, TaskType type) {
        return createSubTaskInternal(taskId, name, userId, sprintId, type, false);
    }

    /**
     * Internal method for creating subtasks with option to bypass past sprint check.
     * Used by DemoDataSeeder to create subtasks in past sprints for demo purposes.
     */
    @Transactional
    public Task createSubTaskInternal(Long taskId, String name, String userId, Long sprintId, TaskType type, boolean allowPastSprint) {
        Task parentTask = this.get(taskId);
        User user = userService.get(userId);
        
        // Only USER_STORY tasks can have subtasks
        if (parentTask.getTaskType() != TaskType.USER_STORY) {
            throw new ServiceException(ErrorConstants.ONLY_USER_STORY_CAN_HAVE_SUBTASKS);
        }
        
        // Any project member (or professor/admin) can create subtasks
        accessChecker.checkCanCreateSubtask(parentTask, userId);
        // Sanitize subtask name to prevent XSS attacks
        String sanitizedName = HtmlSanitizer.sanitize(name);
        Task subtask = new Task(sanitizedName, user);
        
        // Subtasks can only be TASK or BUG, default to TASK if not specified or invalid
        if (type == TaskType.TASK || type == TaskType.BUG) {
            subtask.setType(type);
        } else {
            subtask.setType(TaskType.TASK);
        }
        subtask.setParentTask(parentTask);
        
        // Auto-assign subtask to the student who creates it
        if (user.isUserType(UserType.STUDENT)) {
            subtask.setAssignee(user);
        }
        
        // Determine the target sprint FIRST, then set status based on sprint assignment
        // Assign subtask to ONE sprint only:
        // - If sprintId is provided (created from Sprint view):
        //   - If that sprint is past (endDate < now), assign to the current active sprint instead
        //     (unless allowPastSprint is true, for demo data seeding)
        //   - Otherwise, use that sprint
        // - If sprintId is null (created from Task view), use the newest sprint from parent USER_STORY
        Sprint targetSprint = null;
        ZonedDateTime now = ZonedDateTime.now();
        
        if (sprintId != null) {
            Sprint requestedSprint = sprintService.get(sprintId);
            // Verify the sprint belongs to the same project
            if (!requestedSprint.getProject().getId().equals(parentTask.getProject().getId())) {
                throw new ServiceException(ErrorConstants.SPRINT_NOT_IN_PROJECT);
            }
            
            // If the requested sprint is in the past, find the active sprint for this project
            // (unless allowPastSprint is true for demo data seeding)
            if (!allowPastSprint && requestedSprint.getEndDate() != null && requestedSprint.getEndDate().isBefore(now)) {
                // Find active sprint (startDate < now < endDate) in the project
                targetSprint = sprintService.findActiveSprintForProject(parentTask.getProject().getId())
                    .orElse(requestedSprint);  // Fallback to requested if no active sprint found
            } else {
                targetSprint = requestedSprint;
            }
        } else if (parentTask.getChildTasks() != null && !parentTask.getChildTasks().isEmpty()) {
            // Find the newest sprint from existing subtasks (not parent's sprints)
            targetSprint = parentTask.getChildTasks().stream()
                .filter(child -> child.getActiveSprints() != null && !child.getActiveSprints().isEmpty())
                .flatMap(child -> child.getActiveSprints().stream())
                .max((s1, s2) -> s1.getStartDate().compareTo(s2.getStartDate()))
                .orElse(null);
        }
        
        // Set subtask status based on sprint assignment:
        // - If subtask will be assigned to a sprint, status is TODO (never BACKLOG with sprint)
        // - If no sprint assigned, status is BACKLOG
        if (targetSprint != null) {
            subtask.setStatus(TaskStatus.TODO);
            subtask.getActiveSprints().add(targetSprint);
            targetSprint.addTask(subtask, user);
        } else {
            subtask.setStatus(TaskStatus.BACKLOG);
        }
        
        parentTask.getProject().addTask(subtask);  // This sets project, taskNumber, and taskKey
        parentTask.addChildTask(subtask);
        this.repo.save(subtask);

        // Record activity for subtask creation
        activityService.recordActivity(ActivityType.TASK_CREATED, user, subtask);

        return subtask;
    }

    @Transactional
    public Task editTask(Long id, MergePatchTask editTask, String userId) {
        Task task = get(id);
        User user = userService.get(userId);
        
        // Check if task is frozen - no edits allowed on frozen tasks
        if (task.isFrozen()) {
            throw new ServiceException(ErrorConstants.TASK_IS_FROZEN);
        }
        
        accessChecker.checkCanViewProject(task.getProject(), userId);

        // Check if user can edit task fields (name, description, estimation, rank, sprints, type)
        // This is required for any field modification except status (which has its own check)
        boolean hasEditFields = editTask.name != null || editTask.description != null || 
                                editTask.estimationPoints != null || editTask.rank != null ||
                                editTask.reporter != null || editTask.assignee != null ||
                                editTask.activeSprints != null || editTask.type != null;
        if (hasEditFields) {
            accessChecker.checkCanEditTask(task, userId);
        }

        List<TaskChange> changes = new ArrayList<>();
        if(editTask.name != null) {
            String oldName = task.getName();
            String name = editTask.name.orElseThrow(
                    () -> new ServiceException(ErrorConstants.CAN_NOT_BE_NULL));
            // Sanitize task name to prevent XSS attacks
            String sanitizedName = HtmlSanitizer.sanitize(name);
            task.setName(sanitizedName);
            changes.add(new TaskNameChange(user, task, oldName, sanitizedName));
        }
        if(editTask.description != null) {
            // Sanitize description to prevent XSS attacks
            String sanitizedDescription = editTask.description.isPresent() 
                    ? HtmlSanitizer.sanitize(editTask.description.get()) 
                    : null;
            task.setDescription(sanitizedDescription);
        }
        if(editTask.type != null) {
            TaskType newType = editTask.type.orElseThrow(
                    () -> new ServiceException(ErrorConstants.CAN_NOT_BE_NULL));
            TaskType oldType = task.getTaskType();
            
            // Validate type change according to entity constraints
            if (oldType != newType) {
                // USER_STORY cannot change type (regardless of having children)
                if (oldType == TaskType.USER_STORY) {
                    throw new ServiceException(ErrorConstants.USER_STORY_CANNOT_CHANGE_TYPE);
                }
                
                // BUG cannot change type
                if (oldType == TaskType.BUG) {
                    throw new ServiceException(ErrorConstants.BUG_CANNOT_CHANGE_TYPE);
                }
                
                // Subtasks (tasks with parent) can only be TASK or BUG
                if (task.getParentTask() != null && newType == TaskType.USER_STORY) {
                    throw new ServiceException(ErrorConstants.SUBTASK_MUST_BE_TASK_OR_BUG);
                }
                
                task.setType(newType);
                changes.add(new TaskTypeChange(user, task, 
                        oldType != null ? oldType.toString() : null, newType.toString()));
            }
        }
        if(editTask.reporter != null) {
            User reporterUser = null;
            if (editTask.reporter.isPresent()) {
                reporterUser = userService.getByEmail(editTask.reporter.get());
                if (!task.getProject().isMember(reporterUser)) {
                    throw new ServiceException(ErrorConstants.USER_NOT_PRJ_MEMBER);
                }
                task.setReporter(reporterUser);
            }
        }
        if(editTask.assignee != null) {
            User assigneeUser = null;
            String oldValue = task.getAssignee() != null ? task.getAssignee().getUsername() : null;
            if(editTask.assignee.isPresent()) {
                assigneeUser = userService.getByEmail(editTask.assignee.get());
                if(!task.getProject().isMember(assigneeUser)) {
                    throw new ServiceException(ErrorConstants.USER_NOT_PRJ_MEMBER);
                }
                task.setAssignee(assigneeUser);
            } else {
                task.setAssignee(null);
            }
            String newValue = task.getAssignee() != null ? task.getAssignee().getUsername() : null;
            changes.add(new TaskAssigneeChange(user, task, oldValue, newValue));
        }
        if(editTask.estimationPoints != null) {
            // USER_STORY estimation points are calculated from subtasks, cannot be set manually
            if (task.getTaskType() == TaskType.USER_STORY) {
                throw new ServiceException(ErrorConstants.USER_STORY_ESTIMATION_IS_CALCULATED);
            }
            // TASK/BUG can only have estimation points in VERIFY or DONE status
            TaskStatus currentStatus = task.getStatus();
            if (currentStatus != TaskStatus.VERIFY && currentStatus != TaskStatus.DONE) {
                throw new ServiceException(ErrorConstants.ESTIMATION_ONLY_IN_VERIFY_OR_DONE);
            }
            Integer oldPoints = task.getEstimationPoints();
            Integer points = editTask.estimationPoints.orElse(null);
            task.setEstimationPoints(points);
            changes.add(new TaskEstimationPointsChange(user, task, oldPoints, points));
        }
        if(editTask.status != null) {
            // Only assigned user (or professor/admin) can modify status
            accessChecker.checkCanModifyTaskStatus(task, userId);
            TaskStatus status = editTask.status.orElseThrow(
                    () -> new ServiceException(ErrorConstants.CAN_NOT_BE_NULL));
            
            // Check if task is in a future sprint - cannot change status from TODO
            TaskStatus currentStatus = task.getStatus();
            if (currentStatus == TaskStatus.TODO && status != TaskStatus.TODO) {
                Collection<Sprint> activeSprints = task.getActiveSprints();
                if (activeSprints != null && !activeSprints.isEmpty()) {
                    // Check if ALL sprints are in DRAFT (future) status
                    boolean allSprintsInFuture = activeSprints.stream()
                            .allMatch(sprint -> sprint.getEffectiveStatus() == SprintStatus.DRAFT);
                    if (allSprintsInFuture) {
                        throw new ServiceException(ErrorConstants.TASK_STATUS_CHANGE_IN_FUTURE_SPRINT);
                    }
                }
            }
            
            // Check if task can be moved to VERIFY
            if (status == TaskStatus.VERIFY && !task.canMoveToVerify()) {
                throw new ServiceException(ErrorConstants.TASK_CANNOT_VERIFY_WITHOUT_PULL_REQUEST);
            }
            
            // Check if task can be moved to DONE
            if (status == TaskStatus.DONE && !task.canMoveToDone()) {
                // Determine the specific reason for rejection
                if (!task.hasPullRequest()) {
                    throw new ServiceException(ErrorConstants.TASK_CANNOT_BE_DONE_WITHOUT_PULL_REQUEST);
                } else if (!task.areAllPRsMerged()) {
                    throw new ServiceException(ErrorConstants.TASK_CANNOT_BE_DONE_WITHOUT_MERGED_PRS);
                } else if (task.getEstimationPoints() == null || task.getEstimationPoints() <= 0) {
                    throw new ServiceException(ErrorConstants.TASK_CANNOT_BE_DONE_WITHOUT_ESTIMATION);
                } else if (task.getTaskType() == TaskType.USER_STORY) {
                    throw new ServiceException(ErrorConstants.USER_STORY_CANNOT_BE_DONE_WITH_PENDING_SUBTASKS);
                }
            }
            
            TaskStatus oldStatus = task.getStatus();
            task.setStatus(status);
            changes.add(new TaskStatusChange(user, task, oldStatus.toString(), status.toString()));
            
            // If this is a subtask being set to DONE, check if parent USER_STORY should auto-complete
            if (status == TaskStatus.DONE && task.getParentTask() != null) {
                Task parentTask = task.getParentTask();
                if (parentTask.getTaskType() == TaskType.USER_STORY && parentTask.areAllSubtasksDone()) {
                    TaskStatus parentOldStatus = parentTask.getStatus();
                    if (parentOldStatus != TaskStatus.DONE) {
                        parentTask.setStatus(TaskStatus.DONE);
                        changes.add(new TaskStatusChange(user, parentTask, 
                                parentOldStatus.toString(), TaskStatus.DONE.toString()));
                        repo.save(parentTask);
                    }
                }
            }
        }
        if(editTask.rank != null) {
            Integer newRank = editTask.rank.orElseThrow(
                    () -> new ServiceException(ErrorConstants.CAN_NOT_BE_NULL));
            Integer currentRank = task.getRank();
            if(!Objects.equals(newRank, currentRank)) {
                //Collection<TaskChange> otherChanges = updateOtherTasksRank(user, newRank, currentRank);
                changes.add(new TaskRankChange(user, task, task.getRank(), newRank));
                task.setRank(newRank);
                //changes.addAll(otherChanges);
            }
        }
        if(editTask.activeSprints != null){
            Collection<Long> sprintsIds = editTask.activeSprints.orElseThrow(
                    () -> new ServiceException(ErrorConstants.CAN_NOT_BE_NULL));
            
            // Moving to backlog (removing from sprint) validations
            if (sprintsIds.isEmpty()) {
                // Subtasks cannot be moved to backlog individually - must move parent USER_STORY
                if (task.getTaskType() != TaskType.USER_STORY && task.getParentTask() != null) {
                    throw new ServiceException(ErrorConstants.SUBTASK_CANNOT_MOVE_TO_BACKLOG);
                }
                
                // TASK/BUG (without parent) can only move to backlog if status is TODO
                if (task.getTaskType() != TaskType.USER_STORY && task.getStatus() != TaskStatus.TODO) {
                    throw new ServiceException(ErrorConstants.TASK_BEGUN_CANNOT_MOVE_TO_BACKLOG);
                }
                
                // USER_STORY can only move to backlog if ALL subtasks are in TODO status
                if (task.getTaskType() == TaskType.USER_STORY) {
                    Collection<Task> childTasks = task.getChildTasks();
                    if (childTasks != null && !childTasks.isEmpty()) {
                        boolean anySubtaskNotTodo = childTasks.stream()
                            .anyMatch(subtask -> subtask.getStatus() != TaskStatus.TODO);
                        if (anySubtaskNotTodo) {
                            throw new ServiceException(ErrorConstants.USER_STORY_SUBTASKS_NOT_TODO);
                        }
                    }
                }
            }
            
            // USER_STORY can only be assigned to sprint if ALL subtasks are unassigned from any sprint
            // When assigned, all subtasks will be automatically assigned to the same sprint
            // This check only applies when ASSIGNING to a sprint (not when removing/moving to backlog)
            if (!sprintsIds.isEmpty() && task.getTaskType() == TaskType.USER_STORY) {
                Collection<Task> childTasks = task.getChildTasks();
                if (childTasks != null && !childTasks.isEmpty()) {
                    // Force initialization of child tasks and their sprints to avoid lazy loading issues
                    for (Task subtask : childTasks) {
                        if (subtask.getActiveSprints() != null) {
                            subtask.getActiveSprints().size(); // Force initialization
                        }
                    }
                    // Check if any subtask is assigned to a sprint
                    boolean anySubtaskHasSprint = childTasks.stream()
                        .anyMatch(subtask -> subtask.getActiveSprints() != null && !subtask.getActiveSprints().isEmpty());
                    if (anySubtaskHasSprint) {
                        throw new ServiceException(ErrorConstants.USER_STORY_CANNOT_BE_ASSIGNED_TO_SPRINT);
                    }
                }
            }
            
            // TASK/BUG can only be in one sprint
            if (sprintsIds.size() > 1) {
                throw new ServiceException(ErrorConstants.TASK_CAN_ONLY_BE_IN_ONE_SPRINT);
            }
            
            // Cannot reassign if task is DONE
            if (task.getStatus() == TaskStatus.DONE && !sprintsIds.isEmpty()) {
                throw new ServiceException(ErrorConstants.CANNOT_REASSIGN_DONE_TASK);
            }
            
            // Task in FUTURE sprint can only be moved to backlog (not to another sprint)
            if (!sprintsIds.isEmpty() && accessChecker.isTaskInFutureSprintOnly(task)) {
                throw new ServiceException(ErrorConstants.TASK_IN_FUTURE_SPRINT_ONLY_TO_BACKLOG);
            }
            
            // Validate sprint is active or future (not closed)
            Collection<Sprint> sprints = sprintService.getSprintsByIds(sprintsIds);
            for (Sprint sprint : sprints) {
                if (sprint.getEffectiveStatus() == SprintStatus.CLOSED) {
                    throw new ServiceException(ErrorConstants.SPRINT_NOT_ACTIVE_OR_FUTURE);
                }
                // Also verify sprint belongs to same project
                if (!sprint.getProject().getId().equals(task.getProject().getId())) {
                    throw new ServiceException(ErrorConstants.SPRINT_NOT_IN_PROJECT);
                }
            }
            
            String oldValues = task.getActiveSprints().stream().map(Sprint::getName).collect(Collectors.joining(","));
            String newValues = sprints.stream().map(Sprint::getName).collect(Collectors.joining(","));
            task.getActiveSprints().stream().forEach(sprint -> sprint.removeTask(task));
            task.setActiveSprints(sprints);
            sprints.stream().forEach(sprint -> sprint.addTask(task, user));
            changes.add(new TaskActiveSprintsChange(user, task, oldValues, newValues));
            
            // For USER_STORY: cascade sprint assignment to all subtasks
            if (task.getTaskType() == TaskType.USER_STORY && task.getChildTasks() != null) {
                for (Task subtask : task.getChildTasks()) {
                    // Remove from old sprints
                    subtask.getActiveSprints().stream().forEach(sprint -> sprint.removeTask(subtask));
                    // Assign to new sprints
                    subtask.setActiveSprints(new ArrayList<>(sprints));
                    sprints.stream().forEach(sprint -> sprint.addTask(subtask, user));
                    repo.save(subtask);
                }
            }
            
            // For TASK/BUG with parent: add new sprints to parent USER_STORY
            if (task.getTaskType() != TaskType.USER_STORY && task.getParentTask() != null) {
                Task parentTask = task.getParentTask();
                for (Sprint sprint : sprints) {
                    if (!parentTask.getActiveSprints().contains(sprint)) {
                        parentTask.getActiveSprints().add(sprint);
                        sprint.addTask(parentTask, user);
                    }
                }
                repo.save(parentTask);
            }
        }
        if (editTask.comment != null) {
            // Any project member can add comments
            accessChecker.checkCanAddComment(task, userId);
            Comment comment = editTask.comment.orElseThrow(
                    () -> new ServiceException(ErrorConstants.CAN_NOT_BE_NULL));
            task.addComment(commentService.addComment(comment.getContent(), userService.get(userId), task));
            // Record activity for comment
            activityService.recordActivity(ActivityType.COMMENT_ADDED, user, task.getProject(), task, null);
        }
        if (editTask.pointsReview != null) {
            PointsReview pointsReview = editTask.pointsReview.orElseThrow(
                    () -> new ServiceException(ErrorConstants.CAN_NOT_BE_NULL));
            pointsReviewService.addPointsReview(pointsReview.getPoints(), pointsReview.getComment(), userService.get(userId), task);
        }
        repo.save(task);
        for(TaskChange change: changes) {
            taskChangeService.store(change);
            // Record corresponding activity
            recordActivityForChange(change, user, task);
        }
        return task;
    }

    /**
     * Records an activity for a task change.
     */
    private void recordActivityForChange(TaskChange change, User actor, Task task) {
        ActivityType activityType = null;
        String oldValue = null;
        String newValue = null;
        
        if (change instanceof TaskStatusChange statusChange) {
            activityType = ActivityType.TASK_STATUS_CHANGED;
            oldValue = statusChange.getOldValue();
            newValue = statusChange.getNewValue();
        } else if (change instanceof TaskAssigneeChange assigneeChange) {
            // Determine if it's assign or unassign
            if (assigneeChange.getNewValue() == null) {
                activityType = ActivityType.TASK_UNASSIGNED;
            } else {
                activityType = ActivityType.TASK_ASSIGNED;
            }
            oldValue = assigneeChange.getOldValue();
            newValue = assigneeChange.getNewValue();
        } else if (change instanceof TaskEstimationPointsChange pointsChange) {
            activityType = ActivityType.TASK_ESTIMATION_CHANGED;
            oldValue = pointsChange.getOldValue() != null ? pointsChange.getOldValue().toString() : null;
            newValue = pointsChange.getNewValue() != null ? pointsChange.getNewValue().toString() : null;
        } else if (change instanceof TaskTypeChange typeChange) {
            activityType = ActivityType.TASK_UPDATED;
            oldValue = typeChange.getOldValue();
            newValue = typeChange.getNewValue();
        } else if (change instanceof TaskNameChange nameChange) {
            activityType = ActivityType.TASK_UPDATED;
            oldValue = nameChange.getOldValue();
            newValue = nameChange.getNewValue();
        } else if (change instanceof TaskActiveSprintsChange sprintChange) {
            // Determine if task was added to or removed from sprints
            String oldSprints = sprintChange.getOldValue();
            String newSprints = sprintChange.getNewValue();
            if (newSprints != null && !newSprints.isEmpty() && (oldSprints == null || oldSprints.isEmpty())) {
                activityType = ActivityType.TASK_ADDED_TO_SPRINT;
            } else if ((newSprints == null || newSprints.isEmpty()) && oldSprints != null && !oldSprints.isEmpty()) {
                activityType = ActivityType.TASK_REMOVED_FROM_SPRINT;
            } else {
                activityType = ActivityType.TASK_UPDATED;
            }
            oldValue = oldSprints;
            newValue = newSprints;
        }
        
        if (activityType != null) {
            activityService.recordActivity(activityType, actor, task.getProject(), task, 
                    null, oldValue, newValue);
        }
    }

    /**
     * Internal method to edit a task without authorization checks.
     * For use by data seeders only.
     */
    @Transactional
    public Task editTaskInternal(Long id, MergePatchTask editTask, String userId) {
        Task task = get(id);
        User user = userService.get(userId);
        
        if(editTask.name != null) {
            String name = editTask.name.orElseThrow(
                    () -> new ServiceException(ErrorConstants.CAN_NOT_BE_NULL));
            task.setName(name);
        }
        if(editTask.description != null) {
            task.setDescription(editTask.description.orElse(null));
        }
        if(editTask.reporter != null) {
            if (editTask.reporter.isPresent()) {
                User reporterUser = userService.getByEmail(editTask.reporter.get());
                task.setReporter(reporterUser);
            }
        }
        if(editTask.assignee != null) {
            if(editTask.assignee.isPresent()) {
                User assigneeUser = userService.getByEmail(editTask.assignee.get());
                task.setAssignee(assigneeUser);
            } else {
                task.setAssignee(null);
            }
        }
        if(editTask.estimationPoints != null) {
            Integer points = editTask.estimationPoints.orElse(null);
            task.setEstimationPoints(points);
        }
        if(editTask.status != null) {
            TaskStatus status = editTask.status.orElseThrow(
                    () -> new ServiceException(ErrorConstants.CAN_NOT_BE_NULL));
            task.setStatus(status);
        }
        if(editTask.rank != null) {
            Integer rank = editTask.rank.orElse(null);
            task.setRank(rank);
        }
        if(editTask.activeSprints != null) {
            Collection<Long> sprintIds = editTask.activeSprints.orElse(new ArrayList<>());
            Collection<Sprint> sprints = sprintService.getSprintsByIds(sprintIds);
            task.getActiveSprints().stream().forEach(sprint -> sprint.removeTask(task));
            task.setActiveSprints(sprints);
            sprints.stream().forEach(sprint -> sprint.addTask(task, user));
        }
        repo.save(task);
        return task;
    }


    @Transactional
    public void deleteTask(Long id, String userId) {
        Task task = get(id);
        User user = userService.get(userId);
        // Only task reporter, subject owner (professor), or admin can delete tasks
        accessChecker.checkCanDeleteTask(task, userId);
        
        // Validate delete constraints
        if (task.getTaskType() == TaskType.USER_STORY) {
            // USER_STORY can only be deleted if it has no subtasks
            if (task.getChildTasks() != null && !task.getChildTasks().isEmpty()) {
                throw new ServiceException(ErrorConstants.USER_STORY_HAS_SUBTASKS_CANNOT_DELETE);
            }
        } else {
            // TASK or BUG can only be deleted if status is TODO or INPROGRESS
            if (task.getStatus() != TaskStatus.TODO && task.getStatus() != TaskStatus.INPROGRESS) {
                throw new ServiceException(ErrorConstants.TASK_STATUS_CANNOT_DELETE);
            }
        }
        
        task.getActiveSprints().stream().forEach(sprint -> sprint.removeTask(task));
        task.setActiveSprints(new ArrayList<>());
        if (task.getParentTask() == null){
            Collection<Task> removeTask = task.getChildTasks();
            removeTask.stream().forEach(childTask -> childTask.setParentTask(null));
            removeTask.stream().forEach(childTask -> childTask.getActiveSprints().stream().forEach(sprint -> sprint.removeTask(childTask)));
            repo.deleteAll(removeTask);
        }
        repo.delete(task);
    }

    /**
     * Get all task status values with localized names.
     * Locale is determined from the Accept-Language header.
     */
    public Map<String,String> getListOfStatus() {
        return i18nService.getAllTaskStatusLocalized();
    }

    /**
     * Get User Story status values (BACKLOG to DONE) with localized names.
     * Locale is determined from the Accept-Language header.
     */
    public Map<String,String> getListOfUsStatus() {
        return i18nService.getUsStatusLocalized();
    }

    /**
     * Get subtask status values (TODO, INPROGRESS, VERIFY, DONE) with localized names.
     * Locale is determined from the Accept-Language header.
     */
    public Map<String,String> getListOfTaskStatus() {
        return i18nService.getSubtaskStatusLocalized();
    }

    /**
     * Get all task types with localized names.
     * Locale is determined from the Accept-Language header.
     */
    public Map<String,String> getListOfTypes() {
        return i18nService.getAllTaskTypesLocalized();
    }

    /**
     * Get a task with authorization check.
     */
    public Task getTask(Long taskId, String userId) {
        Task task = get(taskId);
        accessChecker.checkCanViewProject(task.getProject(), userId);
        return task;
    }

    /**
     * Get comments for a task with authorization check.
     */
    public Collection<Comment> getComments(Long taskId, String userId) {
        Task task = get(taskId);
        accessChecker.checkCanViewProject(task.getProject(), userId);
        return commentService.getComments(taskId);
    }

    /**
     * Add a comment to a task with authorization check.
     * Any project member can add comments to tasks in their project.
     */
    @Transactional
    public Comment addComment(Long taskId, String content, String userId) {
        Task task = get(taskId);
        User user = userService.get(userId);
        accessChecker.checkCanAddComment(task, userId);
        Comment comment = commentService.addComment(content, user, task);
        task.addComment(comment);
        repo.save(task);
        return comment;
    }

    /**
     * Get task history with authorization check in a single transaction.
     * This ensures atomicity between the auth check and the data retrieval.
     */
    @Transactional(readOnly = true)
    public List<TaskChange> getTaskHistory(Long taskId, String userId, String search) {
        Task task = get(taskId);
        accessChecker.checkCanViewProject(task.getProject(), userId);
        
        // Simply return the task changes from the relationship
        // The 'search' parameter is ignored for now - can be added later if needed
        return task.getTaskChanges();
    }

    private Collection<TaskChange> updateOtherTasksRank(User user, Integer newRank, Integer currentRank) {
        List<TaskChange> changes = new ArrayList<>();
        List<Task> tasks = new ArrayList<>();
        int increase = 0;
        if(newRank > currentRank) {
            //tasks = repo().findAllBetweenRanksOfBacklog(backlogId, currentRank +1, newRank);
            increase = -1;
        } else if (newRank < currentRank){
            //tasks = repo().findAllBetweenRanksOfBacklog(backlogId, newRank, currentRank -1);
            increase = 1;
        }
        for(Task otherTask : tasks) {
            Integer otherNewRank = otherTask.getRank() + increase;
            otherTask.setRank(otherNewRank);
            //changes.add(new TaskRankChange(user, otherTask, otherNewRank));
        }

        return changes;
    }

    /**
     * Get the latest 5 tasks from all projects the user has access to.
     */
    public List<Task> getRecentTasks(String userId) {
        Collection<Project> accessibleProjects = projectService.getProjectsForUser(userId);
        if (accessibleProjects.isEmpty()) {
            return List.of();
        }
        return repo.findTop5ByProjectInOrderByCreatedAtDesc(accessibleProjects);
    }

    /**
     * Get all tasks from projects the user has access to, paginated with optional filters.
     */
    public org.springframework.data.domain.Page<Task> getMyTasks(
            String userId, 
            int page, 
            int size,
            TaskType type,
            TaskStatus status,
            String assigneeId,
            String sortOrder) {
        Collection<Project> accessibleProjects = projectService.getProjectsForUser(userId);
        if (accessibleProjects.isEmpty()) {
            return org.springframework.data.domain.Page.empty();
        }
        
        // Build sort direction
        org.springframework.data.domain.Sort.Direction direction = 
            "asc".equalsIgnoreCase(sortOrder) 
                ? org.springframework.data.domain.Sort.Direction.ASC 
                : org.springframework.data.domain.Sort.Direction.DESC;
        
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(page, size, 
                org.springframework.data.domain.Sort.by(direction, "createdAt"));
        
        // Build specification with filters
        org.springframework.data.jpa.domain.Specification<Task> spec = 
            (root, query, cb) -> root.get("project").in(accessibleProjects);
        
        if (type != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("type"), type));
        }
        
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        
        if (assigneeId != null && !assigneeId.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("assignee").get("id"), assigneeId));
        }
        
        return repo.findAll(spec, pageable);
    }

    /**
     * Freeze a task (PROFESSOR only)
     */
    @Transactional
    public Task freezeTask(Long taskId, String userId) {
        Task task = get(taskId);
        User user = userService.get(userId);
        
        // Only professors can freeze tasks
        if (!user.isUserType(UserType.PROFESSOR)) {
            throw new ServiceException(ErrorConstants.ONLY_PROFESSOR_CAN_FREEZE_TASK);
        }
        
        // Check if professor has access to this project's course
        accessChecker.checkCanViewProject(task.getProject(), userId);
        
        task.setFrozen(true);
        repo.save(task);
        return task;
    }

    /**
     * Unfreeze a task (PROFESSOR only)
     */
    @Transactional
    public Task unfreezeTask(Long taskId, String userId) {
        Task task = get(taskId);
        User user = userService.get(userId);
        
        // Only professors can unfreeze tasks
        if (!user.isUserType(UserType.PROFESSOR)) {
            throw new ServiceException(ErrorConstants.ONLY_PROFESSOR_CAN_FREEZE_TASK);
        }
        
        // Check if professor has access to this project's course
        accessChecker.checkCanViewProject(task.getProject(), userId);
        
        task.setFrozen(false);
        repo.save(task);
        return task;
    }

    // ==================== Task Attribute Values ====================

    /**
     * Get all attribute values for a task.
     * Also returns attributes from the course profile that don't have values yet.
     */
    public List<TaskAttributeValue> getTaskAttributeValues(Long taskId, String userId) {
        Task task = get(taskId);
        accessChecker.checkCanViewProject(task.getProject(), userId);
        
        return taskAttributeValueRepository.findByTaskId(taskId);
    }

    /**
     * Get available attributes for a task from the course profile.
     * Only returns attributes with target = TASK.
     */
    public List<ProfileAttribute> getAvailableTaskAttributes(Long taskId, String userId) {
        Task task = get(taskId);
        accessChecker.checkCanViewProject(task.getProject(), userId);
        
        // Get the course profile
        Course course = task.getProject().getCourse();
        Profile profile = course.getProfile();
        
        if (profile == null) {
            return Collections.emptyList();
        }
        
        // Return only attributes with target = TASK
        return profile.getAttributes().stream()
                .filter(attr -> attr.getTarget() == AttributeTarget.TASK)
                .collect(Collectors.toList());
    }

    /**
     * Set or update an attribute value for a task.
     * Only professors can set attribute values.
     */
    @Transactional
    public TaskAttributeValue setTaskAttributeValue(Long taskId, Long attributeId, String value, String userId) {
        Task task = get(taskId);
        User user = userService.get(userId);
        
        // Only professors can set attribute values
        if (!user.isUserType(UserType.PROFESSOR)) {
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
        
        accessChecker.checkCanViewProject(task.getProject(), userId);
        
        // Verify the attribute belongs to the task's course profile
        Course course = task.getProject().getCourse();
        Profile profile = course.getProfile();
        
        if (profile == null) {
            throw new ServiceException("No profile is applied to this course");
        }
        
        ProfileAttribute attribute = profileAttributeRepository.findById(attributeId)
                .orElseThrow(() -> new ServiceException("Attribute not found"));
        
        // Verify attribute belongs to the course's profile
        if (!attribute.getProfileId().equals(profile.getId())) {
            throw new ServiceException("Attribute does not belong to the course profile");
        }
        
        // Verify attribute target is TASK
        if (attribute.getTarget() != AttributeTarget.TASK) {
            throw new ServiceException("Attribute is not applicable to tasks");
        }
        
        // Validate value based on type
        validateAttributeValue(attribute, value);
        
        // Find or create the attribute value
        Optional<TaskAttributeValue> existingValue = 
                taskAttributeValueRepository.findByTaskIdAndAttributeId(taskId, attributeId);
        
        TaskAttributeValue attributeValue;
        if (existingValue.isPresent()) {
            attributeValue = existingValue.get();
            attributeValue.setValue(value);
        } else {
            attributeValue = new TaskAttributeValue(task, attribute, value);
        }
        
        return taskAttributeValueRepository.save(attributeValue);
    }

    /**
     * Delete an attribute value from a task.
     * Only professors can delete attribute values.
     */
    @Transactional
    public void deleteTaskAttributeValue(Long taskId, Long attributeId, String userId) {
        Task task = get(taskId);
        User user = userService.get(userId);
        
        // Only professors can delete attribute values
        if (!user.isUserType(UserType.PROFESSOR)) {
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
        
        accessChecker.checkCanViewProject(task.getProject(), userId);
        
        taskAttributeValueRepository.deleteByTaskIdAndAttributeId(taskId, attributeId);
    }

    /**
     * Validate the value based on attribute type
     */
    private void validateAttributeValue(ProfileAttribute attribute, String value) {
        if (value == null || value.isBlank()) {
            return; // Allow empty values
        }
        
        switch (attribute.getType()) {
            case INTEGER:
                try {
                    Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new ServiceException("Invalid integer value");
                }
                break;
            case FLOAT:
                try {
                    Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    throw new ServiceException("Invalid decimal value");
                }
                break;
            case ENUM:
                if (attribute.getEnumRef() != null) {
                    List<String> allowedValues = attribute.getEnumRef().getValues();
                    if (!allowedValues.contains(value)) {
                        throw new ServiceException("Invalid enum value. Allowed values: " + String.join(", ", allowedValues));
                    }
                }
                break;
            case STRING:
            default:
                // No validation needed for strings
                break;
        }
    }
}
