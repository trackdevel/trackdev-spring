package org.trackdev.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for tasks list response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TasksResponseDTO {
    private List<TaskBasicDTO> tasks;
}
