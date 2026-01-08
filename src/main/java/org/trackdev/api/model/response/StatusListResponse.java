package org.trackdev.api.model.response;

import java.util.Map;

/**
 * DTO for status/type list responses
 */
public class StatusListResponse {
    private Map<String, String> statuses;

    public StatusListResponse() {}

    public StatusListResponse(Map<String, String> statuses) {
        this.statuses = statuses;
    }

    public Map<String, String> getStatuses() {
        return statuses;
    }

    public void setStatuses(Map<String, String> statuses) {
        this.statuses = statuses;
    }
}
