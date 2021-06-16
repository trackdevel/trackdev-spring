package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.Course;
import org.udg.trackdev.spring.entity.CourseYear;
import org.udg.trackdev.spring.repository.CourseYearRepository;

@Service
public class CourseYearService extends BaseService<CourseYear, CourseYearRepository> {

    @Autowired
    CourseService courseService;

    @Transactional
    public CourseYear createCourseYear(Long courseId, Integer startYear, Integer endYear, String loggedInUserId) {
        Course course = courseService.getCourse(courseId);
        if(!courseService.canManageCourse(course, loggedInUserId)) {
            throw new ServiceException("User cannot manage this course");
        }
        CourseYear courseYear = new CourseYear(startYear, endYear);
        courseYear.setCourse(course);
        course.addCourseYear(courseYear);
        return courseYear;
    }
}
