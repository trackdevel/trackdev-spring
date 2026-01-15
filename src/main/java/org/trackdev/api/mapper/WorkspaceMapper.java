package org.trackdev.api.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.trackdev.api.dto.WorkspaceBasicDTO;
import org.trackdev.api.dto.WorkspaceCompleteDTO;
import org.trackdev.api.entity.Workspace;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class, SubjectMapper.class})
public interface WorkspaceMapper {

    @Named("workspaceToBasicDTO")
    WorkspaceBasicDTO toBasicDTO(Workspace workspace);

    @Named("workspaceToCompleteDTO")
    WorkspaceCompleteDTO toCompleteDTO(Workspace workspace);

    @IterableMapping(qualifiedByName = "workspaceToBasicDTO")
    List<WorkspaceBasicDTO> toBasicDTOList(List<Workspace> workspaces);
}
