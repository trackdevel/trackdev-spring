package org.trackdev.api.dto;

import lombok.Data;
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
    private String value;
    
    // For ENUM type, include the possible values
    private String[] enumValues;
}
