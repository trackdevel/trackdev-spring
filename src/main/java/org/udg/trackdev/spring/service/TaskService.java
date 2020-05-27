package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.entity.Backlog;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.repository.TaskRepository;

@Service
public class TaskService extends BaseService<Task, TaskRepository> {

    @Autowired
    BacklogService backlogService;

    @Transactional
    public Task create(String name, Long backlogId) {
        Backlog backlog = backlogService.get(backlogId);
        Task task = new Task(name);
        task.setBacklog(backlog);
        this.repo.save(task);
        return task;
    }

}
