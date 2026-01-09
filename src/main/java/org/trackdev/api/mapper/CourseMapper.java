package org.trackdev.api.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.trackdev.api.dto.CourseBasicDTO;
import org.trackdev.api.dto.CourseCompleteDTO;
import org.trackdev.api.dto.CourseStudentDTO;
import org.trackdev.api.entity.Course;

import java.util.Collection;

@Mapper(componentModel = "spring", uses = {SubjectMapper.class, ProjectMapper.class})
public interface CourseMapper {

    @Named("courseToBasicDTO")
    @Mapping(target = "subject", source = "subject", qualifiedByName = "subjectToBasicDTO")
    @Mapping(target = "ownerId", source = "ownerId")
    CourseBasicDTO toBasicDTO(Course course);

    @Named("courseToCompleteDTO")
    @Mapping(target = "subject", source = "subject", qualifiedByName = "subjectToBasicDTO")
    @Mapping(target = "ownerId", source = "ownerId")
    @Mapping(target = "projectCount", expression = "java(course.getProjects() != null ? course.getProjects().size() : 0)")
    @Mapping(target = "studentCount", expression = "java(course.getStudents() != null ? course.getStudents().size() : 0)")
    CourseCompleteDTO toCompleteDTO(Course course);

    @IterableMapping(qualifiedByName = "courseToBasicDTO")
    Collection<CourseBasicDTO> toBasicDTOList(Collection<Course> courses);

    @IterableMapping(qualifiedByName = "courseToCompleteDTO")
    Collection<CourseCompleteDTO> toCompleteDTOList(Collection<Course> courses);

    @Named("courseToStudentDTO")
    @Mapping(target = "subject", source = "subject", qualifiedByName = "subjectToBasicDTO")
    @Mapping(target = "enrolledProjects", ignore = true)
    CourseStudentDTO toStudentDTO(Course course);
}
