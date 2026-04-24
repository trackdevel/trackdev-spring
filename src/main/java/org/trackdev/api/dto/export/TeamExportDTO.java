package org.trackdev.api.dto.export;

import lombok.Data;

import java.util.List;

@Data
public class TeamExportDTO {
    private Long projectId;
    private List<TeamMemberExportDTO> members;
}
