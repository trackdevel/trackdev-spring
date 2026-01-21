package org.trackdev.api.dto;

import lombok.Data;

import java.time.ZonedDateTime;

/**
 * DTO for CourseInvite - Basic info
 */
@Data
public class CourseInviteDTO {
    private Long id;
    private String fullName;
    private String email;
    private Long courseId;
    private String invitedById;
    private String invitedByName;
    private String status;
    private ZonedDateTime createdAt;
    private ZonedDateTime expiresAt;
    private ZonedDateTime acceptedAt;
    private String acceptedById;
    private String acceptedByName;
}
