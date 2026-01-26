package org.trackdev.api.dto;

import lombok.Data;

import java.util.List;

/**
 * DTO for Profile - Complete level (with enums and attributes)
 */
@Data
public class ProfileCompleteDTO {
    private Long id;
    private String name;
    private String description;
    private String ownerId;
    private List<ProfileEnumDTO> enums;
    private List<ProfileAttributeDTO> attributes;
}
