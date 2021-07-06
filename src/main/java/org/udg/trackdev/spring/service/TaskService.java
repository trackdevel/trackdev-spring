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
public class TaskService extends BaseService<Task, TaskRepository> {

    @Autowired
    BacklogService backlogService;

    @Autowired
    UserService userService;

    @Transactional
    public Task createTask(Long backlogId, String name, String userId) {
        Backlog backlog = backlogService.get(backlogId);
        User user = userService.get(userId);
        checkCanManageBacklog(backlog, user);
        Task task = new Task(name, user);
        task.setBacklog(backlog);
        this.repo.save(task);
        return task;
    }

    private void checkCanManageBacklog(Backlog backlog, User user) {
        Group group = backlog.getGroup();
        if(!group.isMember(user)) {
            throw new ServiceException("User cannot manage this backlog");
        }
    }
}
