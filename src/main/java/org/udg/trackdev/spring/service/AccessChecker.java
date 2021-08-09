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
    private final String defaultNoAccessMessage = "User does not have access to this resource";

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

    public void checkCanViewCourse(Course course, String userId) {
        if(isCourseOwner(course, userId)) {
            return;
        }
        throw new ServiceException(defaultNoAccessMessage);
    }

    public void checkCanManageCourseYear(CourseYear courseYear, String userId) {
        checkCanManageCourse(courseYear.getCourse(), userId);
    }

    public void checkCanViewCourseYear(CourseYear courseYear, String userId) {
        if(isCourseOwner(courseYear.getCourse(), userId)) {
            return;
        }
        User user = userService.get(userId);
        if(courseYear.isEnrolled(user)) {
            return;
        }
        throw new ServiceException(defaultNoAccessMessage);
    }

    public void checkCanViewCourseYearAllStudents(CourseYear courseYear, String userId) {
        if(!isCourseOwner(courseYear.getCourse(), userId)) {
            throw new ServiceException(defaultNoAccessMessage);
        }
    }

    public boolean canViewCourseYearAllGroups(CourseYear courseYear, String userId) {
        if(isCourseOwner(courseYear.getCourse(), userId)) {
            return true;
        }
        return false;
    }

    public void checkCanManageGroup(Group group, String userId) {
        checkCanManageCourseYear(group.getCourseYear(), userId);
    }

    public void checkCanViewGroup(Group group, String userId) {
        if(group.isMember(userId)) {
            return;
        }
        Course course = group.getCourseYear().getCourse();
        if(isCourseOwner(course, userId)) {
            return;
        }
        throw new ServiceException(defaultNoAccessMessage);
    }

    public void checkCanManageBacklog(Backlog backlog, String userId) {
        User user = userService.get(userId);
        checkCanManageBacklog(backlog, user);
    }

    public void checkCanManageBacklog(Backlog backlog, User user) {
        Group group = backlog.getGroup();
        if(group.isMember(user)) {
           return;
        }
        Course course = group.getCourseYear().getCourse();
        if(isCourseOwner(course, user.getId())) {
            return;
        }
        throw new ServiceException("User cannot manage this backlog");
    }

    public void checkCanViewBacklog(Backlog backlog, String userId) {
        Group group = backlog.getGroup();
        if(group.isMember(userId)) {
            return;
        }
        Course course = group.getCourseYear().getCourse();
        if(isCourseOwner(course, userId)) {
            return;
        }
        throw new ServiceException(defaultNoAccessMessage);
    }

    public void checkCanViewAllTasks(String userId) {
        // Consider allowing Admin in the future.
        throw new ServiceException("User cannot see all tasks");
    }

    private boolean isCourseOwner(Course course, String userId) {
        return course.getOwnerId().equals(userId);
    }
}
