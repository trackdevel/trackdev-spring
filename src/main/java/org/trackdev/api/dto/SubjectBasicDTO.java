package org.trackdev.api.dto;

import lombok.Data;

/**
 * DTO for Subject - Basic level (embedded in courses/hierarchy)
 */
@Data
public class SubjectBasicDTO {
    private Long id;
    private String name;
    private String acronym;
    private String ownerId;
}
