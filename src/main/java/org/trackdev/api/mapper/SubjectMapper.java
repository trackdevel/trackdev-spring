package org.trackdev.api.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.trackdev.api.dto.CourseBasicDTO;
import org.trackdev.api.dto.SubjectBasicDTO;
import org.trackdev.api.dto.SubjectCompleteDTO;
import org.trackdev.api.entity.Course;
import org.trackdev.api.entity.Subject;

import java.util.Collection;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface SubjectMapper {

    @Named("subjectToBasicDTO")
    SubjectBasicDTO toBasicDTO(Subject subject);

    @Named("subjectToCompleteDTO")
    @Mapping(target = "courses", source = "courses", qualifiedByName = "coursesToBasicDTOList")
    SubjectCompleteDTO toCompleteDTO(Subject subject);

    @IterableMapping(qualifiedByName = "subjectToCompleteDTO")
    Collection<SubjectCompleteDTO> toCompleteDTOList(Collection<Subject> subjects);

    @Named("courseToBasicDTOInternal")
    default CourseBasicDTO courseToBasicDTO(Course course) {
        if (course == null) {
            return null;
        }
        CourseBasicDTO dto = new CourseBasicDTO();
        dto.setId(course.getId());
        dto.setStartYear(course.getStartYear());
        dto.setGithubOrganization(course.getGithubOrganization());
        dto.setOwnerId(course.getOwnerId());
        return dto;
    }

    @Named("coursesToBasicDTOList")
    default Collection<CourseBasicDTO> coursesToBasicDTOList(Collection<Course> courses) {
        if (courses == null) {
            return null;
        }
        return courses.stream()
                .map(this::courseToBasicDTO)
                .collect(Collectors.toList());
    }
}
