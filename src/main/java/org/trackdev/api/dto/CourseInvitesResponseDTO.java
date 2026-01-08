package org.trackdev.api.dto;

import lombok.Data;

import java.util.Collection;

/**
 * Response wrapper for a list of course invites
 */
@Data
public class CourseInvitesResponseDTO {
    private Collection<CourseInviteDTO> invites;

    public CourseInvitesResponseDTO(Collection<CourseInviteDTO> invites) {
        this.invites = invites;
    }
}
