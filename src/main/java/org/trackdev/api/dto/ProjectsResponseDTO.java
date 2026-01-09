package org.trackdev.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

/**
 * DTO for projects list response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectsResponseDTO {
    private Collection<ProjectWithMembersDTO> projects;
}
