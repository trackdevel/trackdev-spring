package org.trackdev.api.entity;

/**
 * Types of activities that can be tracked in the notification system.
 */
public enum ActivityType {
    // Task lifecycle events
    TASK_CREATED,
    TASK_UPDATED,
    TASK_STATUS_CHANGED,
    TASK_ASSIGNED,
    TASK_UNASSIGNED,
    TASK_ESTIMATION_CHANGED,
    
    // Comment events
    COMMENT_ADDED,
    
    // Pull Request events
    PR_LINKED,
    PR_UNLINKED,
    PR_STATE_CHANGED,
    PR_MERGED,
    
    // Sprint events
    TASK_ADDED_TO_SPRINT,
    TASK_REMOVED_FROM_SPRINT
}
