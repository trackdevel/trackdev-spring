package org.trackdev.api.dto;

import lombok.Data;
import java.util.Collection;

@Data
public class WorkspaceCompleteDTO {
    private Long id;
    private String name;
    private Collection<UserSummaryDTO> users;
    private Collection<SubjectBasicDTO> subjects;
}
