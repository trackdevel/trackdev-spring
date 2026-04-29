package org.trackdev.api.dto;

import lombok.Data;
import org.trackdev.api.entity.AttributeAppliedBy;
import org.trackdev.api.entity.AttributeType;

/**
 * DTO for StudentAttributeValue
 */
@Data
public class StudentAttributeValueDTO {
    private Long id;
    private String userId;
    private Long attributeId;
    private String attributeName;
    private AttributeType attributeType;
    private AttributeAppliedBy attributeAppliedBy;
    private String value;
    /** Second value, only populated when attributeType is ENUM_PAIR. */
    private String valueB;
    private String textValue;
    private EnumValueEntryDTO[] enumValues;
    /** Possible values for the second enum slot — populated only when attributeType is ENUM_PAIR. */
    private EnumValueEntryDTO[] enumValues2;
}