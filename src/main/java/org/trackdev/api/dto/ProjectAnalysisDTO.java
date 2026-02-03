package org.trackdev.api.dto;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * DTO for project analysis status and results
 */
@Data
public class ProjectAnalysisDTO {
    
    private String id;
    private Long projectId;
    private String projectName;
    private String status; // IN_PROGRESS, DONE, FAILED
    private ZonedDateTime startedAt;
    private ZonedDateTime completedAt;
    private String startedByName;
    private String startedById;
    
    // Progress tracking
    private Integer totalPrs;
    private Integer processedPrs;
    private Integer progressPercent;
    
    // Summary statistics
    private Integer totalFiles;
    private Integer totalSurvivingLines;
    private Integer totalDeletedLines;
    private Double survivalRate;
    
    private String errorMessage;
    
    /**
     * Summary by author
     */
    @Data
    public static class AuthorSummaryDTO {
        private String authorId;
        private String authorName;
        private String authorUsername;
        private Integer survivingLines;
        private Integer deletedLines;
        private Integer fileCount;
        private Double survivalRate;
    }
    
    /**
     * Summary by sprint
     */
    @Data
    public static class SprintSummaryDTO {
        private Long sprintId;
        private String sprintName;
        private Integer survivingLines;
        private Integer deletedLines;
        private Integer fileCount;
        private Double survivalRate;
    }
    
    /**
     * File detail in analysis results
     */
    @Data
    public static class FileDTO {
        private String id;
        private String prId;
        private Integer prNumber;
        private String prTitle;
        private Long taskId;
        private String taskName;
        private Long sprintId;
        private String sprintName;
        private String authorId;
        private String authorName;
        private String filePath;
        private String status;
        private Integer additions;
        private Integer deletions;
        private Integer survivingLines;
        private Integer deletedLines;
        private Integer currentLines;
        private Double survivalRate;
    }
    
    /**
     * Full results with summaries and file list
     */
    @Data
    public static class ResultsDTO {
        private ProjectAnalysisDTO analysis;
        private List<AuthorSummaryDTO> authorSummaries;
        private List<SprintSummaryDTO> sprintSummaries;
        private List<FileDTO> files;
    }
}
