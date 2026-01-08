package org.trackdev.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.trackdev.api.entity.WorkLog;

@Component
public interface WorkLogRepository extends JpaRepository<WorkLog, Long> {
}
