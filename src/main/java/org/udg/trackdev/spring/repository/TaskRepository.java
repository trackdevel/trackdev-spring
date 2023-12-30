package org.udg.trackdev.spring.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.udg.trackdev.spring.entity.Task;

import java.util.List;

@Repository
public interface TaskRepository extends BaseRepositoryLong<Task> {
}
