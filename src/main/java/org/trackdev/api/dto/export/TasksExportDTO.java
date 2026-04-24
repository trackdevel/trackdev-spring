package org.trackdev.api.dto.export;

import lombok.Data;

import java.util.List;

@Data
public class TasksExportDTO {
    private Long projectId;
    private List<TaskExportDTO> tasks;
}
