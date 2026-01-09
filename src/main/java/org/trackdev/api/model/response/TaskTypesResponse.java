package org.trackdev.api.model.response;

import java.util.Map;

/**
 * DTO for task types list response
 */
public class TaskTypesResponse {
    private Map<String, String> types;

    public TaskTypesResponse() {}

    public TaskTypesResponse(Map<String, String> types) {
        this.types = types;
    }

    public Map<String, String> getTypes() {
        return types;
    }

    public void setTypes(Map<String, String> types) {
        this.types = types;
    }
}
