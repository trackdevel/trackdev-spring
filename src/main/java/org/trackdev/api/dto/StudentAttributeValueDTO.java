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
    private String[] enumValues;
}
