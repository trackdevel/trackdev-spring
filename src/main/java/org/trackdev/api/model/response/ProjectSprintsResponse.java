package org.trackdev.api.model.response;

import java.util.List;

/**
 * DTO for project sprints list response
 */
public class ProjectSprintsResponse {
    private List<SprintSummary> sprints;
    private Long projectId;

    public ProjectSprintsResponse() {}

    public ProjectSprintsResponse(List<SprintSummary> sprints, Long projectId) {
        this.sprints = sprints;
        this.projectId = projectId;
    }

    public List<SprintSummary> getSprints() {
        return sprints;
    }

    public void setSprints(List<SprintSummary> sprints) {
        this.sprints = sprints;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    /**
     * Sprint summary DTO
     */
    public static class SprintSummary {
        private Long id;
        private String value;
        private String label;
        private String startDate;
        private String endDate;
        private String status;

        public SprintSummary() {}

        public SprintSummary(Long id, String name, String startDate, String endDate, String status) {
            this.id = id;
            this.value = name;
            this.label = name;
            this.startDate = startDate;
            this.endDate = endDate;
            this.status = status;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
