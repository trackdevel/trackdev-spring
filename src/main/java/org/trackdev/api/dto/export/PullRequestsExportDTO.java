package org.trackdev.api.dto.export;

import lombok.Data;

import java.util.List;

@Data
public class PullRequestsExportDTO {
    private Long projectId;
    private List<PullRequestExportDTO> pullRequests;
}
