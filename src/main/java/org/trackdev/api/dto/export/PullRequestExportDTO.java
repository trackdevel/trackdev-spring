package org.trackdev.api.dto.export;

import lombok.Data;
import org.trackdev.api.dto.PullRequestAttributeValueDTO;
import org.trackdev.api.dto.PullRequestDTO;

import java.util.List;

@Data
public class PullRequestExportDTO {
    private PullRequestDTO pullRequest;
    private List<TaskRef> tasks;
    private List<PullRequestAttributeValueDTO> attributeValues;

    @Data
    public static class TaskRef {
        private Long id;
        private String taskKey;
    }
}
