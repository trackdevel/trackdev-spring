package org.trackdev.api.model.response;

import java.util.Map;

/**
 * DTO for project qualification/ranking response
 */
public class ProjectQualificationResponse {
    private Long projectId;
    private Map<String, UserQualification> qualifications;

    public ProjectQualificationResponse() {}

    public ProjectQualificationResponse(Long projectId, Map<String, UserQualification> qualifications) {
        this.projectId = projectId;
        this.qualifications = qualifications;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Map<String, UserQualification> getQualifications() {
        return qualifications;
    }

    public void setQualifications(Map<String, UserQualification> qualifications) {
        this.qualifications = qualifications;
    }

    /**
     * User qualification info
     */
    public static class UserQualification {
        private String name;
        private String acronym;
        private String color;
        private String qualification;

        public UserQualification() {}

        public UserQualification(String name, String acronym, String color, String qualification) {
            this.name = name;
            this.acronym = acronym;
            this.color = color;
            this.qualification = qualification;
        }

        public UserQualification(Map<String, String> map) {
            this.name = map.get("name");
            this.acronym = map.get("acronym");
            this.color = map.get("color");
            this.qualification = map.get("qualification");
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAcronym() {
            return acronym;
        }

        public void setAcronym(String acronym) {
            this.acronym = acronym;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getQualification() {
            return qualification;
        }

        public void setQualification(String qualification) {
            this.qualification = qualification;
        }
    }
}
