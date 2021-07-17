package org.udg.trackdev.spring.service;

import org.springframework.stereotype.Service;
import org.udg.trackdev.spring.entity.taskchanges.TaskChange;
import org.udg.trackdev.spring.repository.TaskChangeRepository;

import java.util.List;

@Service
public class TaskChangeService extends BaseServiceLong<TaskChange, TaskChangeRepository> {

    public void store(TaskChange taskChange) {
        repo().save(taskChange);
    }

    public List<TaskChange> findTaskChangesByTask(Long id)  {
        return this.repo.findByTaskId(id);
    }
}
