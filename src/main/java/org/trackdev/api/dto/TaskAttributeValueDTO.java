package org.trackdev.api.dto;

import lombok.Data;
import org.trackdev.api.entity.AttributeAppliedBy;
import org.trackdev.api.entity.AttributeType;

/**
 * DTO for TaskAttributeValue - represents the value of a profile attribute for a task
 */
@Data
public class TaskAttributeValueDTO {
    private Long id;
    private Long taskId;
    private Long attributeId;
    private String attributeName;
    private AttributeType attributeType;
    private AttributeAppliedBy attributeAppliedBy;
    private String value;
    /** Second value, only populated when attributeType is ENUM_PAIR. */
    private String valueB;
    private String textValue;

    // For ENUM type, include the possible values with descriptions
    private EnumValueEntryDTO[] enumValues;
    /** Possible values for the second enum slot — populated only when attributeType is ENUM_PAIR. */
    private EnumValueEntryDTO[] enumValues2;
}