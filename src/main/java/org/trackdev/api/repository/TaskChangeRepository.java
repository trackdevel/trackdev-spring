package org.trackdev.api.repository;

import org.springframework.stereotype.Repository;
import org.trackdev.api.entity.taskchanges.TaskChange;

@Repository
public interface TaskChangeRepository extends BaseRepositoryLong<TaskChange> {
}
