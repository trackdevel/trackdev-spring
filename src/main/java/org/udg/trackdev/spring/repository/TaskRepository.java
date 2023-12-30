package org.udg.trackdev.spring.repository;

import org.springframework.stereotype.Repository;
import org.udg.trackdev.spring.entity.Task;

@Repository
public interface TaskRepository extends BaseRepositoryLong<Task> {
}
