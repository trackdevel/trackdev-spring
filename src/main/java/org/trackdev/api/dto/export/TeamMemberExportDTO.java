package org.trackdev.api.dto.export;

import lombok.Data;
import org.trackdev.api.dto.StudentAttributeValueDTO;
import org.trackdev.api.dto.UserSummaryDTO;

import java.util.List;
import java.util.Set;

@Data
public class TeamMemberExportDTO {
    private UserSummaryDTO user;
    private Set<String> roles;
    private List<StudentAttributeValueDTO> attributeValues;
}
