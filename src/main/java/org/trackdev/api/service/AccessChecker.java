package org.trackdev.api.service;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.trackdev.api.configuration.UserType;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.Course;
import org.trackdev.api.entity.Project;
import org.trackdev.api.entity.Sprint;
import org.trackdev.api.entity.SprintStatus;
import org.trackdev.api.entity.Subject;
import org.trackdev.api.entity.TaskStatus;
import org.trackdev.api.entity.TaskType;
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
        // Admins can view all projects
        if (userService.get(userId).isUserType(UserType.ADMIN)) {
            return;
        }
        // Note: Students enrolled in the course do NOT automatically get access to all projects
        // They must be explicit members of the project to view it
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
     * Check if user is the reporter (creator) of a task.
     * Returns true if the user created this task.
     */
    public boolean isTaskReporter(org.trackdev.api.entity.Task task, String userId) {
        return task.getReporter() != null && task.getReporter().getId().equals(userId);
    }

    /**
     * Check if user can create subtasks for a task.
     * Any project member can create subtasks for a USER_STORY.
     * Professors (subject owners) and admins can also create subtasks.
     */
    public void checkCanCreateSubtask(org.trackdev.api.entity.Task task, String userId) {
        // Project members can create subtasks
        if (task.getProject().isMember(userId)) {
            return;
        }
        // Subject owner (professor) can create subtasks
        Subject subject = task.getProject().getCourse().getSubject();
        if (isSubjectOwner(subject, userId)) {
            return;
        }
        // Course owner (professor) can create subtasks
        Course course = task.getProject().getCourse();
        if (course.getOwnerId().equals(userId)) {
            return;
        }
        // Admin can create subtasks
        if (userService.get(userId).isUserType(UserType.ADMIN)) {
            return;
        }
        throw new ServiceException(ErrorConstants.UNAUTHORIZED);
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
     * Students: must be the assignee OR the reporter AND be a project member
     * Professors: course owner or subject owner can edit any task in their course
     * Admin: can edit any task
     */
    public void checkCanEditTask(org.trackdev.api.entity.Task task, String userId) {
        Project project = task.getProject();
        Course course = project.getCourse();
        
        // Admin can edit any task
        if (userService.get(userId).isUserType(UserType.ADMIN)) {
            return;
        }
        
        // Course owner (professor) can edit any task in their course
        if (course.getOwnerId().equals(userId)) {
            return;
        }
        
        // Subject owner (professor) can edit any task in their subject's courses
        Subject subject = course.getSubject();
        if (isSubjectOwner(subject, userId)) {
            return;
        }
        
        // For students: must be a project member AND be the assignee OR reporter
        if (project.isMember(userId)) {
            if (isTaskAssignee(task, userId) || isTaskReporter(task, userId)) {
                return;
            }
            throw new ServiceException(ErrorConstants.ONLY_ASSIGNEE_CAN_EDIT_TASK);
        }
        
        // User is not a project member - unauthorized
        throw new ServiceException(ErrorConstants.UNAUTHORIZED);
    }

    /**
     * Check if user can edit a task (returns boolean, doesn't throw).
     * Students: must be the assignee OR the reporter AND be a project member
     * Professors: course owner or subject owner can edit any task in their course
     * Admin: can edit any task
     */
    public boolean canEditTask(org.trackdev.api.entity.Task task, String userId) {
        Project project = task.getProject();
        Course course = project.getCourse();
        
        // Admin can edit any task
        if (userService.get(userId).isUserType(UserType.ADMIN)) {
            return true;
        }
        
        // Course owner (professor) can edit any task in their course
        if (course.getOwnerId().equals(userId)) {
            return true;
        }
        
        // Subject owner (professor) can edit any task in their subject's courses
        Subject subject = course.getSubject();
        if (isSubjectOwner(subject, userId)) {
            return true;
        }
        
        // For students: must be a project member AND be the assignee OR reporter
        if (project.isMember(userId) && (isTaskAssignee(task, userId) || isTaskReporter(task, userId))) {
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

    /**
     * Check if current user can manage (edit) a student in their courses.
     * - ADMIN can manage any student
     * - WORKSPACE_ADMIN can manage students in their workspace
     * - PROFESSOR can manage STUDENT users enrolled in courses they own
     */
    public void checkCanManageCourseStudent(User currentUser, User targetUser) {
        // Cannot edit yourself
        if (currentUser.getId().equals(targetUser.getId())) {
            throw new ServiceException(ErrorConstants.CANNOT_MANAGE_SELF);
        }
        
        // Target must be a STUDENT
        if (!targetUser.isUserType(UserType.STUDENT)) {
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
        
        // ADMIN can manage any student
        if (currentUser.isUserType(UserType.ADMIN)) {
            return;
        }
        
        // WORKSPACE_ADMIN can manage students in their workspace
        if (currentUser.isUserType(UserType.WORKSPACE_ADMIN)) {
            if (currentUser.getWorkspace() != null && targetUser.getWorkspace() != null &&
                currentUser.getWorkspace().getId().equals(targetUser.getWorkspace().getId())) {
                return;
            }
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
        
        // PROFESSOR can manage students enrolled in courses they own
        if (currentUser.isUserType(UserType.PROFESSOR)) {
            Collection<Course> ownedCourses = courseService.getCoursesForUser(currentUser.getId());
            for (Course course : ownedCourses) {
                if (course.isStudentEnrolled(targetUser.getId())) {
                    return;
                }
            }
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
        
        throw new ServiceException(ErrorConstants.UNAUTHORIZED);
    }

    // USER VIEWING

    /**
     * Check if current user can view information about a target user.
     * - ADMIN can view any user
     * - WORKSPACE_ADMIN can view users in their workspace
     * - PROFESSOR can view professors in their workspace + students in courses they own
     * - STUDENT can view other students in the same projects
     */
    public void checkCanViewUser(String currentUserId, User targetUser) {
        User currentUser = userService.get(currentUserId);
        
        // Users can always view themselves
        if (currentUser.getId().equals(targetUser.getId())) {
            return;
        }
        
        // ADMIN can view any user
        if (currentUser.isUserType(UserType.ADMIN)) {
            return;
        }
        
        // WORKSPACE_ADMIN can view users in their workspace
        if (currentUser.isUserType(UserType.WORKSPACE_ADMIN)) {
            if (currentUser.getWorkspace() != null && targetUser.getWorkspace() != null &&
                currentUser.getWorkspace().getId().equals(targetUser.getWorkspace().getId())) {
                return;
            }
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
        
        // PROFESSOR can view:
        // 1. Other professors in their workspace
        // 2. Students enrolled in courses they own
        if (currentUser.isUserType(UserType.PROFESSOR)) {
            // Can view other professors in same workspace
            if (targetUser.isUserType(UserType.PROFESSOR) || targetUser.isUserType(UserType.WORKSPACE_ADMIN)) {
                if (currentUser.getWorkspace() != null && targetUser.getWorkspace() != null &&
                    currentUser.getWorkspace().getId().equals(targetUser.getWorkspace().getId())) {
                    return;
                }
            }
            
            // Can view students in courses they own
            if (targetUser.isUserType(UserType.STUDENT)) {
                Collection<Course> ownedCourses = courseService.getCoursesForUser(currentUser.getId());
                for (Course course : ownedCourses) {
                    if (course.isStudentEnrolled(targetUser.getId())) {
                        return;
                    }
                }
            }
            
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
        
        // STUDENT can only view other students in the same projects
        if (currentUser.isUserType(UserType.STUDENT)) {
            // Get all projects the current user is a member of
            Collection<Project> currentUserProjects = currentUser.getProjects();
            
            // Check if target user is a member of any of those projects
            for (Project project : currentUserProjects) {
                if (project.isMember(targetUser.getId())) {
                    return;
                }
            }
            
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
        
        throw new ServiceException(ErrorConstants.UNAUTHORIZED);
    }

    // PROFILES

    /**
     * Check if user can create a profile.
     * Only PROFESSOR can create profiles.
     */
    public void checkCanCreateProfile(User user) {
        if (!user.isUserType(UserType.PROFESSOR)) {
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }
    }

    /**
     * Check if user can view a profile.
     * ADMIN can view any profile.
     * PROFESSOR can view their own profiles.
     */
    public void checkCanViewProfile(org.trackdev.api.entity.Profile profile, String userId) {
        User user = userService.get(userId);
        
        // ADMIN can view any profile
        if (user.isUserType(UserType.ADMIN)) {
            return;
        }
        
        // PROFESSOR can view their own profiles
        if (user.isUserType(UserType.PROFESSOR) && profile.getOwnerId().equals(userId)) {
            return;
        }
        
        throw new ServiceException(ErrorConstants.UNAUTHORIZED);
    }

    /**
     * Check if user can manage (edit/delete) a profile.
     * Only the profile owner can manage it.
     */
    public void checkCanManageProfile(org.trackdev.api.entity.Profile profile, String userId) {
        User user = userService.get(userId);
        
        // ADMIN can manage any profile
        if (user.isUserType(UserType.ADMIN)) {
            return;
        }
        
        // Only the owner can manage the profile
        if (profile.getOwnerId().equals(userId)) {
            return;
        }
        
        throw new ServiceException(ErrorConstants.UNAUTHORIZED);
    }

    // =============================================================================
    // TASK PERMISSION COMPUTATION (for DTO permission flags)
    // =============================================================================

    /**
     * Check if user is a professor with access to the task's project.
     * This includes course owner, subject owner, or admin.
     */
    public boolean isProfessorForTask(org.trackdev.api.entity.Task task, String userId) {
        User user = userService.get(userId);
        if (user.isUserType(UserType.ADMIN)) {
            return true;
        }
        Course course = task.getProject().getCourse();
        if (course.getOwnerId().equals(userId)) {
            return true;
        }
        Subject subject = course.getSubject();
        return isSubjectOwner(subject, userId);
    }

    /**
     * Check if a TASK or BUG is in a past (CLOSED) sprint only.
     * Returns false for USER_STORY or tasks with no sprints.
     */
    public boolean isTaskInPastSprintOnly(org.trackdev.api.entity.Task task) {
        if (task.getTaskType() == TaskType.USER_STORY) {
            return false;
        }
        Collection<Sprint> sprints = task.getActiveSprints();
        if (sprints == null || sprints.isEmpty()) {
            return false;
        }
        // Check if ALL sprints are CLOSED (past)
        return sprints.stream().allMatch(sprint -> sprint.getEffectiveStatus() == SprintStatus.CLOSED);
    }

    /**
     * Compute canEditStatus permission.
     * USER_STORY cannot have status changed manually.
     * TASK/BUG in past sprint only cannot change status (unless professor).
     */
    public boolean canEditStatus(org.trackdev.api.entity.Task task, String userId) {
        // USER_STORY status is computed from children, cannot be changed manually
        if (task.getTaskType() == TaskType.USER_STORY) {
            return false;
        }
        boolean isProfessor = isProfessorForTask(task, userId);
        // If frozen, only professor can edit
        if (task.isFrozen() && !isProfessor) {
            return false;
        }
        // If in past sprint only, only professor can edit
        if (isTaskInPastSprintOnly(task) && !isProfessor) {
            return false;
        }
        // Check basic edit permission
        return canEditTask(task, userId);
    }

    /**
     * Compute canEditSprint permission.
     * TASK/BUG in past sprint can still edit sprint to move to active/future (if not DONE).
     */
    public boolean canEditSprint(org.trackdev.api.entity.Task task, String userId) {
        // USER_STORY sprint is derived from subtasks, cannot be changed directly
        // (unless all subtasks are unassigned)
        if (task.getTaskType() == TaskType.USER_STORY) {
            // Check if USER_STORY has subtasks with sprints
            Collection<org.trackdev.api.entity.Task> children = task.getChildTasks();
            if (children != null && !children.isEmpty()) {
                boolean anySubtaskHasSprint = children.stream()
                    .anyMatch(child -> child.getActiveSprints() != null && !child.getActiveSprints().isEmpty());
                if (anySubtaskHasSprint) {
                    return false;  // Sprint is derived from subtasks
                }
            }
        }
        boolean isProfessor = isProfessorForTask(task, userId);
        // If frozen, only professor can edit
        if (task.isFrozen() && !isProfessor) {
            return false;
        }
        // DONE status tasks cannot change sprint (except professor)
        if (task.getStatus() == TaskStatus.DONE && !isProfessor) {
            return false;
        }
        // Special case: TASK/BUG in past sprint can still edit sprint to escape
        // (this is intentionally more permissive than canEdit)
        if (isTaskInPastSprintOnly(task) && !isProfessor) {
            // Allow sprint change to escape past sprint, if not DONE
            return task.getStatus() != TaskStatus.DONE && canEditTask(task, userId);
        }
        return canEditTask(task, userId);
    }

    /**
     * Compute canEditType permission.
     * USER_STORY and BUG cannot change type.
     * Subtasks can only be TASK or BUG.
     */
    public boolean canEditType(org.trackdev.api.entity.Task task, String userId) {
        // USER_STORY cannot change type
        if (task.getTaskType() == TaskType.USER_STORY) {
            return false;
        }
        // BUG cannot change type
        if (task.getTaskType() == TaskType.BUG) {
            return false;
        }
        boolean isProfessor = isProfessorForTask(task, userId);
        // If frozen, only professor can edit
        if (task.isFrozen() && !isProfessor) {
            return false;
        }
        // If in past sprint only, only professor can edit
        if (isTaskInPastSprintOnly(task) && !isProfessor) {
            return false;
        }
        return canEditTask(task, userId);
    }

    /**
     * Compute canEditEstimation permission.
     * Only TASK and BUG can have estimation edited (USER_STORY is sum of subtasks).
     */
    public boolean canEditEstimation(org.trackdev.api.entity.Task task, String userId) {
        // USER_STORY estimation is sum of subtasks
        if (task.getTaskType() == TaskType.USER_STORY) {
            return false;
        }
        boolean isProfessor = isProfessorForTask(task, userId);
        // If frozen, only professor can edit
        if (task.isFrozen() && !isProfessor) {
            return false;
        }
        // If in past sprint only, only professor can edit
        if (isTaskInPastSprintOnly(task) && !isProfessor) {
            return false;
        }
        return canEditTask(task, userId);
    }

    /**
     * Compute canDelete permission.
     * Only professor or assignee can delete (NOT reporter).
     * USER_STORY can only be deleted if it has no subtasks.
     * TASK/BUG can only be deleted if status is TODO or INPROGRESS.
     */
    public boolean canDeleteTask(org.trackdev.api.entity.Task task, String userId) {
        boolean isProfessor = isProfessorForTask(task, userId);
        // If frozen, only professor can delete
        if (task.isFrozen() && !isProfessor) {
            return false;
        }
        // If in past sprint only, only professor can delete
        if (isTaskInPastSprintOnly(task) && !isProfessor) {
            return false;
        }
        // Only professor or assignee can delete (NOT reporter)
        // This is stricter than canEditTask which allows reporter OR assignee
        if (!isProfessor && !isTaskAssignee(task, userId)) {
            return false;
        }
        // USER_STORY: can only delete if no subtasks
        if (task.getTaskType() == TaskType.USER_STORY) {
            Collection<org.trackdev.api.entity.Task> children = task.getChildTasks();
            return children == null || children.isEmpty();
        }
        // TASK/BUG: can only delete if status is TODO or INPROGRESS
        return task.getStatus() == TaskStatus.TODO || task.getStatus() == TaskStatus.INPROGRESS;
    }

    /**
     * Compute canSelfAssign permission.
     * Only students can self-assign. Task must be unassigned.
     */
    public boolean canSelfAssign(org.trackdev.api.entity.Task task, String userId) {
        User user = userService.get(userId);
        // Only students can self-assign
        if (!user.isUserType(UserType.STUDENT)) {
            return false;
        }
        // Task must be unassigned
        if (task.getAssignee() != null) {
            return false;
        }
        // If frozen, cannot self-assign
        if (task.isFrozen()) {
            return false;
        }
        // If in past sprint only, cannot self-assign
        if (isTaskInPastSprintOnly(task)) {
            return false;
        }
        // Must be a project member
        return task.getProject().isMember(userId);
    }

    /**
     * Compute canUnassign permission.
     * Assignee or professor can unassign.
     */
    public boolean canUnassign(org.trackdev.api.entity.Task task, String userId) {
        // Must have an assignee to unassign
        if (task.getAssignee() == null) {
            return false;
        }
        boolean isProfessor = isProfessorForTask(task, userId);
        // If frozen, only professor can unassign
        if (task.isFrozen() && !isProfessor) {
            return false;
        }
        // If in past sprint only, only professor can unassign
        if (isTaskInPastSprintOnly(task) && !isProfessor) {
            return false;
        }
        // Assignee can unassign themselves
        if (isTaskAssignee(task, userId)) {
            return true;
        }
        // Professor can unassign anyone
        return isProfessor;
    }

    /**
     * Compute canAddSubtask permission.
     * Only USER_STORY can have subtasks. Any project member can add.
     */
    public boolean canAddSubtask(org.trackdev.api.entity.Task task, String userId) {
        // Only USER_STORY can have subtasks
        if (task.getTaskType() != TaskType.USER_STORY) {
            return false;
        }
        boolean isProfessor = isProfessorForTask(task, userId);
        // If frozen, only professor can add subtasks
        if (task.isFrozen() && !isProfessor) {
            return false;
        }
        // Check if user can create subtasks
        return task.getProject().isMember(userId) || isProfessor;
    }

    /**
     * Compute canFreeze permission.
     * Only professors can freeze/unfreeze tasks.
     */
    public boolean canFreeze(org.trackdev.api.entity.Task task, String userId) {
        return isProfessorForTask(task, userId);
    }

    /**
     * Compute canComment permission.
     * Any project member or professor can comment.
     */
    public boolean canComment(org.trackdev.api.entity.Task task, String userId) {
        boolean isProfessor = isProfessorForTask(task, userId);
        // If frozen, only professor can comment
        if (task.isFrozen() && !isProfessor) {
            return false;
        }
        // Project members can comment
        if (task.getProject().isMember(userId)) {
            return true;
        }
        // Professor can comment
        return isProfessor;
    }

}
