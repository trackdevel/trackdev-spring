package org.trackdev.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.trackdev.api.entity.PointsReview;

import java.util.List;

/**
 * DTO for Task detail view - includes project and points review at root level
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskDetailDTO extends TaskWithProjectDTO {
    private List<PointsReview> pointsReview;
    
    /** Whether the current user can edit this task */
    private boolean canEdit;
}
