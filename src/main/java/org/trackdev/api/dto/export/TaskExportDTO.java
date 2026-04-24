package org.trackdev.api.dto.export;

import lombok.Data;
import org.trackdev.api.dto.TaskAttributeValueDTO;
import org.trackdev.api.dto.TaskBasicDTO;

import java.util.List;

@Data
public class TaskExportDTO {
    private TaskBasicDTO task;
    private List<TaskAttributeValueDTO> attributeValues;
}
