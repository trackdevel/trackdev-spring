package org.trackdev.api.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.trackdev.api.entity.ProjectAnalysisFile;

import java.util.List;

public interface ProjectAnalysisFileRepository extends BaseRepositoryUUID<ProjectAnalysisFile> {

    /**
     * Find all files for an analysis
     */
    List<ProjectAnalysisFile> findByAnalysisId(String analysisId);

    /**
     * Find files for an analysis filtered by sprint
     */
    List<ProjectAnalysisFile> findByAnalysisIdAndSprintId(String analysisId, Long sprintId);

    /**
     * Find files for an analysis filtered by author
     */
    List<ProjectAnalysisFile> findByAnalysisIdAndAuthorId(String analysisId, String authorId);

    /**
     * Find files for an analysis filtered by sprint and author
     */
    List<ProjectAnalysisFile> findByAnalysisIdAndSprintIdAndAuthorId(String analysisId, Long sprintId, String authorId);

    /**
     * Get summary statistics grouped by author for an analysis
     */
    @Query("SELECT f.author.id, f.author.fullName, f.author.username, " +
           "SUM(f.survivingLines), SUM(f.deletedLines), COUNT(f) " +
           "FROM ProjectAnalysisFile f " +
           "WHERE f.analysis.id = :analysisId " +
           "GROUP BY f.author.id, f.author.fullName, f.author.username")
    List<Object[]> getSummaryByAuthor(@Param("analysisId") String analysisId);

    /**
     * Get summary statistics grouped by author for an analysis, filtered by sprint
     */
    @Query("SELECT f.author.id, f.author.fullName, f.author.username, " +
           "SUM(f.survivingLines), SUM(f.deletedLines), COUNT(f) " +
           "FROM ProjectAnalysisFile f " +
           "WHERE f.analysis.id = :analysisId AND f.sprint.id = :sprintId " +
           "GROUP BY f.author.id, f.author.fullName, f.author.username")
    List<Object[]> getSummaryByAuthorAndSprint(@Param("analysisId") String analysisId, @Param("sprintId") Long sprintId);

    /**
     * Get summary statistics grouped by sprint for an analysis
     */
    @Query("SELECT f.sprint.id, f.sprint.name, " +
           "SUM(f.survivingLines), SUM(f.deletedLines), COUNT(f) " +
           "FROM ProjectAnalysisFile f " +
           "WHERE f.analysis.id = :analysisId " +
           "GROUP BY f.sprint.id, f.sprint.name")
    List<Object[]> getSummaryBySprint(@Param("analysisId") String analysisId);

    /**
     * Delete all files for an analysis (for cleanup/re-run)
     */
    void deleteByAnalysisId(String analysisId);

    /**
     * Find files by analysis and PR
     */
    List<ProjectAnalysisFile> findByAnalysisIdAndPullRequestId(String analysisId, String prId);
}
