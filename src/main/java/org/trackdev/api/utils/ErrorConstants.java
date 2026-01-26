package org.trackdev.api.utils;

/**
 * Error message keys for i18n support.
 * All values are message keys that will be resolved using MessageSource.
 */
public final class ErrorConstants {

    // User errors
    public static final String USER_MAIL_NOT_FOUND = "error.user.mail.not.found";
    public static final String USER_NOT_FOUND = "error.user.not.found";
    public static final String USER_ALREADY_EXIST = "error.user.already.exists";
    public static final String LOGIN_KO = "error.login.failed";
    public static final String USER_DISABLED = "error.user.disabled";
    public static final String REGISTER_KO = "error.register.failed";
    public static final String RECOVERY_CODE_NOT_MATCH = "error.recovery.code.invalid";
    public static final String USER_SHOULD_NOT_LOGEDIN = "error.user.should.not.be.logged.in";
    public static final String USER_NOT_LOGGED_IN = "error.user.not.logged.in";
    public static final String UNKNOWN_ROLE = "error.role.unknown";
    
    // Validation errors
    public static final String INVALID_PRJ_NAME_LENGTH = "error.project.name.length";
    public static final String INVALID_COURSE_START_YEAR = "error.course.year.invalid";
    public static final String INVALID_PRJ_QUALIFICATION = "error.project.qualification.invalid";
    public static final String INVALID_SPRINT_NAME_LENGTH = "error.sprint.name.length";
    public static final String INVALID_SUBJECT_NAME_LENGTH = "error.subject.name.length";
    public static final String INVALID_SUBJECT_ACRONYM_LENGTH = "error.subject.acronym.length";
    public static final String INVALID_TASK_NAME_LENGTH = "error.task.name.length";
    public static final String PASSWORD_MINIUM_LENGTH = "error.password.min.length";
    public static final String INVALID_MAIL_FORMAT = "error.email.format.invalid";
    public static final String INVALID_MAIL_SIZE = "error.email.size.invalid";
    public static final String INVALID_USERNAME_SIZE = "error.username.size.invalid";
    public static final String INVALID_USERNAME_FORMAT = "error.username.format.invalid";
    public static final String INVALID_FULL_NAME_SIZE = "error.fullname.size.invalid";
    public static final String INVALID_PASSWORD_FORMAT = "error.password.format.invalid";
    
    // Authorization errors
    public static final String UNAUTHORIZED = "error.unauthorized";
    
    // Task errors
    public static final String TASK_ALREADY_REVIEWED = "error.task.already.reviewed";
    public static final String ONLY_ASSIGNEE_CAN_CREATE_SUBTASK = "error.task.subtask.only.assignee";
    public static final String ONLY_ASSIGNEE_CAN_MODIFY_STATUS = "error.task.status.only.assignee";
    public static final String ONLY_ASSIGNEE_CAN_EDIT_TASK = "error.task.edit.only.assignee";
    public static final String TASK_ALREADY_ASSIGNED = "error.task.already.assigned";
    public static final String ONLY_USER_STORY_CAN_HAVE_SUBTASKS = "error.task.subtasks.user.story.only";
    public static final String USER_STORY_CANNOT_BE_DONE_WITH_PENDING_SUBTASKS = "error.task.user.story.pending.subtasks";
    public static final String TASK_CANNOT_BE_DONE_WITHOUT_ESTIMATION = "error.task.done.requires.estimation";
    public static final String TASK_CANNOT_BE_DONE_WITHOUT_PULL_REQUEST = "error.task.done.requires.pr";
    public static final String TASK_CANNOT_BE_DONE_WITHOUT_MERGED_PRS = "error.task.done.requires.merged.prs";
    public static final String TASK_CANNOT_VERIFY_WITHOUT_PULL_REQUEST = "error.task.verify.requires.pr";
    public static final String USER_STORY_WITH_CHILDREN_CANNOT_CHANGE_TYPE = "error.task.user.story.has.children";
    public static final String USER_STORY_ESTIMATION_IS_CALCULATED = "error.task.user.story.estimation.auto";
    public static final String ESTIMATION_ONLY_IN_VERIFY_OR_DONE = "error.task.estimation.verify.or.done";
    public static final String SUBTASK_MUST_BE_TASK_OR_BUG = "error.task.subtask.type.invalid";
    public static final String TASK_IS_FROZEN = "error.task.frozen";
    public static final String ONLY_PROFESSOR_CAN_FREEZE_TASK = "error.task.freeze.professor.only";
    
    // Project errors
    public static final String PRJ_WITHOUT_MEMBERS = "error.project.no.members";
    public static final String PRJ_WITHOUT_QUALIFICATION = "error.project.no.qualification";
    public static final String USER_NOT_PRJ_MEMBER = "error.project.not.member";
    public static final String CANNOT_GENERATE_UNIQUE_SLUG = "error.project.slug.generation.failed";
    public static final String CANNOT_REMOVE_MEMBER_HAS_ASSIGNED_TASKS = "error.project.member.has.tasks";
    public static final String PROJECT_HAS_TASKS = "error.project.has.tasks";
    
    // Subject errors
    public static final String SUBJECT_NOT_EXIST = "error.subject.not.found";
    public static final String SUBJECT_HAS_COURSES = "error.subject.has.courses";
    
    // Entity errors
    public static final String CAN_NOT_BE_NULL = "error.field.null";
    public static final String ENTITY_NOT_EXIST = "error.entity.not.found";
    
    // GitHub errors
    public static final String GITHUB_TOKEN_INVALID = "error.github.token.invalid";
    public static final String API_GITHUB_KO = "error.github.api.failed";
    public static final String GITHUB_REPO_ALREADY_EXISTS = "error.github.repo.exists";
    public static final String GITHUB_REPO_NOT_FOUND = "error.github.repo.not.found";
    public static final String GITHUB_REPO_ACCESS_DENIED = "error.github.repo.access.denied";
    public static final String GITHUB_WEBHOOK_CREATE_FAILED = "error.github.webhook.create.failed";
    public static final String GITHUB_WEBHOOK_DELETE_FAILED = "error.github.webhook.delete.failed";
    public static final String INVALID_GITHUB_URL = "error.github.url.invalid";
    
    // Course errors
    public static final String COURSE_ALREADY_EXISTS = "error.course.already.exists";
    
    // Sprint errors
    public static final String SPRINT_NOT_IN_PROJECT = "error.sprint.not.in.project";
    public static final String USER_STORY_CANNOT_BE_ASSIGNED_TO_SPRINT = "error.sprint.user.story.assignment";
    public static final String TASK_CAN_ONLY_BE_IN_ONE_SPRINT = "error.sprint.task.one.sprint";
    public static final String CANNOT_REASSIGN_DONE_TASK = "error.sprint.done.task.reassign";
    public static final String SPRINT_NOT_ACTIVE_OR_FUTURE = "error.sprint.not.active.or.future";
    public static final String SPRINT_END_DATE_BEFORE_START = "error.sprint.end.before.start";
    public static final String SPRINT_PATTERN_ALREADY_APPLIED = "error.sprint.pattern.already.applied";
    public static final String SPRINT_PATTERN_NOT_IN_COURSE = "error.sprint.pattern.not.in.course";
    
    // Workspace errors
    public static final String WORKSPACE_NOT_EXIST = "error.workspace.not.found";
    public static final String INVALID_WORKSPACE_NAME_LENGTH = "error.workspace.name.length";
    public static final String WORKSPACE_ADMIN_CANNOT_CREATE_USER_TYPE = "error.workspace.admin.user.type";
    public static final String WORKSPACE_ADMIN_CAN_ONLY_CREATE_PROFESSOR = "error.workspace.admin.professor.only";
    public static final String WORKSPACE_REQUIRED = "error.workspace.required";
    
    // Admin user creation errors
    public static final String ADMIN_CAN_ONLY_CREATE_ADMIN_OR_WORKSPACE_ADMIN = "error.admin.create.user.type";
    
    // Professor user creation errors
    public static final String PROFESSOR_CAN_ONLY_CREATE_STUDENTS = "error.professor.create.students.only";
    public static final String COURSE_REQUIRED = "error.course.required";
    
    // Course invite errors
    public static final String INVITE_NOT_FOUND = "error.invite.not.found";
    public static final String INVITE_ALREADY_USED = "error.invite.already.used";
    public static final String INVITE_EXPIRED = "error.invite.expired";
    public static final String INVITE_CANCELLED = "error.invite.cancelled";
    public static final String INVITE_CANNOT_CANCEL = "error.invite.cannot.cancel";
    public static final String STUDENT_NOT_ENROLLED = "error.student.not.enrolled";
    public static final String STUDENT_ALREADY_ENROLLED = "error.student.already.enrolled";
    
    // Profile validation errors
    public static final String PROFILE_ALREADY_APPLIED = "A profile has already been applied to this project";
    public static final String PROFILE_NOT_IN_COURSE = "Profile does not belong to the project's course";
    
    // User deletion errors
    public static final String CANNOT_DELETE_USER_HAS_SUBJECTS = "error.user.delete.has.subjects";
    public static final String CANNOT_DELETE_USER_HAS_REPORTED_TASKS = "error.user.delete.has.reported.tasks";
    public static final String CANNOT_DELETE_USER_HAS_ASSIGNED_TASKS = "error.user.delete.has.assigned.tasks";
    public static final String CANNOT_DELETE_USER_HAS_COMMENTS = "error.user.delete.has.comments";
    public static final String CANNOT_DELETE_USER_HAS_SENT_INVITES = "error.user.delete.has.invites";
    public static final String CANNOT_DELETE_USER_HAS_OWNED_COURSES = "error.user.delete.has.courses";
    public static final String CANNOT_MANAGE_SELF = "error.user.manage.self";
    
    // Password reset errors
    public static final String INVALID_RESET_TOKEN = "error.password.reset.token.invalid";
    public static final String EXPIRED_RESET_TOKEN = "error.password.reset.token.expired";
    
    // Profile errors
    public static final String PROFILE_ALREADY_APPLIED = "error.profile.already.applied";
    
    // Profile errors
    public static final String PROFILE_NOT_EXIST = "Profile does not exist";
    public static final String PROFILE_NAME_ALREADY_EXISTS = "A profile with this name already exists";
    public static final String INVALID_PROFILE_NAME_LENGTH = "Profile name must be between 1 and 100 characters";
    public static final String PROFILE_ENUM_NAME_ALREADY_EXISTS = "An enum with this name already exists in the profile";
    public static final String PROFILE_ATTRIBUTE_NAME_ALREADY_EXISTS = "An attribute with this name already exists in the profile";
    public static final String PROFILE_ENUM_REF_NOT_FOUND = "Referenced enum not found in profile";
    public static final String PROFILE_ENUM_REF_REQUIRED = "Enum reference is required when attribute type is ENUM";
    
    public static final String EMPTY = "";
}
