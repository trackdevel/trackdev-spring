package org.trackdev.api.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.trackdev.api.dto.ProjectBasicDTO;
import org.trackdev.api.dto.ProjectCompleteDTO;
import org.trackdev.api.dto.ProjectWithMembersDTO;
import org.trackdev.api.entity.Project;

import java.util.Collection;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CourseMapper.class, SprintMapper.class})
public interface ProjectMapper {

    @Named("projectToBasicDTO")
    @Mapping(target = "course", source = "course", qualifiedByName = "courseToBasicDTO")
    ProjectBasicDTO toBasicDTO(Project project);

    @Named("projectToWithMembersDTO")
    @Mapping(target = "course", source = "course", qualifiedByName = "courseToBasicDTO")
    @Mapping(target = "members", source = "members", qualifiedByName = "userToSummaryDTO")
    @Mapping(target = "sprints", source = "sprints", qualifiedByName = "sprintToBasicDTO")
    ProjectWithMembersDTO toWithMembersDTO(Project project);

    @Named("projectToCompleteDTO")
    @Mapping(target = "course", source = "course", qualifiedByName = "courseToBasicDTO")
    @Mapping(target = "members", source = "members", qualifiedByName = "userToSummaryDTO")
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "sprints", ignore = true)
    ProjectCompleteDTO toCompleteDTO(Project project);

    @IterableMapping(qualifiedByName = "projectToBasicDTO")
    Collection<ProjectBasicDTO> toBasicDTOList(Collection<Project> projects);

    @IterableMapping(qualifiedByName = "projectToWithMembersDTO")
    Collection<ProjectWithMembersDTO> toWithMembersDTOList(Collection<Project> projects);
}
