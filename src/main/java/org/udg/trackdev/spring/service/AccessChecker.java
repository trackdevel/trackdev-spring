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

    // SUBJECTS

    public void checkCanCreateSubject(User user) {
        boolean isProfessor = user.isUserType(UserType.PROFESSOR);
        if(!isProfessor) {
            throw new ServiceException("User does not have rights to create courses");
        }
    }

    public void checkCanManageSubject(Subject subject, String userId) {
        if(!isSubjectOwner(subject, userId)) {
            throw new ServiceException("User cannot manage this subject");
        }
    }

    public void checkCanViewSubject(Subject subject, String userId) {
        if(isSubjectOwner(subject, userId)) {
            return;
        }
        throw new ServiceException(defaultNoAccessMessage);
    }

    // COURSES

    public void checkCanManageCourse(Course course, String userId) {
        checkCanManageSubject(course.getSubject(), userId);
    }

    public void checkCanViewCourse(Course course, String userId) {
        if(isSubjectOwner(course.getSubject(), userId)) {
            return;
        }
        User user = userService.get(userId);
        if(course.isEnrolled(user)) {
            return;
        }
        throw new ServiceException(defaultNoAccessMessage);
    }

    public void checkCanViewCourseAllMembers(Course course, String userId) {
        if(!isSubjectOwner(course.getSubject(), userId)) {
            throw new ServiceException(defaultNoAccessMessage);
        }
    }

    public boolean canViewCourseAllProjects(Course course, String userId) {
        if(isSubjectOwner(course.getSubject(), userId)) {
            return true;
        }
        return false;
    }

    // PROJECTS

    public void checkCanManageProject(Project project, String userId) {
        checkCanManageCourse(project.getCourse(), userId);
    }

    public void checkCanViewProject(Project project, String userId) {
        if(project.isMember(userId)) {
            return;
        }
        Subject subject = project.getCourse().getSubject();
        if(isSubjectOwner(subject, userId)) {
            return;
        }
        throw new ServiceException(defaultNoAccessMessage);
    }

    public void checkCanViewAllTasks(String userId) {
        if(userService.get(userId).isUserType(UserType.ADMIN)) {
            return;
        }
        throw new ServiceException("User cannot see all tasks");
    }

    public void checkCanViewAllProjects(String userId) {
        if(userService.get(userId).isUserType(UserType.ADMIN)) {
            return;
        }
        throw new ServiceException("User cannot see all projects");
    }

    private boolean isSubjectOwner(Subject subject, String userId) {
        return subject.getOwnerId().equals(userId);
    }

    public boolean isUserAdminOrProfessor(User user) {
        return user.isUserType(UserType.ADMIN) || user.isUserType(UserType.PROFESSOR);
    }

}
