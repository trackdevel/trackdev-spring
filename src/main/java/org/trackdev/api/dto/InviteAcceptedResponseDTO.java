package org.trackdev.api.dto;

import lombok.Data;

/**
 * DTO returned after accepting an invitation
 */
@Data
public class InviteAcceptedResponseDTO {
    private Long courseId;
    private String courseName;
    private Integer startYear;
    private boolean newUserCreated;
    private boolean passwordChangeRequired;
    private String message;

    public InviteAcceptedResponseDTO(Long courseId, String courseName, Integer startYear, 
                                      boolean newUserCreated, boolean passwordChangeRequired, String message) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.startYear = startYear;
        this.newUserCreated = newUserCreated;
        this.passwordChangeRequired = passwordChangeRequired;
        this.message = message;
    }
}
