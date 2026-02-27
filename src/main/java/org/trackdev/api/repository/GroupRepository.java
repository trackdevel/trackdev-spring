package org.trackdev.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.trackdev.api.entity.Project;

import java.util.List;

@Component
public interface GroupRepository extends BaseRepositoryLong<Project> {

    boolean existsBySlug(String slug);

    List<Project> findByCourseIdOrderByNameAsc(Long courseId);

    Page<Project> findAllByOrderByNameAsc(Pageable pageable);

    Page<Project> findByCourseIdOrderByNameAsc(Long courseId, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN p.members m LEFT JOIN p.course c LEFT JOIN c.subject s " +
           "WHERE m.id = :userId OR c.owner.id = :userId OR s.owner.id = :userId " +
           "ORDER BY p.name ASC")
    Page<Project> findProjectsForUser(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN p.members m LEFT JOIN p.course c LEFT JOIN c.subject s " +
           "WHERE (m.id = :userId OR c.owner.id = :userId OR s.owner.id = :userId) " +
           "AND c.id = :courseId " +
           "ORDER BY p.name ASC")
    Page<Project> findProjectsForUserByCourse(@Param("userId") String userId, @Param("courseId") Long courseId, Pageable pageable);
}
