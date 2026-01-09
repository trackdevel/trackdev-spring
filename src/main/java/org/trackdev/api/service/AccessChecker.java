package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.trackdev.api.configuration.UserType;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.Course;
import org.trackdev.api.entity.Project;
import org.trackdev.api.entity.Subject;
import org.trackdev.api.entity.User;
import org.trackdev.api.utils.ErrorConstants;

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
    @Lazy
    UserService userService;

    // SUBJECTS

    /**
     * Check if user can create a subject.
     * Only ADMIN users can create subjects.
     */
    public void checkCanCreateSubject(User user) {
        if (!user.isUserType(UserType.ADMIN)) {
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
    }

    /**
     * Check if user can manage (edit/delete) a subject.
     * Only ADMIN users can manage subjects.
     */
    public void checkCanManageSubject(Subject subject, String userId) {
        User user = userService.get(userId);
        if (!user.isUserType(UserType.ADMIN)) {
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
    }

    /**
     * Check if user can view a subject.
     * ADMIN and PROFESSOR can view all subjects.
     */
    public void checkCanViewSubject(Subject subject, String userId) {
        User user = userService.get(userId);
        if (user.isUserType(UserType.ADMIN) || user.isUserType(UserType.PROFESSOR)) {
            return;
        }
        throw new ServiceException(ErrorConstants.UNAUTHORIZED);
    }

    // COURSES

    /**
     * Check if user can create a course under a subject.
     * ADMIN and PROFESSOR can create courses.
     */
    public void checkCanCreateCourse(Subject subject, String userId) {
        User user = userService.get(userId);
        if (user.isUserType(UserType.ADMIN) || user.isUserType(UserType.PROFESSOR)) {
            return;
        }
        throw new ServiceException(ErrorConstants.UNAUTHORIZED);
    }

    public void checkCanManageCourse(Course course, String userId) {
        User user = userService.get(userId);
        // Admin can manage any course
        if (user.isUserType(UserType.ADMIN)) {
            return;
        }
        // Course owner can manage their own course
        if (course.getOwnerId() != null && course.getOwnerId().equals(userId)) {
            return;
        }
        throw new ServiceException(ErrorConstants.UNAUTHORIZED);
    }

    public void checkCanViewCourse(Course course, String userId) {
        User user = userService.get(userId);
        // Admin can view any course
        if (user.isUserType(UserType.ADMIN)) {
            return;
        }
        // Course owner can view their course
        if (course.getOwnerId() != null && course.getOwnerId().equals(userId)) {
            return;
        }
        // Subject owner can view courses in their subject
        if(isSubjectOwner(course.getSubject(), userId)) {
            return;
        }
        throw new ServiceException(ErrorConstants.UNAUTHORIZED);
    }


    public boolean canViewCourseAllProjects(Course course, String userId) {
        User user = userService.get(userId);
        // Admin can view all projects
        if (user.isUserType(UserType.ADMIN)) {
            return true;
        }
        // Course owner can view all projects in their course
        if (course.getOwnerId() != null && course.getOwnerId().equals(userId)) {
            return true;
        }
        // Subject owner can view all projects
        if(isSubjectOwner(course.getSubject(), userId)) {
            return true;
        }
        return false;
    }

    // PROJECTS

    public void checkCanManageProject(Project project, String userId) {
        // Admins can manage any project
        if (userService.get(userId).isUserType(UserType.ADMIN)) {
            return;
        }
        checkCanManageCourse(project.getCourse(), userId);
    }

    public void checkCanViewProject(Project project, String userId) {
        // Project members can always view
        if(project.isMember(userId)) {
            return;
        }
        Course course = project.getCourse();
        // Course owner can view all projects in their course
        if(course.getOwnerId().equals(userId)) {
            return;
        }
        // Subject owner can view all projects in their subject's courses
        Subject subject = course.getSubject();
        if(isSubjectOwner(subject, userId)) {
            return;
        }
        // Enrolled students can view projects in their enrolled courses
        if(course.isStudentEnrolled(userId)) {
            return;
        }
        // Admins can view all projects
        if (userService.get(userId).isUserType(UserType.ADMIN)) {
            return;
        }
        throw new ServiceException(ErrorConstants.UNAUTHORIZED);
    }

    /**
     * Check if user can manage GitHub repos for a project.
     * Project members (students) can add/update/delete GitHub repos.
     */
    public void checkCanManageGitHubRepos(Project project, String userId) {
        // Project members can manage GitHub repos
        if (project.isMember(userId)) {
            return;
        }
        // Course owner can manage GitHub repos
        Course course = project.getCourse();
        if (course.getOwnerId().equals(userId)) {
            return;
        }
        // Subject owner can manage GitHub repos
        if (isSubjectOwner(course.getSubject(), userId)) {
            return;
        }
        // Admins can manage any GitHub repos
        if (userService.get(userId).isUserType(UserType.ADMIN)) {
            return;
        }
        throw new ServiceException(ErrorConstants.UNAUTHORIZED);
    }

    public void checkCanViewAllTasks(String userId) {
        if(userService.get(userId).isUserType(UserType.ADMIN)) {
            return;
        }
        throw new ServiceException(ErrorConstants.UNAUTHORIZED);
    }

    public boolean checkCanViewAllProjects(String userId) {
        if(userService.get(userId).isUserType(UserType.ADMIN)) {
            return true;
        }
        else{
            return false;
        }
    }

    private boolean isSubjectOwner(Subject subject, String userId) {
        return subject.getOwnerId().equals(userId);
    }

    /**
     * Check if user is an admin. Returns true/false without throwing.
     */
    public boolean isUserAdmin(User user) {
        return user.isUserType(UserType.ADMIN);
    }

    /**
     * Check if user is an admin. Throws exception if not.
     */
    public void checkIsUserAdmin(User user) {
        if (!user.isUserType(UserType.ADMIN)) {
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
    }

    /**
     * Check if user is a professor or admin. Throws exception if not.
     */
    public void checkIsProfessorOrAdmin(User user) {
        if (!user.isUserType(UserType.ADMIN) && !user.isUserType(UserType.PROFESSOR)) {
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
    }

    /**
     * Check if user can delete a task. Allowed for:
     * - Task reporter (creator)
     * - Subject owner (professor)
     * - Admin
     */
    public void checkCanDeleteTask(org.trackdev.api.entity.Task task, String userId) {
        // Task reporter can delete their own task
        if (task.getReporter() != null && task.getReporter().getId().equals(userId)) {
            return;
        }
        // Subject owner (professor) can delete any task in their course
        Subject subject = task.getProject().getCourse().getSubject();
        if (isSubjectOwner(subject, userId)) {
            return;
        }
        // Admin can delete any task
        if (userService.get(userId).isUserType(UserType.ADMIN)) {
            return;
        }
        throw new ServiceException(ErrorConstants.UNAUTHORIZED);
    }

    /**
     * Check if user is the assignee of a task.
     * Returns true if the user is assigned to this task.
     */
    public boolean isTaskAssignee(org.trackdev.api.entity.Task task, String userId) {
        return task.getAssignee() != null && task.getAssignee().getId().equals(userId);
    }

    /**
     * Check if user can create subtasks for a task.
     * Only the assigned user can create subtasks.
     * Professors (subject owners) and admins can also create subtasks.
     */
    public void checkCanCreateSubtask(org.trackdev.api.entity.Task task, String userId) {
        // Assigned user can create subtasks
        if (isTaskAssignee(task, userId)) {
            return;
        }
        // Subject owner (professor) can create subtasks
        Subject subject = task.getProject().getCourse().getSubject();
        if (isSubjectOwner(subject, userId)) {
            return;
        }
        // Admin can create subtasks
        if (userService.get(userId).isUserType(UserType.ADMIN)) {
            return;
        }
        throw new ServiceException(ErrorConstants.ONLY_ASSIGNEE_CAN_CREATE_SUBTASK);
    }

    /**
     * Check if user can modify the status of a task.
     * Only the assigned user can modify the status.
     * Professors (subject owners) and admins can also modify status.
     */
    public void checkCanModifyTaskStatus(org.trackdev.api.entity.Task task, String userId) {
        // Assigned user can modify status
        if (isTaskAssignee(task, userId)) {
            return;
        }
        // Subject owner (professor) can modify status
        Subject subject = task.getProject().getCourse().getSubject();
        if (isSubjectOwner(subject, userId)) {
            return;
        }
        // Admin can modify status
        if (userService.get(userId).isUserType(UserType.ADMIN)) {
            return;
        }
        throw new ServiceException(ErrorConstants.ONLY_ASSIGNEE_CAN_MODIFY_STATUS);
    }

    /**
     * Check if user can self-assign a task.
     * Any project member can self-assign an unassigned task.
     */
    public void checkCanSelfAssignTask(org.trackdev.api.entity.Task task, String userId) {
        // First check if user is a project member
        checkCanViewProject(task.getProject(), userId);
        
        // Task must be unassigned or already assigned to the user
        if (task.getAssignee() != null && !task.getAssignee().getId().equals(userId)) {
            throw new ServiceException(ErrorConstants.TASK_ALREADY_ASSIGNED);
        }
    }

    /**
     * Check if user can edit a task (name, description, estimation points, etc.).
     * Only the assigned user, subject owner (professor), or admin can edit.
     */
    public void checkCanEditTask(org.trackdev.api.entity.Task task, String userId) {
        // Assigned user can edit
        if (isTaskAssignee(task, userId)) {
            return;
        }
        // Subject owner (professor) can edit
        Subject subject = task.getProject().getCourse().getSubject();
        if (isSubjectOwner(subject, userId)) {
            return;
        }
        // Admin can edit
        if (userService.get(userId).isUserType(UserType.ADMIN)) {
            return;
        }
        throw new ServiceException(ErrorConstants.ONLY_ASSIGNEE_CAN_EDIT_TASK);
    }

    /**
     * Check if user can edit a task (returns boolean, doesn't throw).
     */
    public boolean canEditTask(org.trackdev.api.entity.Task task, String userId) {
        // Assigned user can edit
        if (isTaskAssignee(task, userId)) {
            return true;
        }
        // Subject owner (professor) can edit
        Subject subject = task.getProject().getCourse().getSubject();
        if (isSubjectOwner(subject, userId)) {
            return true;
        }
        // Admin can edit
        if (userService.get(userId).isUserType(UserType.ADMIN)) {
            return true;
        }
        return false;
    }

    /**
     * Check if user can add a comment to a task.
     * Any project member can add comments to tasks in their project.
     * Professors (subject owners) and admins can also add comments.
     */
    public void checkCanAddComment(org.trackdev.api.entity.Task task, String userId) {
        Project project = task.getProject();
        
        // Project members can add comments
        if (project.isMember(userId)) {
            return;
        }
        // Subject owner (professor) can add comments
        Subject subject = project.getCourse().getSubject();
        if (isSubjectOwner(subject, userId)) {
            return;
        }
        // Course owner can add comments
        if (project.getCourse().getOwnerId().equals(userId)) {
            return;
        }
        // Admin can add comments
        if (userService.get(userId).isUserType(UserType.ADMIN)) {
            return;
        }
        throw new ServiceException(ErrorConstants.UNAUTHORIZED);
    }

}
