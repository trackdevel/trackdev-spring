package org.trackdev.api.dto;

import lombok.Data;

import java.util.List;

/**
 * DTO for detailed Pull Request analysis with file-level information
 */
@Data
public class PRDetailedAnalysisDTO {
    private String id;
    private String url;
    private Integer prNumber;
    private String title;
    private String state;
    private Boolean merged;
    private String repoFullName;
    private UserSummaryDTO author;
    private Integer additions;
    private Integer deletions;
    private Integer changedFiles;
    private Integer survivingLines;
    private List<PRFileDetailDTO> files;
}
