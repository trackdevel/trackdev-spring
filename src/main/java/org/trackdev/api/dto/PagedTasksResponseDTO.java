package org.trackdev.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for paginated tasks list response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedTasksResponseDTO {
    private List<TaskBasicDTO> tasks;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
}
