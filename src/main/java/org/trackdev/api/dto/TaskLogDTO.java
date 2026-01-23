package org.trackdev.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for task change history logs
 * Matches the TaskLog interface expected by the frontend
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskLogDTO {
    private Long id;
    private Long taskId;
    private String field;
    private String oldValue;
    private String newValue;
    private String timestamp;
    private String userId;
    private String username;
    private String fullName;
}
