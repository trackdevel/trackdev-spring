package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.Backlog;
import org.udg.trackdev.spring.entity.Group;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.repository.TaskRepository;

@Service
public class TaskService extends BaseServiceLong<Task, TaskRepository> {

    @Autowired
    BacklogService backlogService;

    @Autowired
    UserService userService;

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
}
