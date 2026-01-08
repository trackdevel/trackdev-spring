package org.trackdev.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

/**
 * DTO for subjects list response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectsResponseDTO {
    private Collection<SubjectCompleteDTO> subjects;
}
