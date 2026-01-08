package org.trackdev.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;
import java.util.Set;

/**
 * DTO for Project - With members (ProjectWithUser view)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectWithMembersDTO extends ProjectBasicDTO {
    private Set<UserSummaryDTO> members;
    private Collection<SprintBasicDTO> sprints;
}
