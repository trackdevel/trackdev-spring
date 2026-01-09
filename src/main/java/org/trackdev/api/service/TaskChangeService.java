package org.trackdev.api.service;

import org.springframework.stereotype.Service;
import org.trackdev.api.entity.taskchanges.TaskChange;
import org.trackdev.api.repository.TaskChangeRepository;

@Service
public class TaskChangeService extends BaseServiceLong<TaskChange, TaskChangeRepository> {

    public void store(TaskChange taskChange) {
        repo().save(taskChange);
    }
}
