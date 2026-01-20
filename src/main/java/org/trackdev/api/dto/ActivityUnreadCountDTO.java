package org.trackdev.api.dto;

import lombok.Data;

/**
 * Response DTO for activity unread count
 */
@Data
public class ActivityUnreadCountDTO {
    private long unreadCount;
    private boolean hasUnread;

    public ActivityUnreadCountDTO() {}

    public ActivityUnreadCountDTO(long unreadCount) {
        this.unreadCount = unreadCount;
        this.hasUnread = unreadCount > 0;
    }
}
