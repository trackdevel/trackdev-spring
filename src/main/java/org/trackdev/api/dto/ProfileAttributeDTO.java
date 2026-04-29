package org.trackdev.api.dto;

import lombok.Data;
import org.trackdev.api.entity.AttributeAppliedBy;
import org.trackdev.api.entity.AttributeTarget;
import org.trackdev.api.entity.AttributeType;
import org.trackdev.api.entity.AttributeVisibility;

import java.util.List;

/**
 * DTO for ProfileAttribute
 */
@Data
public class ProfileAttributeDTO {
    private Long id;
    private String name;
    private AttributeType type;
    private AttributeTarget target;
    private AttributeAppliedBy appliedBy;
    private AttributeVisibility visibility;
    private Long enumRefId;
    private String enumRefName;
    private List<EnumValueEntryDTO> enumValues;
    /** Second enum, only populated when type is ENUM_PAIR. */
    private Long enumRef2Id;
    private String enumRef2Name;
    private List<EnumValueEntryDTO> enumValues2;
    private String defaultValue;
    private String minValue;
    private String maxValue;
}