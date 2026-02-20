package org.trackdev.api.dto;

import lombok.Data;
import org.trackdev.api.entity.AttributeAppliedBy;
import org.trackdev.api.entity.AttributeType;

/**
 * DTO for PullRequestAttributeValue
 */
@Data
public class PullRequestAttributeValueDTO {
    private Long id;
    private String pullRequestId;
    private Long attributeId;
    private String attributeName;
    private AttributeType attributeType;
    private AttributeAppliedBy attributeAppliedBy;
    private String value;
    private EnumValueEntryDTO[] enumValues;
}
