package org.udg.trackdev.spring.repository;

import org.springframework.stereotype.Component;
import org.udg.trackdev.spring.entity.Course;

import java.util.List;

@Component
public interface CourseRepository extends BaseRepositoryLong<Course> {
    List<Course> findByOwner(String owner);
}
