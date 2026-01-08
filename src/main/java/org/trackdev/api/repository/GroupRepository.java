package org.trackdev.api.repository;

import org.springframework.stereotype.Component;
import org.trackdev.api.entity.Project;

@Component
public interface GroupRepository extends BaseRepositoryLong<Project> {
    
    boolean existsBySlug(String slug);
}
