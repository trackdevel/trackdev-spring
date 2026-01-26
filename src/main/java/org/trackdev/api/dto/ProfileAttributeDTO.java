package org.trackdev.api.dto;

import lombok.Data;
import org.trackdev.api.entity.AttributeTarget;
import org.trackdev.api.entity.AttributeType;

/**
 * DTO for ProfileAttribute
 */
@Data
public class ProfileAttributeDTO {
    private Long id;
    private String name;
    private AttributeType type;
    private AttributeTarget target;
    private Long enumRefId;
    private String enumRefName;
}
