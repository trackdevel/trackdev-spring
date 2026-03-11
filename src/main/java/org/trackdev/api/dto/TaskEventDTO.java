package org.trackdev.api.dto;

import lombok.Data;

@Data
public class TaskEventDTO {
    private String eventType;
    private Long taskId;
    private String actorUserId;
    private String actorFullName;
    private TaskBasicDTO task;
}
