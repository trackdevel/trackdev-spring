package org.trackdev.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.trackdev.api.entity.PointsReview;

import java.util.List;

/**
 * DTO for Task detail view - includes project and points review at root level
 * Also includes computed permission flags based on current user context.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskDetailDTO extends TaskWithProjectDTO {
    private List<PointsReview> pointsReview;
    
    // =============================================================================
    // PERMISSION FLAGS (computed based on current user context)
    // =============================================================================
    
    /** Whether the current user can edit this task (name, description, etc.) */
    private boolean canEdit;
    
    /** Whether the current user can change the task status */
    private boolean canEditStatus;
    
    /** Whether the current user can change the sprint assignment */
    private boolean canEditSprint;
    
    /** Whether the current user can change the task type */
    private boolean canEditType;
    
    /** Whether the current user can change estimation points */
    private boolean canEditEstimation;
    
    /** Whether the current user can delete this task */
    private boolean canDelete;
    
    /** Whether the current user can self-assign this task */
    private boolean canSelfAssign;
    
    /** Whether the current user can unassign the current assignee */
    private boolean canUnassign;
    
    /** Whether the current user can add subtasks to this task (only for USER_STORY) */
    private boolean canAddSubtask;
    
    /** Whether the current user can freeze/unfreeze this task */
    private boolean canFreeze;
    
    /** Whether the current user can add comments */
    private boolean canComment;
}
