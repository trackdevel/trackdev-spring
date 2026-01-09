package org.trackdev.api.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.trackdev.api.entity.Course;

import java.util.Collection;
import java.util.List;

@Component
public interface CourseRepository extends BaseRepositoryLong<Course> {

    @Query("SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.projects LEFT JOIN FETCH c.students WHERE c.ownerId = :userId OR c.subject.ownerId = :userId")
    Collection<Course> findByOwnerIdOrSubjectOwnerId(@Param("userId") String userId);

    @Query("SELECT DISTINCT c FROM Course c JOIN c.projects p JOIN p.members m WHERE m.id = :userId")
    Collection<Course> findByStudentMembership(@Param("userId") String userId);

    Course findBySubject_IdAndStartYear(Long subjectId, Integer startYear);

    @Query("SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.projects LEFT JOIN FETCH c.students")
    List<Course> findAllWithProjectsAndStudents();

}
