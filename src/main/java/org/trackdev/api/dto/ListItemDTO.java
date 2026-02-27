package org.trackdev.api.dto;

import lombok.Data;

/**
 * DTO for an individual list item in a LIST-type attribute.
 */
@Data
public class ListItemDTO {
    private int orderIndex;
    private String enumValue;
    private String title;
    private String description;
}
