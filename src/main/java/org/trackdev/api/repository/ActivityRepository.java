package org.trackdev.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.trackdev.api.entity.Activity;
import org.trackdev.api.entity.Project;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface ActivityRepository extends BaseRepositoryLong<Activity> {

    /**
     * Find all activities for a collection of projects, ordered by creation date desc
     */
    Page<Activity> findByProjectInOrderByCreatedAtDesc(Collection<Project> projects, Pageable pageable);

    /**
     * Find all activities for a collection of projects
     */
    List<Activity> findByProjectInOrderByCreatedAtDesc(Collection<Project> projects);

    /**
     * Find activities for projects created after a specific timestamp
     */
    List<Activity> findByProjectInAndCreatedAtAfterOrderByCreatedAtDesc(
        Collection<Project> projects, 
        ZonedDateTime after
    );

    /**
     * Count activities for projects created after a specific timestamp
     */
    long countByProjectInAndCreatedAtAfter(Collection<Project> projects, ZonedDateTime after);

    /**
     * Find activities for a specific project
     */
    Page<Activity> findByProjectOrderByCreatedAtDesc(Project project, Pageable pageable);

    /**
     * Delete all activities for a specific project
     */
    void deleteByProject(Project project);
}
