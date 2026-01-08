package org.trackdev.api.dto;

import lombok.Data;

import java.util.Collection;

/**
 * DTO for Course details including students
 */
@Data
public class CourseDetailsDTO {
    private Long id;
    private Integer startYear;
    private String githubOrganization;
    private String ownerId;
    private SubjectBasicDTO subject;
    private Collection<ProjectWithMembersDTO> projects;
    private Collection<UserSummaryDTO> students;
    private Collection<CourseInviteDTO> pendingInvites;
}
