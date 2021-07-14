package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.*;
import org.udg.trackdev.spring.entity.taskchanges.*;
import org.udg.trackdev.spring.model.MergePatchTask;
import org.udg.trackdev.spring.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService extends BaseServiceLong<Task, TaskRepository> {

    @Autowired
    BacklogService backlogService;

    @Autowired
    UserService userService;

    @Autowired
    TaskChangeService taskChangeService;

    @Autowired
    AccessChecker accessChecker;

    @Transactional
    public Task createTask(Long backlogId, String name, String userId) {
        Backlog backlog = backlogService.get(backlogId);
        User user = userService.get(userId);
        accessChecker.checkCanManageBacklog(backlog, user);
        Task task = new Task(name, user);
        task.setBacklog(backlog);
        this.repo.save(task);
        return task;
    }

    @Transactional
    public Task editTask(Long id, MergePatchTask editTask, String userId) {
        Task task = get(id);
        User user = userService.get(userId);
        accessChecker.checkCanManageBacklog(task.getBacklog(), user);

        List<TaskChange> changes = new ArrayList<>();
        if(editTask.name != null) {
            String name = editTask.name.orElseThrow(
                    () -> new ServiceException("Not possible to set name to null"));
            task.setName(name);
            changes.add(new TaskNameChange(user, task, name));
        }
        if(editTask.assignee != null) {
            User assigneeUser = null;
            if(editTask.assignee.isPresent()) {
                assigneeUser = userService.getByUsername(editTask.assignee.get());
                if(!task.getBacklog().getGroup().isMember(assigneeUser)) {
                    throw new ServiceException("Assignee is not in the list of possible assignees");
                }
                task.setAssignee(assigneeUser);
            } else {
                task.setAssignee(null);
            }
            changes.add(new TaskAssigneeChange(user, task, assigneeUser));
        }
        if(editTask.estimationPoints != null) {
            Integer points = editTask.estimationPoints.orElse(null);
            task.setEstimationPoints(points);
            changes.add(new TaskEstimationPointsChange(user, task, points));
        }
        if(editTask.status != null) {
            TaskStatus status = editTask.status.orElseThrow(
                    () -> new ServiceException("Not possible to set status to null"));
            task.setStatus(status);
            changes.add(new TaskStatusChange(user, task, status));
        }
        repo.save(task);
        for(TaskChange change: changes) {
            taskChangeService.store(change);
        }
        return task;
    }
}
