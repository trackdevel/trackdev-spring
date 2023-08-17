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

    public void checkCanManageCourse(Subject subject, String userId) {
        if(!isCourseOwner(subject, userId)) {
            throw new ServiceException("User cannot manage this subject");
        }
    }

    public void checkCanViewCourse(Subject subject, String userId) {
        if(isCourseOwner(subject, userId)) {
            return;
        }
        throw new ServiceException(defaultNoAccessMessage);
    }

    public void checkCanManageCourseYear(Courses courses, String userId) {
        checkCanManageCourse(courses.getSubject(), userId);
    }

    public void checkCanViewCourseYear(Courses courses, String userId) {
        if(isCourseOwner(courses.getSubject(), userId)) {
            return;
        }
        User user = userService.get(userId);
        if(courses.isEnrolled(user)) {
            return;
        }
        throw new ServiceException(defaultNoAccessMessage);
    }

    public void checkCanViewCourseYearAllStudents(Courses courses, String userId) {
        if(!isCourseOwner(courses.getSubject(), userId)) {
            throw new ServiceException(defaultNoAccessMessage);
        }
    }

    public boolean canViewCourseYearAllGroups(Courses courses, String userId) {
        if(isCourseOwner(courses.getSubject(), userId)) {
            return true;
        }
        return false;
    }

    public void checkCanManageGroup(Project project, String userId) {
        checkCanManageCourseYear(project.getCourseYear(), userId);
    }

    public void checkCanViewGroup(Project project, String userId) {
        if(project.isMember(userId)) {
            return;
        }
        Subject subject = project.getCourseYear().getSubject();
        if(isCourseOwner(subject, userId)) {
            return;
        }
        throw new ServiceException(defaultNoAccessMessage);
    }

    public void checkCanManageBacklog(Backlog backlog, String userId) {
        User user = userService.get(userId);
        checkCanManageBacklog(backlog, user);
    }

    public void checkCanManageBacklog(Backlog backlog, User user) {
        Project project = backlog.getGroup();
        if(project.isMember(user)) {
           return;
        }
        Subject subject = project.getCourseYear().getSubject();
        if(isCourseOwner(subject, user.getId())) {
            return;
        }
        throw new ServiceException("User cannot manage this backlog");
    }

    public void checkCanViewBacklog(Backlog backlog, String userId) {
        Project project = backlog.getGroup();
        if(project.isMember(userId)) {
            return;
        }
        Subject subject = project.getCourseYear().getSubject();
        if(isCourseOwner(subject, userId)) {
            return;
        }
        throw new ServiceException(defaultNoAccessMessage);
    }

    public void checkCanViewAllTasks(String userId) {
        // Consider allowing Admin in the future.
        throw new ServiceException("User cannot see all tasks");
    }

    private boolean isCourseOwner(Subject subject, String userId) {
        return subject.getOwnerId().equals(userId);
    }

    public boolean isUserAdminOrProfessor(User user) {
        return user.isUserType(UserType.PROFESSOR) || user.isUserType(UserType.ADMIN);
    }

}
