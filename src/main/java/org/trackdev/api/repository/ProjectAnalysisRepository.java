package org.trackdev.api.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.trackdev.api.entity.ProjectAnalysis;

import java.util.List;
import java.util.Optional;

public interface ProjectAnalysisRepository extends BaseRepositoryUUID<ProjectAnalysis> {

    /**
     * Find all analyses for a project, ordered by most recent first
     */
    List<ProjectAnalysis> findByProjectIdOrderByStartedAtDesc(Long projectId);

    /**
     * Find the most recent analysis for a project
     */
    Optional<ProjectAnalysis> findFirstByProjectIdOrderByStartedAtDesc(Long projectId);

    /**
     * Check if there's an in-progress analysis for a project
     */
    @Query("SELECT COUNT(a) > 0 FROM ProjectAnalysis a WHERE a.project.id = :projectId AND a.status = 'IN_PROGRESS'")
    boolean existsInProgressByProjectId(@Param("projectId") Long projectId);

    /**
     * Find in-progress analysis for a project
     */
    Optional<ProjectAnalysis> findByProjectIdAndStatus(Long projectId, ProjectAnalysis.AnalysisStatus status);
}
