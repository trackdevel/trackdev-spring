package org.trackdev.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.trackdev.api.entity.Activity;
import org.trackdev.api.entity.Project;
import org.trackdev.api.entity.Sprint;
import org.trackdev.api.entity.User;

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
     * Find activities for a specific project and actor
     */
    Page<Activity> findByProjectAndActorOrderByCreatedAtDesc(Project project, User actor, Pageable pageable);

    /**
     * Find activities for a specific project and sprint (via task.activeSprints collection)
     */
    @Query("SELECT a FROM Activity a WHERE a.project = :project AND :sprint MEMBER OF a.task.activeSprints ORDER BY a.createdAt DESC")
    Page<Activity> findByProjectAndSprintOrderByCreatedAtDesc(@Param("project") Project project, @Param("sprint") Sprint sprint, Pageable pageable);

    /**
     * Find activities for a specific project, sprint, and actor
     */
    @Query("SELECT a FROM Activity a WHERE a.project = :project AND :sprint MEMBER OF a.task.activeSprints AND a.actor = :actor ORDER BY a.createdAt DESC")
    Page<Activity> findByProjectAndSprintAndActorOrderByCreatedAtDesc(@Param("project") Project project, @Param("sprint") Sprint sprint, @Param("actor") User actor, Pageable pageable);

    /**
     * Delete all activities for a specific project
     */
    void deleteByProject(Project project);
}
