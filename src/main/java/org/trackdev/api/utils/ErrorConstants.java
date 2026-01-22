package org.trackdev.api.utils;

public final class ErrorConstants {

    public static final String USER_MAIL_NOT_FOUND = "No user exists with this email address";
    public static final String USER_NOT_FOUND = "User does not exist <%s>";
    public static final String USER_ALREADY_EXIST = "User already exists";
    public static final String LOGIN_KO = "Email or password is incorrect";
    public static final String USER_DISABLED = "User is not enabled, contact the administrator";
    public static final String REGISTER_KO = "Error registering the user";
    public static final String RECOVERY_CODE_NOT_MATCH = "The recovery code is incorrect";
    public static final String USER_SHOULD_NOT_LOGEDIN = "User should not be authenticated";
    public static final String USER_NOT_LOGGED_IN = "User must be authenticated";
    public static final String UNKNOWN_ROLE = "Unknown role";
    public static final String INVALID_PRJ_NAME_LENGTH = "Project name must be between 1 and 50 characters";
    public static final String INVALID_COURSE_START_YEAR = "The selected year is not valid";

    public static final String INVALID_PRJ_QUALIFICATION = "Qualification must be a value between 0 and 10";
    public static final String INVALID_SPRINT_NAME_LENGTH = "Sprint name must be between 1 and 50 characters";
    public static final String INVALID_SUBJECT_NAME_LENGTH = "Subject name must be between 1 and 50 characters";
    public static final String INVALID_SUBJECT_ACRONYM_LENGTH = "Subject acronym must be between 3 and 5 characters";
    public static final String INVALID_TASK_NAME_LENGTH = "Task name must be between 1 and 100 characters";
    public static final String PASSWORD_MINIUM_LENGTH = "Password must have at least 8 characters";
    public static final String INVALID_MAIL_FORMAT = "Email address format is not valid";
    public static final String INVALID_MAIL_SIZE = "Email address length is not valid";
    public static final String INVALID_USERNAME_SIZE = "Username length must be between 1 and 50 characters";
    public static final String INVALID_USERNAME_FORMAT = "Username can only contain alphanumeric characters, hyphens (-), underscores (_), and hash (#)";
    public static final String INVALID_FULL_NAME_SIZE = "Full name length must be between 1 and 100 characters";
    public static final String INVALID_PASSWORD_FORMAT = "Password must contain at least one lowercase letter, one uppercase letter, and one number";
    public static final String UNAUTHORIZED = "User is not authorized to perform this action or view this resource";
    public static final String TASK_ALREADY_REVIEWED = "This user has already reviewed the points for this task";
    public static final String PRJ_WITHOUT_MEMBERS = "Project must have at least one member";
    public static final String PRJ_WITHOUT_QUALIFICATION = "Cannot calculate the grade of a project without qualification";
    public static final String SUBJECT_NOT_EXIST = "Subject does not exist";
    public static final String USER_NOT_PRJ_MEMBER = "User is not a member of the project";
    public static final String ONLY_ASSIGNEE_CAN_CREATE_SUBTASK = "Only the assigned user can create subtasks for this task";
    public static final String ONLY_ASSIGNEE_CAN_MODIFY_STATUS = "Only the assigned user can modify the status of this task";
    public static final String ONLY_ASSIGNEE_CAN_EDIT_TASK = "Only the assigned user can edit this task";
    public static final String TASK_ALREADY_ASSIGNED = "Task is already assigned to another user";
    public static final String ONLY_USER_STORY_CAN_HAVE_SUBTASKS = "Only tasks of type USER_STORY can have subtasks";
    public static final String USER_STORY_CANNOT_BE_DONE_WITH_PENDING_SUBTASKS = "A USER_STORY cannot be marked as DONE while it has subtasks that are not DONE";
    public static final String TASK_CANNOT_BE_DONE_WITHOUT_ESTIMATION = "A task cannot be marked as DONE without estimation points";
    public static final String TASK_CANNOT_BE_DONE_WITHOUT_PULL_REQUEST = "A task cannot be marked as DONE without at least one Pull Request";
    public static final String TASK_CANNOT_BE_DONE_WITHOUT_MERGED_PRS = "A task cannot be marked as DONE until all Pull Requests are merged";
    public static final String TASK_CANNOT_VERIFY_WITHOUT_PULL_REQUEST = "A task cannot be moved to VERIFY without at least one Pull Request";
    public static final String USER_STORY_WITH_CHILDREN_CANNOT_CHANGE_TYPE = "A USER_STORY with child tasks cannot change its type";
    public static final String USER_STORY_ESTIMATION_IS_CALCULATED = "USER_STORY estimation points are automatically calculated from subtasks and cannot be set manually";
    public static final String ESTIMATION_ONLY_IN_VERIFY_OR_DONE = "Estimation points can only be set when task is in Verification or Done status";
    public static final String SUBTASK_MUST_BE_TASK_OR_BUG = "A subtask can only be of type TASK or BUG";
    public static final String TASK_IS_FROZEN = "This task is frozen and cannot be modified";
    public static final String ONLY_PROFESSOR_CAN_FREEZE_TASK = "Only professors can freeze or unfreeze tasks";
    public static final String CAN_NOT_BE_NULL = "There is a NULL field that cannot be null";
    public static final String ENTITY_NOT_EXIST = "Entity does not exist";
    public static final String GITHUB_TOKEN_INVALID = "ERROR: Invalid token";
    public static final String API_GITHUB_KO = "ERROR: GitHub API is not working";
    public static final String GITHUB_REPO_ALREADY_EXISTS = "This GitHub repository is already linked to the project";
    public static final String GITHUB_REPO_NOT_FOUND = "GitHub repository not found";
    public static final String GITHUB_REPO_ACCESS_DENIED = "Access denied to GitHub repository. Check your token permissions.";
    public static final String GITHUB_WEBHOOK_CREATE_FAILED = "Failed to create webhook on GitHub repository";
    public static final String GITHUB_WEBHOOK_DELETE_FAILED = "Failed to delete webhook from GitHub repository";
    public static final String INVALID_GITHUB_URL = "Invalid GitHub repository URL";
    public static final String SUBJECT_HAS_COURSES = "Cannot delete subject that has associated courses";
    public static final String COURSE_ALREADY_EXISTS = "A course already exists for this subject and year";
    public static final String SPRINT_NOT_IN_PROJECT = "Sprint does not belong to the same project as the task";
    public static final String USER_STORY_CANNOT_BE_ASSIGNED_TO_SPRINT = "USER_STORY can only be assigned to a sprint when all its subtasks are unassigned from any sprint";
    public static final String TASK_CAN_ONLY_BE_IN_ONE_SPRINT = "A task can only be assigned to one sprint at a time";
    public static final String CANNOT_REASSIGN_DONE_TASK = "Tasks in DONE status cannot be reassigned to another sprint";
    public static final String SPRINT_NOT_ACTIVE_OR_FUTURE = "Tasks can only be assigned to active or future sprints";
    
    // Workspace errors
    public static final String WORKSPACE_NOT_EXIST = "Workspace does not exist";
    public static final String INVALID_WORKSPACE_NAME_LENGTH = "Workspace name must be between 1 and 100 characters";
    public static final String WORKSPACE_ADMIN_CANNOT_CREATE_USER_TYPE = "Workspace administrators can only create Professor and Student users";
    public static final String WORKSPACE_ADMIN_CAN_ONLY_CREATE_PROFESSOR = "Workspace administrators can only create Professor users";
    public static final String WORKSPACE_REQUIRED = "Workspace ID is required for this operation";
    
    // Admin user creation errors
    public static final String ADMIN_CAN_ONLY_CREATE_ADMIN_OR_WORKSPACE_ADMIN = "Administrators can only create Administrator or Workspace Administrator users";
    
    // Professor user creation errors
    public static final String PROFESSOR_CAN_ONLY_CREATE_STUDENTS = "Professors can only create Student users";
    public static final String COURSE_REQUIRED = "Course ID is required for this operation";
    
    // Course invite errors
    public static final String INVITE_NOT_FOUND = "Invitation not found or invalid token";
    public static final String INVITE_ALREADY_USED = "This invitation has already been used";
    public static final String INVITE_EXPIRED = "This invitation has expired";
    public static final String INVITE_CANCELLED = "This invitation has been cancelled";
    public static final String INVITE_CANNOT_CANCEL = "Only pending invitations can be cancelled";
    public static final String STUDENT_NOT_ENROLLED = "Student is not enrolled in this course";
    public static final String STUDENT_ALREADY_ENROLLED = "Student is already enrolled in this course";
    
    // Project errors
    public static final String CANNOT_GENERATE_UNIQUE_SLUG = "Unable to generate a unique project ID. Please try again.";
    public static final String CANNOT_REMOVE_MEMBER_HAS_ASSIGNED_TASKS = "Cannot remove member who has assigned tasks in this project";
    
    // Sprint validation errors
    public static final String SPRINT_END_DATE_BEFORE_START = "Sprint end date must be after start date";
    public static final String SPRINT_PATTERN_ALREADY_APPLIED = "A sprint pattern has already been applied to this project";
    public static final String SPRINT_PATTERN_NOT_IN_COURSE = "Sprint pattern does not belong to the project's course";
    
    // User deletion errors
    public static final String CANNOT_DELETE_USER_HAS_SUBJECTS = "Cannot delete user who owns subjects";
    public static final String CANNOT_DELETE_USER_HAS_REPORTED_TASKS = "Cannot delete user who has reported tasks";
    public static final String CANNOT_DELETE_USER_HAS_ASSIGNED_TASKS = "Cannot delete user who has assigned tasks";
    public static final String CANNOT_DELETE_USER_HAS_COMMENTS = "Cannot delete user who has authored comments";
    public static final String CANNOT_DELETE_USER_HAS_SENT_INVITES = "Cannot delete user who has sent course invites";
    public static final String CANNOT_DELETE_USER_HAS_OWNED_COURSES = "Cannot delete user who owns courses";
    public static final String CANNOT_MANAGE_SELF = "You cannot delete or edit your own account from this interface";
    
    public static final String EMPTY = "";
}
