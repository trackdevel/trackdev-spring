package org.trackdev.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for CourseInvite - Basic info
 */
@Data
public class CourseInviteDTO {
    private Long id;
    private String email;
    private Long courseId;
    private String invitedById;
    private String invitedByName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime acceptedAt;
    private String acceptedById;
    private String acceptedByName;
}
