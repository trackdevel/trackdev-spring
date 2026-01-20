package org.trackdev.api.dto;

import lombok.Data;

import java.util.List;

/**
 * Response DTO for paginated activities
 */
@Data
public class ActivitiesResponseDTO {
    private List<ActivityDTO> activities;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public ActivitiesResponseDTO() {}

    public ActivitiesResponseDTO(List<ActivityDTO> activities, int page, int size, 
                                  long totalElements, int totalPages, 
                                  boolean hasNext, boolean hasPrevious) {
        this.activities = activities;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }
}
