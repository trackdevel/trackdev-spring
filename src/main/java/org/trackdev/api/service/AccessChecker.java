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
import org.trackdev.api.entity.Workspace;
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

    @Autowired
    @Lazy
    CourseService courseService;

    // SUBJECTS

    /**
     * Check if user can create a subject.
     * ADMIN can create subjects in any workspace.
     * WORKSPACE_ADMIN can create subjects in their own workspace.
     */
    public void checkCanCreateSubject(User user) {
        if (!user.isUserType(UserType.ADMIN) && !user.isUserType(UserType.WORKSPACE_ADMIN)) {
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
    }

    /**
     * Check if user can manage (edit/delete) a subject.
     * ADMIN can manage any subject.
     * WORKSPACE_ADMIN can manage subjects in their workspace.
     */
    public void checkCanManageSubject(Subject subject, String userId) {
        User user = userService.get(userId);
        
        // ADMIN can manage any subject
        if (user.isUserType(UserType.ADMIN)) {
            return;
        }
        
        // WORKSPACE_ADMIN can manage subjects in their workspace
        if (user.isUserType(UserType.WORKSPACE_ADMIN)) {
            if (subject.getWorkspace() != null && 
                user.getWorkspace() != null && 
                subject.getWorkspace().getId().equals(user.getWorkspace().getId())) {
                return;
            }
        }
        
        throw new ServiceException(ErrorConstants.UNAUTHORIZED);
    }

    /**
     * Check if user can view a subject.
     * ADMIN can view all subjects.
     * WORKSPACE_ADMIN can view subjects in their workspace.
     * PROFESSOR can view all subjects.
     */
    public void checkCanViewSubject(Subject subject, String userId) {
        User user = userService.get(userId);
        
        // ADMIN and PROFESSOR can view all subjects
        if (user.isUserType(UserType.ADMIN) || user.isUserType(UserType.PROFESSOR)) {
            return;
        }
        
        // WORKSPACE_ADMIN can view subjects in their workspace
        if (user.isUserType(UserType.WORKSPACE_ADMIN)) {
            if (subject.getWorkspace() != null && 
                user.getWorkspace() != null && 
                subject.getWorkspace().getId().equals(user.getWorkspace().getId())) {
                return;
            }
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
        // WORKSPACE_ADMIN can manage courses in their workspace
        if (user.isUserType(UserType.WORKSPACE_ADMIN)) {
            if (course.getSubject() != null && 
                course.getSubject().getWorkspace() != null &&
                user.getWorkspace() != null &&
                course.getSubject().getWorkspace().getId().equals(user.getWorkspace().getId())) {
                return;
            }
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
        // WORKSPACE_ADMIN can view courses in their workspace
        if (user.isUserType(UserType.WORKSPACE_ADMIN)) {
            if (course.getSubject() != null && 
                course.getSubject().getWorkspace() != null &&
                user.getWorkspace() != null &&
                course.getSubject().getWorkspace().getId().equals(user.getWorkspace().getId())) {
                return;
            }
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
        // WORKSPACE_ADMIN can view all projects in courses from their workspace
        if (user.isUserType(UserType.WORKSPACE_ADMIN)) {
            if (course.getSubject() != null && 
                course.getSubject().getWorkspace() != null &&
                user.getWorkspace() != null &&
                course.getSubject().getWorkspace().getId().equals(user.getWorkspace().getId())) {
                return true;
            }
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
     * Professors (subject owners and course owners) and admins can also modify status.
     */
    public void checkCanModifyTaskStatus(org.trackdev.api.entity.Task task, String userId) {
        // Assigned user can modify status
        if (isTaskAssignee(task, userId)) {
            return;
        }
        // Course owner (professor) can modify status
        Course course = task.getProject().getCourse();
        if (course.getOwnerId().equals(userId)) {
            return;
        }
        // Subject owner (professor) can modify status
        Subject subject = course.getSubject();
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
     * Only the assigned user, course owner (professor), subject owner (professor), or admin can edit.
     */
    public void checkCanEditTask(org.trackdev.api.entity.Task task, String userId) {
        // Assigned user can edit
        if (isTaskAssignee(task, userId)) {
            return;
        }
        // Course owner (professor) can edit
        Course course = task.getProject().getCourse();
        if (course.getOwnerId().equals(userId)) {
            return;
        }
        // Subject owner (professor) can edit
        Subject subject = course.getSubject();
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
        // Course owner (professor) can edit
        Course course = task.getProject().getCourse();
        if (course.getOwnerId().equals(userId)) {
            return true;
        }
        // Subject owner (professor) can edit
        Subject subject = course.getSubject();
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

    // WORKSPACES

    /**
     * Check if user can view all workspaces.
     * Only ADMIN users can view all workspaces.
     */
    public void checkCanViewAllWorkspaces(String userId) {
        User user = userService.get(userId);
        if (!user.isUserType(UserType.ADMIN)) {
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
    }

    /**
     * Check if user can view a workspace.
     * ADMIN can view any workspace.
     * WORKSPACE_ADMIN and other users can view workspaces they belong to.
     */
    public void checkCanViewWorkspace(Workspace workspace, String userId) {
        User user = userService.get(userId);
        // Admin can view any workspace
        if (user.isUserType(UserType.ADMIN)) {
            return;
        }
        // Users can view their own workspace
        if (user.getWorkspaceId() != null && user.getWorkspaceId().equals(workspace.getId())) {
            return;
        }
        throw new ServiceException(ErrorConstants.UNAUTHORIZED);
    }

    /**
     * Check if user can create a workspace.
     * Only ADMIN users can create workspaces.
     */
    public void checkCanCreateWorkspace(String userId) {
        User user = userService.get(userId);
        if (!user.isUserType(UserType.ADMIN)) {
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
    }

    /**
     * Check if user can manage (edit/delete) a workspace.
     * ADMIN can manage any workspace.
     * WORKSPACE_ADMIN can manage their own workspace.
     */
    public void checkCanManageWorkspace(Workspace workspace, String userId) {
        User user = userService.get(userId);
        // Admin can manage any workspace
        if (user.isUserType(UserType.ADMIN)) {
            return;
        }
        // Workspace admin can manage their own workspace
        if (user.isUserType(UserType.WORKSPACE_ADMIN) 
            && user.getWorkspaceId() != null 
            && user.getWorkspaceId().equals(workspace.getId())) {
            return;
        }
        throw new ServiceException(ErrorConstants.UNAUTHORIZED);
    }

    // USER CREATION

    /**
     * Check if user can create another user.
     * According to entity constraints:
     * - ADMIN can create ADMIN (no additional info) or WORKSPACE_ADMIN (needs workspace)
     * - ADMIN cannot create STUDENT or PROFESSOR
     * - WORKSPACE_ADMIN can only create PROFESSOR (auto-assigned to their workspace)
     * - PROFESSOR can only create STUDENT for courses they own
     */
    public void checkCanCreateUser(User currentUser, UserType targetUserType, Long targetWorkspaceId, Long targetCourseId) {
        // Admin can create ADMIN or WORKSPACE_ADMIN
        if (currentUser.isUserType(UserType.ADMIN)) {
            // Admin can only create ADMIN or WORKSPACE_ADMIN
            if (targetUserType != UserType.ADMIN && targetUserType != UserType.WORKSPACE_ADMIN) {
                throw new ServiceException(ErrorConstants.ADMIN_CAN_ONLY_CREATE_ADMIN_OR_WORKSPACE_ADMIN);
            }
            // WORKSPACE_ADMIN requires workspace selection
            if (targetUserType == UserType.WORKSPACE_ADMIN && targetWorkspaceId == null) {
                throw new ServiceException(ErrorConstants.WORKSPACE_REQUIRED);
            }
            return;
        }
        
        // Workspace admin can only create PROFESSOR
        if (currentUser.isUserType(UserType.WORKSPACE_ADMIN)) {
            // Can only create PROFESSOR
            if (targetUserType != UserType.PROFESSOR) {
                throw new ServiceException(ErrorConstants.WORKSPACE_ADMIN_CAN_ONLY_CREATE_PROFESSOR);
            }
            // Professor is auto-assigned to the workspace admin's workspace, no workspace/course selection needed
            return;
        }
        
        // Professor can only create STUDENT for courses they own
        if (currentUser.isUserType(UserType.PROFESSOR)) {
            // Can only create STUDENT
            if (targetUserType != UserType.STUDENT) {
                throw new ServiceException(ErrorConstants.PROFESSOR_CAN_ONLY_CREATE_STUDENTS);
            }
            
            // Must specify a course
            if (targetCourseId == null) {
                throw new ServiceException(ErrorConstants.COURSE_REQUIRED);
            }
            
            // Must own the course
            Course course = courseService.get(targetCourseId);
            if (!course.getOwnerId().equals(currentUser.getId())) {
                throw new ServiceException(ErrorConstants.UNAUTHORIZED);
            }
            
            return;
        }
        
        throw new ServiceException(ErrorConstants.UNAUTHORIZED);
    }

    // USER MANAGEMENT

    /**
     * Check if current user can manage (edit/delete) a target user.
     * - ADMIN can manage any user except themselves (for delete)
     * - WORKSPACE_ADMIN can manage PROFESSOR users in their own workspace
     */
    public void checkCanManageWorkspaceUser(User currentUser, User targetUser) {
        // Cannot delete/edit yourself (safety check)
        if (currentUser.getId().equals(targetUser.getId())) {
            throw new ServiceException(ErrorConstants.CANNOT_MANAGE_SELF);
        }
        
        // ADMIN can manage any user
        if (currentUser.isUserType(UserType.ADMIN)) {
            return;
        }
        
        // WORKSPACE_ADMIN can manage PROFESSOR users in their workspace
        if (currentUser.isUserType(UserType.WORKSPACE_ADMIN)) {
            // Must be a PROFESSOR
            if (!targetUser.isUserType(UserType.PROFESSOR)) {
                throw new ServiceException(ErrorConstants.UNAUTHORIZED);
            }
            
            // Must be in the same workspace
            if (currentUser.getWorkspace() == null || targetUser.getWorkspace() == null) {
                throw new ServiceException(ErrorConstants.UNAUTHORIZED);
            }
            
            if (!currentUser.getWorkspace().getId().equals(targetUser.getWorkspace().getId())) {
                throw new ServiceException(ErrorConstants.UNAUTHORIZED);
            }
            
            return;
        }
        
        throw new ServiceException(ErrorConstants.UNAUTHORIZED);
    }

}
