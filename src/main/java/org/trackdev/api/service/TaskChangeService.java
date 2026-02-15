package org.trackdev.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.entity.Task;
import org.trackdev.api.entity.taskchanges.TaskChange;
import org.trackdev.api.repository.TaskChangeRepository;

@Service
public class TaskChangeService extends BaseServiceLong<TaskChange, TaskChangeRepository> {

    public void store(TaskChange taskChange) {
        repo().save(taskChange);
    }

    /**
     * Delete all task change records for a specific task (used when task is deleted).
     */
    @Transactional
    public void deleteByTask(Task task) {
        repo().deleteByTask(task);
    }
}
