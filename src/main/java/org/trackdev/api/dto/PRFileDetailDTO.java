package org.trackdev.api.dto;

import lombok.Data;

import java.util.List;

/**
 * DTO for detailed file information in a Pull Request.
 * Lines are provided in display order, interleaving current and deleted lines.
 */
@Data
public class PRFileDetailDTO {
    private String filePath;
    private String status; // added, modified, deleted, renamed
    private Integer additions;
    private Integer deletions;
    private Integer survivingLines;
    private Integer deletedLines;
    private Integer currentLines; // Total lines in current file
    private List<LineDetailDTO> lines; // All lines in display order (current + deleted interleaved)
    
    // Internal use only - stores the diff patch for parsing added lines
    private transient String patch;
    
    /**
     * Represents a single line with its content and status.
     * Lines are ordered for display: current file lines with deleted lines interleaved.
     */
    @Data
    public static class LineDetailDTO {
        private Integer lineNumber;      // Current line number (null for deleted lines)
        private Integer originalLineNumber; // Line number in the merge commit (for deleted lines)
        private String content;
        private LineStatus status;
        private String commitSha;
        private String commitUrl;         // URL to the commit
        private String authorFullName;    // Full name of the author (from app user matched by GitHub username)
        private String authorGithubUsername; // GitHub username of the commit author
        private String prFileUrl;         // URL to the file in the PR being analyzed
        private Integer originPrNumber;   // PR number that originally introduced this line (for CURRENT lines)
        private String originPrUrl;       // URL to the PR that originally introduced this line
        
        public LineDetailDTO() {}
        
        public LineDetailDTO(Integer lineNumber, Integer originalLineNumber, String content, LineStatus status, String commitSha) {
            this.lineNumber = lineNumber;
            this.originalLineNumber = originalLineNumber;
            this.content = content;
            this.status = status;
            this.commitSha = commitSha;
        }
        
        public LineDetailDTO(Integer lineNumber, Integer originalLineNumber, String content, LineStatus status, 
                             String commitSha, String commitUrl, String authorFullName, String authorGithubUsername, 
                             String prFileUrl, Integer originPrNumber, String originPrUrl) {
            this.lineNumber = lineNumber;
            this.originalLineNumber = originalLineNumber;
            this.content = content;
            this.status = status;
            this.commitSha = commitSha;
            this.commitUrl = commitUrl;
            this.authorFullName = authorFullName;
            this.authorGithubUsername = authorGithubUsername;
            this.prFileUrl = prFileUrl;
            this.originPrNumber = originPrNumber;
            this.originPrUrl = originPrUrl;
        }
    }
    
    /**
     * Status of a line
     */
    public enum LineStatus {
        SURVIVING,  // Current line that came from the PR (still exists)
        CURRENT,    // Current line that is NOT from the PR (context/other commits)
        DELETED     // Line from PR that was modified or deleted since merge
    }
}
