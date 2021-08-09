package org.udg.trackdev.spring.repository;

import org.springframework.stereotype.Repository;
import org.udg.trackdev.spring.entity.taskchanges.TaskChange;

@Repository
public interface TaskChangeRepository extends BaseRepositoryLong<TaskChange> {
}
