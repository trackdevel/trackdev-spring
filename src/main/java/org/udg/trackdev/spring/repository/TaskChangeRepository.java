package org.udg.trackdev.spring.repository;

import org.springframework.stereotype.Repository;
import org.udg.trackdev.spring.entity.TaskChange;

import java.util.List;

@Repository
public interface TaskChangeRepository extends BaseRepositoryLong<TaskChange> {

    List<TaskChange> findByTaskId(Long taskId);
}
