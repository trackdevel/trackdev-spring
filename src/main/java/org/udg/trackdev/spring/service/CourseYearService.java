package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.*;
import org.udg.trackdev.spring.repository.CourseYearRepository;

@Service
public class CourseYearService extends BaseService<CourseYear, CourseYearRepository> {

    @Autowired
    CourseService courseService;

    @Autowired
    InviteCourseBuilder courseInviteBuilder;

    @Transactional
    public CourseYear createCourseYear(Long courseId, Integer startYear, String loggedInUserId) {
        Course course = courseService.getCourse(courseId);
        if(!courseService.canManageCourse(course, loggedInUserId)) {
            throw new ServiceException("User cannot manage this course");
        }
        CourseYear courseYear = new CourseYear(startYear);
        courseYear.setCourse(course);
        course.addCourseYear(courseYear);
        return courseYear;
    }

    public void deleteCourseYear(Long yearId, String loggedInUserId) {
        CourseYear courseYear = get(yearId);
        if(!courseService.canManageCourse(courseYear.getCourse(), loggedInUserId)) {
            throw new ServiceException("User cannot manage this course");
        }
        repo.delete(courseYear);
    }

    @Transactional
    public Invite createInvite(String email, Long yearId, String ownerId) {
        CourseYear courseYear = get(yearId);
        Invite invite = courseInviteBuilder.Build(email, ownerId, courseYear);
        return invite;
    }
}
