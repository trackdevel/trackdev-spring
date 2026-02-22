package org.trackdev.api.dto;

import lombok.Data;

import java.util.List;

/**
 * DTO for a LIST-type attribute value applied to a student.
 * Contains the attribute metadata and all list items.
 */
@Data
public class StudentAttributeListValueDTO {
    private Long attributeId;
    private String attributeName;
    private String attributeType;
    private List<ListItemDTO> items;
    private EnumValueEntryDTO[] enumValues;
}
