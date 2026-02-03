package org.trackdev.api.repository;

import org.trackdev.api.entity.ProjectAnalysisFileLine;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectAnalysisFileLineRepository extends BaseRepositoryUUID<ProjectAnalysisFileLine> {

    /**
     * Find all lines for a specific file, ordered by display order
     */
    List<ProjectAnalysisFileLine> findByFileIdOrderByDisplayOrderAsc(String fileId);

    /**
     * Find all lines for files in a specific analysis and PR
     */
    @Query("SELECT l FROM ProjectAnalysisFileLine l " +
           "JOIN l.file f " +
           "WHERE f.analysis.id = :analysisId AND f.pullRequest.id = :prId " +
           "ORDER BY f.filePath, l.displayOrder")
    List<ProjectAnalysisFileLine> findByAnalysisIdAndPrId(
            @Param("analysisId") String analysisId, 
            @Param("prId") String prId);

    /**
     * Delete all lines for files in a specific analysis
     */
    @Query("DELETE FROM ProjectAnalysisFileLine l WHERE l.file.id IN " +
           "(SELECT f.id FROM ProjectAnalysisFile f WHERE f.analysis.id = :analysisId)")
    void deleteByAnalysisId(@Param("analysisId") String analysisId);
}
