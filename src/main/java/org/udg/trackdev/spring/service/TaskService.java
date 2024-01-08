package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.*;
import org.udg.trackdev.spring.entity.taskchanges.*;
import org.udg.trackdev.spring.model.MergePatchTask;
import org.udg.trackdev.spring.repository.TaskRepository;

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

    @Transactional
    public Task createTask(Long projectId, String name, String userId) {
        Project project = projectService.get(projectId);
        User user = userService.get(userId);
        accessChecker.checkCanViewProject(project, userId);
        Task task = new Task(name, user);
        task.setType(TaskType.USER_STORY);
        task.setProject(project);
        project.addTask(task);
        this.repo.save(task);

        return task;
    }

    @Transactional
    public Task createSubTask(Long taskId, String name, String userId) {
        Task parentTask = this.get(taskId);
        User user = userService.get(userId);
        accessChecker.checkCanViewProject(parentTask.getProject(), userId);
        Task subtask = new Task(name, user);
        subtask.setProject(parentTask.getProject());
        subtask.setType(TaskType.TASK);
        subtask.setParentTask(parentTask);
        parentTask.addChildTask(subtask);
        this.repo.save(subtask);

        return subtask;
    }

    @Transactional
    public Task editTask(Long id, MergePatchTask editTask, String userId) {
        Task task = get(id);
        User user = userService.get(userId);
        accessChecker.checkCanViewProject(task.getProject(), userId);

        List<TaskChange> changes = new ArrayList<>();
        if(editTask.name != null) {
            String oldName = task.getName();
            String name = editTask.name.orElseThrow(
                    () -> new ServiceException("Not possible to set name to null"));
            task.setName(name);
            changes.add(new TaskNameChange(user.getEmail(), task.getId(), oldName, name));
        }
        if(editTask.description != null) {
            task.setDescription(editTask.description.orElse(null));
        }
        if(editTask.reporter != null) {
            User reporterUser = null;
            if (editTask.reporter.isPresent()) {
                reporterUser = userService.getByEmail(editTask.reporter.get());
                if (!task.getProject().isMember(reporterUser)) {
                    throw new ServiceException("Assignee is not in the list of possible assignees");
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
                    throw new ServiceException("Assignee is not in the list of possible assignees");
                }
                task.setAssignee(assigneeUser);
            } else {
                task.setAssignee(null);
            }
            changes.add(new TaskAssigneeChange(user.getEmail(), task.getId(), oldValue, task.getAssignee().getUsername()));
        }
        if(editTask.estimationPoints != null) {
            Integer oldPoints = task.getEstimationPoints();
            Integer points = editTask.estimationPoints.orElse(null);
            task.setEstimationPoints(points);
            changes.add(new TaskEstimationPointsChange(user.getEmail(), task.getId(), oldPoints, points));
        }
        if(editTask.status != null) {
            TaskStatus status = editTask.status.orElseThrow(
                    () -> new ServiceException("Not possible to set status to null"));
            TaskStatus oldStatus = task.getStatus();
            task.setStatus(status);
            changes.add(new TaskStatusChange(user.getEmail(), task.getId(), oldStatus.name(),  status.name()));
        }
        if(editTask.rank != null) {
            Integer newRank = editTask.rank.orElseThrow(
                    () -> new ServiceException("Not possible to set rank to null"));
            Integer currentRank = task.getRank();
            if(!Objects.equals(newRank, currentRank)) {
                //Collection<TaskChange> otherChanges = updateOtherTasksRank(user, newRank, currentRank);
                changes.add(new TaskRankChange(user.getEmail(), task.getId(), task.getRank(), newRank));
                task.setRank(newRank);
                //changes.addAll(otherChanges);
            }
        }
        if(editTask.activeSprints != null){
            Collection<Long> sprintsIds = editTask.activeSprints.orElseThrow(
                    () -> new ServiceException("Not possible to set activeSprints to null"));
            String oldValues = task.getActiveSprints().stream().map(Sprint::getName).collect(Collectors.joining(","));
            Collection<Sprint> sprints = sprintService.getSpritnsByIds(sprintsIds);
            String newValues = sprints.stream().map(Sprint::getName).collect(Collectors.joining(","));
            task.getActiveSprints().stream().forEach(sprint -> sprint.removeTask(task));
            task.setActiveSprints(sprints);
            sprints.stream().forEach(sprint -> sprint.addTask(task, user));
            changes.add(new TaskActiveSprintsChange(user.getEmail(), task.getId(), oldValues, newValues));
        }
        if (editTask.comment != null) {
            Comment comment = editTask.comment.orElseThrow(
                    () -> new ServiceException("Not possible to set discussion to null"));
            task.addComment(commentService.addComment(comment.getContent(), userService.get(userId), task));
        }
        if (editTask.pointsReview != null) {
            PointsReview pointsReview = editTask.pointsReview.orElseThrow(
                    () -> new ServiceException("Not possible to set pointsReview to null"));
            pointsReviewService.addPointsReview(pointsReview.getPoints(), pointsReview.getComment(), userService.get(userId), task);
        }
        repo.save(task);
        for(TaskChange change: changes) {
            taskChangeService.store(change);
        }
        return task;
    }


    @Transactional
    public void deleteTask(Long id, String userId) {
        Task task = get(id);
        User user = userService.get(userId);
        accessChecker.checkCanViewProject(task.getProject(), userId);
        task.getActiveSprints().stream().forEach(sprint -> sprint.removeTask(task));
        task.setActiveSprints(new ArrayList<>());
        if (task.getParentTask() == null){
            repo.deleteAll(task.getChildTasks());
        }
        repo.delete(task);
    }

    public Map<String,String> getListOfStatus() {
        Map<String,String> status = new HashMap<>();
        for (TaskStatus taskStatus : TaskStatus.values()) {
            status.put(taskStatus.name(), taskStatus.toString());
        }
        return status;
    }

    public Map<String,String> getListOfUsStatus() {
        int i_start = TaskStatus.BACKLOG.ordinal();
        int i_end = TaskStatus.DONE.ordinal();
        Map<String,String> status = new HashMap<>();
        for (int i = i_start; i <= i_end; i++ ) {
            TaskStatus taskStatus = TaskStatus.values()[i];
            status.put(taskStatus.name(), taskStatus.toString());
        }
        return status;
    }

    public Map<String,String> getListOfTaskStatus() {
        Map<String,String> status = new HashMap<>();
        status.put(TaskStatus.DEFINED.name(), TaskStatus.DEFINED.toString());
        status.put(TaskStatus.INPROGRESS.name(), TaskStatus.INPROGRESS.toString());
        status.put(TaskStatus.DONE.name(), TaskStatus.DONE.toString());
        return status;
    }

    public Map<String,String> getListOfTypes() {
        Map<String,String> types = new HashMap<>();
        for (TaskType taskType : TaskType.values()) {
            types.put(taskType.name(),taskType.toString());
        }
        return types;
    }

    public Collection<Comment> getComments(Long taskId) {
        return commentService.getComments(taskId);
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
}
