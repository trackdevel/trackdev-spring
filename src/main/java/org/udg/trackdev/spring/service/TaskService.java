package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.entity.Backlog;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.entity.TaskNameChange;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.repository.TaskRepository;

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
    public Task editTask(Long id, String name, String userId) {
        Task task = get(id);
        User user = userService.get(userId);
        accessChecker.checkCanManageBacklog(task.getBacklog(), user);
        if(name != null) {
            task.setName(name);
        }
        repo.save(task);
        TaskNameChange change = new TaskNameChange(user, task, name);
        taskChangeService.store(change);

        return task;
    }
}
