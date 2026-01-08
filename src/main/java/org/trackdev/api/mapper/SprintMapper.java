package org.trackdev.api.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.trackdev.api.dto.ProjectBasicDTO;
import org.trackdev.api.dto.SprintBasicDTO;
import org.trackdev.api.dto.SprintBoardDTO;
import org.trackdev.api.dto.SprintCompleteDTO;
import org.trackdev.api.entity.Project;
import org.trackdev.api.entity.Sprint;
import org.trackdev.api.entity.SprintStatus;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring", uses = {CourseMapper.class})
public interface SprintMapper {

    @Named("sprintToBasicDTO")
    @Mapping(target = "status", source = "status", qualifiedByName = "sprintStatusToString")
    @Mapping(target = "statusText", source = "statusText")
    SprintBasicDTO toBasicDTO(Sprint sprint);

    @Named("sprintToCompleteDTO")
    @Mapping(target = "status", source = "status", qualifiedByName = "sprintStatusToString")
    @Mapping(target = "statusText", source = "statusText")
    @Mapping(target = "project", source = "project", qualifiedByName = "projectToBasicDTOSimple")
    @Mapping(target = "activeTasks", ignore = true)
    SprintCompleteDTO toCompleteDTO(Sprint sprint);

    @Named("sprintToBoardDTO")
    @Mapping(target = "status", source = "status", qualifiedByName = "sprintStatusToString")
    @Mapping(target = "statusText", source = "statusText")
    @Mapping(target = "project", source = "project", qualifiedByName = "projectToBasicDTOSimple")
    @Mapping(target = "tasks", ignore = true)
    SprintBoardDTO toBoardDTO(Sprint sprint);

    @IterableMapping(qualifiedByName = "sprintToBasicDTO")
    List<SprintBasicDTO> toBasicDTOList(List<Sprint> sprints);

    @IterableMapping(qualifiedByName = "sprintToBasicDTO")
    Collection<SprintBasicDTO> toBasicDTOCollection(Collection<Sprint> sprints);

    @Named("sprintStatusToString")
    default String statusToString(SprintStatus status) {
        return status != null ? status.name() : null;
    }

    /**
     * Simple project mapping to avoid circular dependency with ProjectMapper.
     * Does not include nested course mapping to keep it simple.
     */
    @Named("projectToBasicDTOSimple")
    @Mapping(target = "course", source = "course", qualifiedByName = "courseToBasicDTO")
    ProjectBasicDTO projectToBasicDTO(Project project);
}
