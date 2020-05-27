package org.udg.trackdev.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.udg.trackdev.spring.entity.TaskLog;
import org.udg.trackdev.spring.entity.WorkLog;

@Component
public interface WorkLogRepository extends JpaRepository<WorkLog, Long> {
}
