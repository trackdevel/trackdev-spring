package org.trackdev.api.model;

import jakarta.validation.constraints.NotNull;

public class AddPointsReviewParticipantRequest {

    @NotNull(message = "User ID is required")
    private String userId;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
