package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.*;
import org.udg.trackdev.spring.entity.taskchanges.TaskAssigneeChange;
import org.udg.trackdev.spring.entity.taskchanges.TaskChange;
import org.udg.trackdev.spring.entity.taskchanges.TaskEstimationPointsChange;
import org.udg.trackdev.spring.entity.taskchanges.TaskNameChange;
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
    public Task editTask(Long id, String name, String assignee, Integer estimationPoints, String userId) {
        Task task = get(id);
        User user = userService.get(userId);
        accessChecker.checkCanManageBacklog(task.getBacklog(), user);

        List<TaskChange> changes = new ArrayList<>();
        if(name != null) {
            task.setName(name);
            changes.add(new TaskNameChange(user, task, name));
        }
        if(assignee != null) {
            User assigneeUser = userService.getByUsername(assignee);
            if(!task.getBacklog().getGroup().isMember(assigneeUser)) {
                throw new ServiceException("Assignee is not in the list of possible assignees");
            }
            task.setAssignee(assigneeUser);
            changes.add(new TaskAssigneeChange(user, task, assigneeUser));
        }
        if(estimationPoints != null) {
            task.setEstimationPoints(estimationPoints);
            changes.add(new TaskEstimationPointsChange(user, task, estimationPoints));
        }
        repo.save(task);
        for(TaskChange change: changes) {
            taskChangeService.store(change);
        }
        return task;
    }
}
