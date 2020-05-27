package org.udg.trackdev.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.entity.TaskLog;

@Component
public interface TaskLogRepository extends JpaRepository<TaskLog, Long> {
}
