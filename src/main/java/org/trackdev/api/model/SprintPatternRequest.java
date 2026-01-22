package org.trackdev.api.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Request model for creating a new sprint pattern
 */
public class SprintPatternRequest {
    
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    public String name;

    @Valid
    public List<SprintPatternItemRequest> items;

    public static class SprintPatternItemRequest {
        /**
         * ID of existing item (null for new items)
         */
        public Long id;

        @NotBlank(message = "Sprint name is required")
        @Size(min = 1, max = 50, message = "Sprint name must be between 1 and 50 characters")
        public String name;

        public ZonedDateTime startDate;

        public ZonedDateTime endDate;

        public Integer orderIndex;
    }
}
