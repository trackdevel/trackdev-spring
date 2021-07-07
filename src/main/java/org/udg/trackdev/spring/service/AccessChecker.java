package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.udg.trackdev.spring.configuration.UserType;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.*;

/*
 * Centralized service to control if current principal
 * has access to a concrete entity. Access can be
 * read and manage (includes edit and delete)
 *
 * Note: methods starting with check throw exceptions.
 * otherwise they return boolean.
 */
@Component
public class AccessChecker {

    @Autowired
    UserService userService;

    public void checkCanCreateCourse(User user) {
        boolean isProfessor = user.isUserType(UserType.PROFESSOR);
        if(!isProfessor) {
            throw new ServiceException("User does not have rights to create courses");
        }
    }

    public void checkCanManageCourse(Course course, String userId) {
        if(!isCourseOwner(course, userId)) {
            throw new ServiceException("User cannot manage this course");
        }
    }

    public void checkCanManageCourseYear(CourseYear courseYear, String userId) {
        checkCanManageCourse(courseYear.getCourse(), userId);
    }

    public void checkCanViewCourseYearStudents(CourseYear courseYear, String userId) {
        if(!isCourseOwner(courseYear.getCourse(), userId)) {
            throw new ServiceException("User does not have access to this resource");
        }
    }

    public void checkCanViewCourseYearGroups(CourseYear courseYear, String userId) {
        if(isCourseOwner(courseYear.getCourse(), userId)) {
            return;
        }
        User user = userService.get(userId);
        if(courseYear.isEnrolled(user)) {
            return;
        }
        throw new ServiceException("User does not have access to this resource");
    }

    public void checkCanManageGroup(Group group, String userId) {
        checkCanManageCourseYear(group.getCourseYear(), userId);
    }

    public void checkCanManageBacklog(Backlog backlog, User userId) {
        Group group = backlog.getGroup();
        if(!group.isMember(userId)) {
            throw new ServiceException("User cannot manage this backlog");
        }
    }

    private boolean isCourseOwner(Course course, String userId) {
        return course.getOwnerId().equals(userId);
    }
}
