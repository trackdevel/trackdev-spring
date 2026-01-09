package org.trackdev.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;

/**
 * DTO for User - With projects list (UserWithoutProjectMembers view)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserWithProjectsDTO extends UserBasicDTO {
    private Collection<ProjectBasicDTO> projects;
}
