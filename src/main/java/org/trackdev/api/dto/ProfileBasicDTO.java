package org.trackdev.api.dto;

import lombok.Data;

/**
 * DTO for Profile - Basic level (for lists)
 */
@Data
public class ProfileBasicDTO {
    private Long id;
    private String name;
    private String description;
    private String ownerId;
}
