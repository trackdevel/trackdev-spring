package org.trackdev.api.repository;

import org.springframework.stereotype.Component;
import org.trackdev.api.entity.SprintPattern;

import java.util.List;

@Component
public interface SprintPatternRepository extends BaseRepositoryLong<SprintPattern> {

    List<SprintPattern> findByCourse_IdOrderByName(Long courseId);
}
