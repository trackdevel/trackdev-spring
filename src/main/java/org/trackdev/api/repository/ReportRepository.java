package org.trackdev.api.repository;

import org.springframework.stereotype.Repository;
import org.trackdev.api.entity.Report;

import java.util.List;

@Repository
public interface ReportRepository extends BaseRepositoryLong<Report> {
    
    List<Report> findByCourseId(Long courseId);
}
