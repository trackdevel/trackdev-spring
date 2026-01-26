package org.trackdev.api.dto;

import lombok.Data;

import java.util.List;

/**
 * Response DTO for list of profiles
 */
@Data
public class ProfilesResponseDTO {
    private List<ProfileBasicDTO> profiles;
}
