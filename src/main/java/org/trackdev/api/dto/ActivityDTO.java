package org.trackdev.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for Activity events
 */
@Data
public class ActivityDTO {
    private Long id;
    private String type;
    private LocalDateTime createdAt;
    private String message;
    private String oldValue;
    private String newValue;
    
    // Actor info
    private String actorId;
    private String actorUsername;
    private String actorEmail;
    
    // Project info
    private Long projectId;
    private String projectName;
    
    // Task info (optional)
    private Long taskId;
    private String taskKey;
    private String taskName;
}
