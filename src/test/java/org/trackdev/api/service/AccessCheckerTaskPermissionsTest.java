package org.trackdev.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.trackdev.api.configuration.UserType;
import org.trackdev.api.entity.*;

import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for AccessChecker task permission methods.
 * These tests ensure that backend permission checks cannot be bypassed.
 * 
 * Permission rules tested:
 * - canEditStatus: USER_STORY cannot, TASK/BUG in PAST sprint blocked for students
 * - canEditSprint: USER_STORY with subtasks cannot, DONE status blocked, PAST sprint escape allowed
 * - canEditType: USER_STORY and BUG cannot change type
 * - canEditEstimation: Only TASK/BUG can edit (USER_STORY is computed)
 * - canDelete: USER_STORY needs no subtasks, TASK/BUG needs TODO/INPROGRESS
 * - canSelfAssign: Only unassigned, only students, only project members
 * - canUnassign: Only assignee or professor
 * - canAddSubtask: Only USER_STORY, only project members or professor
 * - canFreeze: Only professors
 * - canComment: Project members or professors, frozen blocks students
 */
@ExtendWith(MockitoExtension.class)
class AccessCheckerTaskPermissionsTest {

    @Mock
    private UserService userService;

    @Mock
    private CourseService courseService;

    private AccessChecker accessChecker;

    // Test entities
    private User adminUser;
    private User professorUser;
    private User studentUser;
    private User otherStudentUser;
    private Workspace workspace;
    private Subject subject;
    private Course course;
    private Project project;
    private Task userStoryTask;
    private Task taskTask;
    private Task bugTask;
    private Sprint activeSprint;
    private Sprint closedSprint;
    private Sprint draftSprint;

    @BeforeEach
    void setUp() {
        accessChecker = new AccessChecker();
        ReflectionTestUtils.setField(accessChecker, "userService", userService);
        ReflectionTestUtils.setField(accessChecker, "courseService", courseService);

        // Create workspace
        workspace = new Workspace();
        ReflectionTestUtils.setField(workspace, "id", 1L);

        // Create admin user
        adminUser = new User();
        ReflectionTestUtils.setField(adminUser, "id", "admin-id");
        Role adminRole = new Role(UserType.ADMIN);
        ReflectionTestUtils.setField(adminUser, "roles", Set.of(adminRole));

        // Create professor user
        professorUser = new User();
        ReflectionTestUtils.setField(professorUser, "id", "professor-id");
        Role professorRole = new Role(UserType.PROFESSOR);
        ReflectionTestUtils.setField(professorUser, "roles", Set.of(professorRole));
        professorUser.setWorkspace(workspace);

        // Create student user
        studentUser = new User();
        ReflectionTestUtils.setField(studentUser, "id", "student-id");
        Role studentRole = new Role(UserType.STUDENT);
        ReflectionTestUtils.setField(studentUser, "roles", Set.of(studentRole));
        studentUser.setWorkspace(workspace);

        // Create another student (not assigned, not reporter)
        otherStudentUser = new User();
        ReflectionTestUtils.setField(otherStudentUser, "id", "other-student-id");
        Role otherStudentRole = new Role(UserType.STUDENT);
        ReflectionTestUtils.setField(otherStudentUser, "roles", Set.of(otherStudentRole));
        otherStudentUser.setWorkspace(workspace);

        // Create subject owned by professor
        subject = new Subject("Test Subject", "TS", professorUser);
        ReflectionTestUtils.setField(subject, "id", 1L);
        ReflectionTestUtils.setField(subject, "ownerId", "professor-id");
        subject.setWorkspace(workspace);

        // Create course
        course = new Course(2025);
        ReflectionTestUtils.setField(course, "id", 1L);
        course.setSubject(subject);
        course.setOwner(professorUser);
        ReflectionTestUtils.setField(course, "ownerId", "professor-id");

        // Create project with student as member
        project = new Project("Test Project");
        ReflectionTestUtils.setField(project, "id", 1L);
        project.setCourse(course);
        Set<User> members = new HashSet<>();
        members.add(studentUser);
        ReflectionTestUtils.setField(project, "members", members);

        // Create sprints
        activeSprint = new Sprint();
        ReflectionTestUtils.setField(activeSprint, "id", 1L);
        activeSprint.setStatus(SprintStatus.ACTIVE);
        activeSprint.setStartDate(ZonedDateTime.now().minusDays(7));
        activeSprint.setEndDate(ZonedDateTime.now().plusDays(7));

        closedSprint = new Sprint();
        ReflectionTestUtils.setField(closedSprint, "id", 2L);
        closedSprint.setStatus(SprintStatus.CLOSED);
        closedSprint.setStartDate(ZonedDateTime.now().minusDays(21));
        closedSprint.setEndDate(ZonedDateTime.now().minusDays(7));

        draftSprint = new Sprint();
        ReflectionTestUtils.setField(draftSprint, "id", 3L);
        draftSprint.setStatus(SprintStatus.DRAFT);
        draftSprint.setStartDate(ZonedDateTime.now().plusDays(7));
        draftSprint.setEndDate(ZonedDateTime.now().plusDays(21));

        // Create USER_STORY task
        userStoryTask = createTask(1L, "User Story", TaskType.USER_STORY, TaskStatus.TODO);

        // Create TASK task
        taskTask = createTask(2L, "Task", TaskType.TASK, TaskStatus.TODO);

        // Create BUG task
        bugTask = createTask(3L, "Bug", TaskType.BUG, TaskStatus.TODO);

        // Setup mocks
        lenient().when(userService.get("admin-id")).thenReturn(adminUser);
        lenient().when(userService.get("professor-id")).thenReturn(professorUser);
        lenient().when(userService.get("student-id")).thenReturn(studentUser);
        lenient().when(userService.get("other-student-id")).thenReturn(otherStudentUser);
    }

    private Task createTask(Long id, String name, TaskType type, TaskStatus status) {
        Task task = new Task();
        ReflectionTestUtils.setField(task, "id", id);
        task.setName(name);
        task.setType(type);
        task.setStatus(status);
        task.setProject(project);
        task.setReporter(studentUser);
        task.setAssignee(studentUser);
        task.setFrozen(false);
        ReflectionTestUtils.setField(task, "activeSprints", new ArrayList<>(List.of(activeSprint)));
        return task;
    }

    // =============================================================================
    // isProfessorForTask Tests
    // =============================================================================

    @Nested
    @DisplayName("isProfessorForTask")
    class IsProfessorForTaskTests {

        @Test
        @DisplayName("Admin should be considered professor for any task")
        void admin_shouldBeConsideredProfessor() {
            assertTrue(accessChecker.isProfessorForTask(taskTask, "admin-id"));
        }

        @Test
        @DisplayName("Course owner should be considered professor")
        void courseOwner_shouldBeConsideredProfessor() {
            assertTrue(accessChecker.isProfessorForTask(taskTask, "professor-id"));
        }

        @Test
        @DisplayName("Student should NOT be considered professor")
        void student_shouldNotBeConsideredProfessor() {
            assertFalse(accessChecker.isProfessorForTask(taskTask, "student-id"));
        }
    }

    // =============================================================================
    // isTaskInPastSprintOnly Tests
    // =============================================================================

    @Nested
    @DisplayName("isTaskInPastSprintOnly")
    class IsTaskInPastSprintOnlyTests {

        @Test
        @DisplayName("USER_STORY should never be considered in past sprint only")
        void userStory_shouldNeverBeInPastSprintOnly() {
            ReflectionTestUtils.setField(userStoryTask, "activeSprints", List.of(closedSprint));
            assertFalse(accessChecker.isTaskInPastSprintOnly(userStoryTask));
        }

        @Test
        @DisplayName("TASK in CLOSED sprint should be in past sprint only")
        void taskInClosedSprint_shouldBeInPastSprintOnly() {
            ReflectionTestUtils.setField(taskTask, "activeSprints", List.of(closedSprint));
            assertTrue(accessChecker.isTaskInPastSprintOnly(taskTask));
        }

        @Test
        @DisplayName("TASK in ACTIVE sprint should NOT be in past sprint only")
        void taskInActiveSprint_shouldNotBeInPastSprintOnly() {
            ReflectionTestUtils.setField(taskTask, "activeSprints", List.of(activeSprint));
            assertFalse(accessChecker.isTaskInPastSprintOnly(taskTask));
        }

        @Test
        @DisplayName("TASK with no sprints should NOT be in past sprint only")
        void taskWithNoSprints_shouldNotBeInPastSprintOnly() {
            ReflectionTestUtils.setField(taskTask, "activeSprints", new ArrayList<>());
            assertFalse(accessChecker.isTaskInPastSprintOnly(taskTask));
        }

        @Test
        @DisplayName("BUG in CLOSED sprint should be in past sprint only")
        void bugInClosedSprint_shouldBeInPastSprintOnly() {
            ReflectionTestUtils.setField(bugTask, "activeSprints", List.of(closedSprint));
            assertTrue(accessChecker.isTaskInPastSprintOnly(bugTask));
        }
    }

    // =============================================================================
    // isTaskInFutureSprintOnly Tests
    // =============================================================================

    @Nested
    @DisplayName("isTaskInFutureSprintOnly")
    class IsTaskInFutureSprintOnlyTests {

        @Test
        @DisplayName("USER_STORY should never be considered in future sprint only")
        void userStory_shouldNeverBeInFutureSprintOnly() {
            ReflectionTestUtils.setField(userStoryTask, "activeSprints", List.of(draftSprint));
            assertFalse(accessChecker.isTaskInFutureSprintOnly(userStoryTask));
        }

        @Test
        @DisplayName("TASK in DRAFT sprint should be in future sprint only")
        void taskInDraftSprint_shouldBeInFutureSprintOnly() {
            ReflectionTestUtils.setField(taskTask, "activeSprints", List.of(draftSprint));
            assertTrue(accessChecker.isTaskInFutureSprintOnly(taskTask));
        }

        @Test
        @DisplayName("TASK in ACTIVE sprint should NOT be in future sprint only")
        void taskInActiveSprint_shouldNotBeInFutureSprintOnly() {
            ReflectionTestUtils.setField(taskTask, "activeSprints", List.of(activeSprint));
            assertFalse(accessChecker.isTaskInFutureSprintOnly(taskTask));
        }

        @Test
        @DisplayName("TASK in CLOSED sprint should NOT be in future sprint only")
        void taskInClosedSprint_shouldNotBeInFutureSprintOnly() {
            ReflectionTestUtils.setField(taskTask, "activeSprints", List.of(closedSprint));
            assertFalse(accessChecker.isTaskInFutureSprintOnly(taskTask));
        }

        @Test
        @DisplayName("TASK with no sprints should NOT be in future sprint only")
        void taskWithNoSprints_shouldNotBeInFutureSprintOnly() {
            ReflectionTestUtils.setField(taskTask, "activeSprints", new ArrayList<>());
            assertFalse(accessChecker.isTaskInFutureSprintOnly(taskTask));
        }

        @Test
        @DisplayName("BUG in DRAFT sprint should be in future sprint only")
        void bugInDraftSprint_shouldBeInFutureSprintOnly() {
            ReflectionTestUtils.setField(bugTask, "activeSprints", List.of(draftSprint));
            assertTrue(accessChecker.isTaskInFutureSprintOnly(bugTask));
        }
    }

    // =============================================================================
    // canEditStatus Tests
    // =============================================================================

    @Nested
    @DisplayName("canEditStatus")
    class CanEditStatusTests {

        @Test
        @DisplayName("USER_STORY status should NOT be editable by anyone")
        void userStory_statusShouldNotBeEditable() {
            assertFalse(accessChecker.canEditStatus(userStoryTask, "student-id"));
            assertFalse(accessChecker.canEditStatus(userStoryTask, "professor-id"));
            assertFalse(accessChecker.canEditStatus(userStoryTask, "admin-id"));
        }

        @Test
        @DisplayName("TASK status should be editable by assignee")
        void task_statusShouldBeEditableByAssignee() {
            assertTrue(accessChecker.canEditStatus(taskTask, "student-id"));
        }

        @Test
        @DisplayName("TASK in PAST sprint should NOT be editable by student")
        void taskInPastSprint_statusShouldNotBeEditableByStudent() {
            ReflectionTestUtils.setField(taskTask, "activeSprints", List.of(closedSprint));
            assertFalse(accessChecker.canEditStatus(taskTask, "student-id"));
        }

        @Test
        @DisplayName("TASK in PAST sprint SHOULD be editable by professor")
        void taskInPastSprint_statusShouldBeEditableByProfessor() {
            ReflectionTestUtils.setField(taskTask, "activeSprints", List.of(closedSprint));
            assertTrue(accessChecker.canEditStatus(taskTask, "professor-id"));
        }

        @Test
        @DisplayName("Frozen TASK should NOT be editable by student")
        void frozenTask_statusShouldNotBeEditableByStudent() {
            taskTask.setFrozen(true);
            assertFalse(accessChecker.canEditStatus(taskTask, "student-id"));
        }

        @Test
        @DisplayName("Frozen TASK SHOULD be editable by professor")
        void frozenTask_statusShouldBeEditableByProfessor() {
            taskTask.setFrozen(true);
            assertTrue(accessChecker.canEditStatus(taskTask, "professor-id"));
        }
    }

    // =============================================================================
    // canEditSprint Tests
    // =============================================================================

    @Nested
    @DisplayName("canEditSprint")
    class CanEditSprintTests {

        @Test
        @DisplayName("USER_STORY with subtasks having sprints should NOT edit sprint")
        void userStoryWithSubtaskSprints_shouldNotEditSprint() {
            Task subtask = createTask(10L, "Subtask", TaskType.TASK, TaskStatus.TODO);
            ReflectionTestUtils.setField(subtask, "activeSprints", List.of(activeSprint));
            ReflectionTestUtils.setField(userStoryTask, "childTasks", List.of(subtask));
            
            assertFalse(accessChecker.canEditSprint(userStoryTask, "student-id"));
        }

        @Test
        @DisplayName("USER_STORY without subtasks SHOULD edit sprint")
        void userStoryWithoutSubtasks_shouldEditSprint() {
            ReflectionTestUtils.setField(userStoryTask, "childTasks", new ArrayList<>());
            assertTrue(accessChecker.canEditSprint(userStoryTask, "student-id"));
        }

        @Test
        @DisplayName("TASK in DONE status should NOT edit sprint (student)")
        void taskInDoneStatus_shouldNotEditSprintByStudent() {
            taskTask.setStatus(TaskStatus.DONE);
            assertFalse(accessChecker.canEditSprint(taskTask, "student-id"));
        }

        @Test
        @DisplayName("TASK in DONE status SHOULD edit sprint by professor")
        void taskInDoneStatus_shouldEditSprintByProfessor() {
            taskTask.setStatus(TaskStatus.DONE);
            assertTrue(accessChecker.canEditSprint(taskTask, "professor-id"));
        }

        @Test
        @DisplayName("TASK in PAST sprint (not DONE) SHOULD edit sprint to escape")
        void taskInPastSprintNotDone_shouldEditSprintToEscape() {
            ReflectionTestUtils.setField(taskTask, "activeSprints", List.of(closedSprint));
            taskTask.setStatus(TaskStatus.INPROGRESS);
            assertTrue(accessChecker.canEditSprint(taskTask, "student-id"));
        }

        @Test
        @DisplayName("TASK in PAST sprint with DONE status should NOT edit sprint (student)")
        void taskInPastSprintDone_shouldNotEditSprintByStudent() {
            ReflectionTestUtils.setField(taskTask, "activeSprints", List.of(closedSprint));
            taskTask.setStatus(TaskStatus.DONE);
            assertFalse(accessChecker.canEditSprint(taskTask, "student-id"));
        }

        @Test
        @DisplayName("Frozen TASK should NOT edit sprint (student)")
        void frozenTask_shouldNotEditSprintByStudent() {
            taskTask.setFrozen(true);
            assertFalse(accessChecker.canEditSprint(taskTask, "student-id"));
        }
    }

    // =============================================================================
    // canEditType Tests
    // =============================================================================

    @Nested
    @DisplayName("canEditType")
    class CanEditTypeTests {

        @Test
        @DisplayName("USER_STORY type should NOT be editable")
        void userStory_typeShouldNotBeEditable() {
            assertFalse(accessChecker.canEditType(userStoryTask, "student-id"));
            assertFalse(accessChecker.canEditType(userStoryTask, "professor-id"));
        }

        @Test
        @DisplayName("BUG type should NOT be editable")
        void bug_typeShouldNotBeEditable() {
            assertFalse(accessChecker.canEditType(bugTask, "student-id"));
            assertFalse(accessChecker.canEditType(bugTask, "professor-id"));
        }

        @Test
        @DisplayName("TASK type SHOULD be editable by assignee")
        void task_typeShouldBeEditableByAssignee() {
            assertTrue(accessChecker.canEditType(taskTask, "student-id"));
        }

        @Test
        @DisplayName("TASK in PAST sprint type should NOT be editable by student")
        void taskInPastSprint_typeShouldNotBeEditableByStudent() {
            ReflectionTestUtils.setField(taskTask, "activeSprints", List.of(closedSprint));
            assertFalse(accessChecker.canEditType(taskTask, "student-id"));
        }

        @Test
        @DisplayName("TASK in PAST sprint type SHOULD be editable by professor")
        void taskInPastSprint_typeShouldBeEditableByProfessor() {
            ReflectionTestUtils.setField(taskTask, "activeSprints", List.of(closedSprint));
            assertTrue(accessChecker.canEditType(taskTask, "professor-id"));
        }

        @Test
        @DisplayName("Frozen TASK type should NOT be editable by student")
        void frozenTask_typeShouldNotBeEditableByStudent() {
            taskTask.setFrozen(true);
            assertFalse(accessChecker.canEditType(taskTask, "student-id"));
        }
    }

    // =============================================================================
    // canEditEstimation Tests
    // =============================================================================

    @Nested
    @DisplayName("canEditEstimation")
    class CanEditEstimationTests {

        @Test
        @DisplayName("USER_STORY estimation should NOT be editable (computed from subtasks)")
        void userStory_estimationShouldNotBeEditable() {
            assertFalse(accessChecker.canEditEstimation(userStoryTask, "student-id"));
            assertFalse(accessChecker.canEditEstimation(userStoryTask, "professor-id"));
        }

        @Test
        @DisplayName("TASK estimation SHOULD be editable by assignee")
        void task_estimationShouldBeEditableByAssignee() {
            assertTrue(accessChecker.canEditEstimation(taskTask, "student-id"));
        }

        @Test
        @DisplayName("BUG estimation SHOULD be editable by assignee")
        void bug_estimationShouldBeEditableByAssignee() {
            assertTrue(accessChecker.canEditEstimation(bugTask, "student-id"));
        }

        @Test
        @DisplayName("TASK in PAST sprint estimation should NOT be editable by student")
        void taskInPastSprint_estimationShouldNotBeEditableByStudent() {
            ReflectionTestUtils.setField(taskTask, "activeSprints", List.of(closedSprint));
            assertFalse(accessChecker.canEditEstimation(taskTask, "student-id"));
        }

        @Test
        @DisplayName("Frozen TASK estimation should NOT be editable by student")
        void frozenTask_estimationShouldNotBeEditableByStudent() {
            taskTask.setFrozen(true);
            assertFalse(accessChecker.canEditEstimation(taskTask, "student-id"));
        }
    }

    // =============================================================================
    // canDeleteTask Tests
    // =============================================================================

    @Nested
    @DisplayName("canDeleteTask")
    class CanDeleteTaskTests {

        @Test
        @DisplayName("USER_STORY with subtasks should NOT be deletable")
        void userStoryWithSubtasks_shouldNotBeDeletable() {
            Task subtask = createTask(10L, "Subtask", TaskType.TASK, TaskStatus.TODO);
            ReflectionTestUtils.setField(userStoryTask, "childTasks", List.of(subtask));
            
            assertFalse(accessChecker.canDeleteTask(userStoryTask, "student-id"));
            assertFalse(accessChecker.canDeleteTask(userStoryTask, "professor-id"));
        }

        @Test
        @DisplayName("USER_STORY without subtasks SHOULD be deletable")
        void userStoryWithoutSubtasks_shouldBeDeletable() {
            ReflectionTestUtils.setField(userStoryTask, "childTasks", new ArrayList<>());
            assertTrue(accessChecker.canDeleteTask(userStoryTask, "student-id"));
        }

        @Test
        @DisplayName("TASK in TODO status SHOULD be deletable")
        void taskInTodoStatus_shouldBeDeletable() {
            taskTask.setStatus(TaskStatus.TODO);
            assertTrue(accessChecker.canDeleteTask(taskTask, "student-id"));
        }

        @Test
        @DisplayName("TASK in INPROGRESS status SHOULD be deletable")
        void taskInInProgressStatus_shouldBeDeletable() {
            taskTask.setStatus(TaskStatus.INPROGRESS);
            assertTrue(accessChecker.canDeleteTask(taskTask, "student-id"));
        }

        @Test
        @DisplayName("TASK in VERIFY status should NOT be deletable")
        void taskInVerifyStatus_shouldNotBeDeletable() {
            taskTask.setStatus(TaskStatus.VERIFY);
            assertFalse(accessChecker.canDeleteTask(taskTask, "student-id"));
        }

        @Test
        @DisplayName("TASK in DONE status should NOT be deletable")
        void taskInDoneStatus_shouldNotBeDeletable() {
            taskTask.setStatus(TaskStatus.DONE);
            assertFalse(accessChecker.canDeleteTask(taskTask, "student-id"));
        }

        @Test
        @DisplayName("Frozen TASK should NOT be deletable by student")
        void frozenTask_shouldNotBeDeletableByStudent() {
            taskTask.setFrozen(true);
            assertFalse(accessChecker.canDeleteTask(taskTask, "student-id"));
        }

        @Test
        @DisplayName("Frozen TASK in TODO SHOULD be deletable by professor")
        void frozenTaskInTodo_shouldBeDeletableByProfessor() {
            taskTask.setFrozen(true);
            taskTask.setStatus(TaskStatus.TODO);
            assertTrue(accessChecker.canDeleteTask(taskTask, "professor-id"));
        }

        @Test
        @DisplayName("TASK in PAST sprint should NOT be deletable by student")
        void taskInPastSprint_shouldNotBeDeletableByStudent() {
            ReflectionTestUtils.setField(taskTask, "activeSprints", List.of(closedSprint));
            assertFalse(accessChecker.canDeleteTask(taskTask, "student-id"));
        }

        @Test
        @DisplayName("Reporter (not assignee) should NOT be able to delete task")
        void reporter_notAssignee_shouldNotDelete() {
            // Student is reporter but not assignee
            taskTask.setReporter(studentUser);
            taskTask.setAssignee(null);  // Unassigned
            taskTask.setStatus(TaskStatus.TODO);
            assertFalse(accessChecker.canDeleteTask(taskTask, "student-id"));
        }

        @Test
        @DisplayName("After unassign, former assignee should NOT be able to delete")
        void afterUnassign_formerAssigneeShouldNotDelete() {
            // Student was assignee, now unassigned
            taskTask.setReporter(studentUser);  // Still reporter
            taskTask.setAssignee(null);  // No longer assignee
            taskTask.setStatus(TaskStatus.TODO);
            assertFalse(accessChecker.canDeleteTask(taskTask, "student-id"));
        }

        @Test
        @DisplayName("Current assignee SHOULD be able to delete (TODO status)")
        void currentAssignee_shouldDelete() {
            taskTask.setAssignee(studentUser);
            taskTask.setStatus(TaskStatus.TODO);
            assertTrue(accessChecker.canDeleteTask(taskTask, "student-id"));
        }
    }

    // =============================================================================
    // canSelfAssign Tests
    // =============================================================================

    @Nested
    @DisplayName("canSelfAssign")
    class CanSelfAssignTests {

        @Test
        @DisplayName("Unassigned task SHOULD allow student self-assign")
        void unassignedTask_shouldAllowStudentSelfAssign() {
            taskTask.setAssignee(null);
            assertTrue(accessChecker.canSelfAssign(taskTask, "student-id"));
        }

        @Test
        @DisplayName("Already assigned task should NOT allow self-assign")
        void assignedTask_shouldNotAllowSelfAssign() {
            taskTask.setAssignee(studentUser);
            assertFalse(accessChecker.canSelfAssign(taskTask, "other-student-id"));
        }

        @Test
        @DisplayName("Professor should NOT self-assign (not a student)")
        void professor_shouldNotSelfAssign() {
            taskTask.setAssignee(null);
            assertFalse(accessChecker.canSelfAssign(taskTask, "professor-id"));
        }

        @Test
        @DisplayName("Frozen task should NOT allow self-assign")
        void frozenTask_shouldNotAllowSelfAssign() {
            taskTask.setAssignee(null);
            taskTask.setFrozen(true);
            assertFalse(accessChecker.canSelfAssign(taskTask, "student-id"));
        }

        @Test
        @DisplayName("Task in PAST sprint should NOT allow self-assign")
        void taskInPastSprint_shouldNotAllowSelfAssign() {
            taskTask.setAssignee(null);
            ReflectionTestUtils.setField(taskTask, "activeSprints", List.of(closedSprint));
            assertFalse(accessChecker.canSelfAssign(taskTask, "student-id"));
        }

        @Test
        @DisplayName("Non-project-member should NOT self-assign")
        void nonProjectMember_shouldNotSelfAssign() {
            taskTask.setAssignee(null);
            // other-student is not a project member
            assertFalse(accessChecker.canSelfAssign(taskTask, "other-student-id"));
        }
    }

    // =============================================================================
    // canUnassign Tests
    // =============================================================================

    @Nested
    @DisplayName("canUnassign")
    class CanUnassignTests {

        @Test
        @DisplayName("Unassigned task should NOT allow unassign")
        void unassignedTask_shouldNotAllowUnassign() {
            taskTask.setAssignee(null);
            assertFalse(accessChecker.canUnassign(taskTask, "student-id"));
        }

        @Test
        @DisplayName("Assignee SHOULD be able to unassign themselves")
        void assignee_shouldBeAbleToUnassign() {
            taskTask.setAssignee(studentUser);
            assertTrue(accessChecker.canUnassign(taskTask, "student-id"));
        }

        @Test
        @DisplayName("Professor SHOULD be able to unassign anyone")
        void professor_shouldBeAbleToUnassignAnyone() {
            taskTask.setAssignee(studentUser);
            assertTrue(accessChecker.canUnassign(taskTask, "professor-id"));
        }

        @Test
        @DisplayName("Other student should NOT be able to unassign")
        void otherStudent_shouldNotBeAbleToUnassign() {
            taskTask.setAssignee(studentUser);
            assertFalse(accessChecker.canUnassign(taskTask, "other-student-id"));
        }

        @Test
        @DisplayName("Frozen task should NOT allow student to unassign")
        void frozenTask_shouldNotAllowStudentToUnassign() {
            taskTask.setFrozen(true);
            assertFalse(accessChecker.canUnassign(taskTask, "student-id"));
        }

        @Test
        @DisplayName("Frozen task SHOULD allow professor to unassign")
        void frozenTask_shouldAllowProfessorToUnassign() {
            taskTask.setFrozen(true);
            assertTrue(accessChecker.canUnassign(taskTask, "professor-id"));
        }

        @Test
        @DisplayName("Task in PAST sprint should NOT allow student to unassign")
        void taskInPastSprint_shouldNotAllowStudentToUnassign() {
            ReflectionTestUtils.setField(taskTask, "activeSprints", List.of(closedSprint));
            assertFalse(accessChecker.canUnassign(taskTask, "student-id"));
        }
    }

    // =============================================================================
    // canAddSubtask Tests
    // =============================================================================

    @Nested
    @DisplayName("canAddSubtask")
    class CanAddSubtaskTests {

        @Test
        @DisplayName("TASK should NOT allow adding subtasks")
        void task_shouldNotAllowAddingSubtasks() {
            assertFalse(accessChecker.canAddSubtask(taskTask, "student-id"));
        }

        @Test
        @DisplayName("BUG should NOT allow adding subtasks")
        void bug_shouldNotAllowAddingSubtasks() {
            assertFalse(accessChecker.canAddSubtask(bugTask, "student-id"));
        }

        @Test
        @DisplayName("USER_STORY SHOULD allow project member to add subtasks")
        void userStory_shouldAllowProjectMemberToAddSubtasks() {
            assertTrue(accessChecker.canAddSubtask(userStoryTask, "student-id"));
        }

        @Test
        @DisplayName("USER_STORY SHOULD allow professor to add subtasks")
        void userStory_shouldAllowProfessorToAddSubtasks() {
            assertTrue(accessChecker.canAddSubtask(userStoryTask, "professor-id"));
        }

        @Test
        @DisplayName("Frozen USER_STORY should NOT allow student to add subtasks")
        void frozenUserStory_shouldNotAllowStudentToAddSubtasks() {
            userStoryTask.setFrozen(true);
            assertFalse(accessChecker.canAddSubtask(userStoryTask, "student-id"));
        }

        @Test
        @DisplayName("Frozen USER_STORY SHOULD allow professor to add subtasks")
        void frozenUserStory_shouldAllowProfessorToAddSubtasks() {
            userStoryTask.setFrozen(true);
            assertTrue(accessChecker.canAddSubtask(userStoryTask, "professor-id"));
        }

        @Test
        @DisplayName("Non-project-member should NOT add subtasks")
        void nonProjectMember_shouldNotAddSubtasks() {
            assertFalse(accessChecker.canAddSubtask(userStoryTask, "other-student-id"));
        }
    }

    // =============================================================================
    // canFreeze Tests
    // =============================================================================

    @Nested
    @DisplayName("canFreeze")
    class CanFreezeTests {

        @Test
        @DisplayName("Student should NOT be able to freeze")
        void student_shouldNotBeAbleToFreeze() {
            assertFalse(accessChecker.canFreeze(taskTask, "student-id"));
        }

        @Test
        @DisplayName("Professor SHOULD be able to freeze")
        void professor_shouldBeAbleToFreeze() {
            assertTrue(accessChecker.canFreeze(taskTask, "professor-id"));
        }

        @Test
        @DisplayName("Admin SHOULD be able to freeze")
        void admin_shouldBeAbleToFreeze() {
            assertTrue(accessChecker.canFreeze(taskTask, "admin-id"));
        }
    }

    // =============================================================================
    // canComment Tests
    // =============================================================================

    @Nested
    @DisplayName("canComment")
    class CanCommentTests {

        @Test
        @DisplayName("Project member SHOULD be able to comment")
        void projectMember_shouldBeAbleToComment() {
            assertTrue(accessChecker.canComment(taskTask, "student-id"));
        }

        @Test
        @DisplayName("Professor SHOULD be able to comment")
        void professor_shouldBeAbleToComment() {
            assertTrue(accessChecker.canComment(taskTask, "professor-id"));
        }

        @Test
        @DisplayName("Non-project-member should NOT be able to comment")
        void nonProjectMember_shouldNotBeAbleToComment() {
            assertFalse(accessChecker.canComment(taskTask, "other-student-id"));
        }

        @Test
        @DisplayName("Frozen task should NOT allow student to comment")
        void frozenTask_shouldNotAllowStudentToComment() {
            taskTask.setFrozen(true);
            assertFalse(accessChecker.canComment(taskTask, "student-id"));
        }

        @Test
        @DisplayName("Frozen task SHOULD allow professor to comment")
        void frozenTask_shouldAllowProfessorToComment() {
            taskTask.setFrozen(true);
            assertTrue(accessChecker.canComment(taskTask, "professor-id"));
        }
    }

    // =============================================================================
    // Cross-cutting Security Tests
    // =============================================================================

    @Nested
    @DisplayName("Security - Bypass Prevention")
    class SecurityBypassPreventionTests {

        @Test
        @DisplayName("Non-member student should NOT edit task")
        void nonMemberStudent_shouldNotEditTask() {
            assertFalse(accessChecker.canEditTask(taskTask, "other-student-id"));
        }

        @Test
        @DisplayName("Task in PAST sprint - all edit operations blocked for student")
        void taskInPastSprint_allEditOperationsBlockedForStudent() {
            ReflectionTestUtils.setField(taskTask, "activeSprints", List.of(closedSprint));
            
            assertFalse(accessChecker.canEditStatus(taskTask, "student-id"));
            assertFalse(accessChecker.canEditType(taskTask, "student-id"));
            assertFalse(accessChecker.canEditEstimation(taskTask, "student-id"));
            assertFalse(accessChecker.canDeleteTask(taskTask, "student-id"));
            assertFalse(accessChecker.canUnassign(taskTask, "student-id"));
            assertFalse(accessChecker.canSelfAssign(taskTask, "student-id"));
        }

        @Test
        @DisplayName("Task in PAST sprint - professor CAN still edit")
        void taskInPastSprint_professorCanStillEdit() {
            ReflectionTestUtils.setField(taskTask, "activeSprints", List.of(closedSprint));
            taskTask.setStatus(TaskStatus.TODO); // Ensure TODO for delete test
            
            assertTrue(accessChecker.canEditStatus(taskTask, "professor-id"));
            assertTrue(accessChecker.canEditType(taskTask, "professor-id"));
            assertTrue(accessChecker.canEditEstimation(taskTask, "professor-id"));
            assertTrue(accessChecker.canDeleteTask(taskTask, "professor-id"));
            assertTrue(accessChecker.canUnassign(taskTask, "professor-id"));
        }

        @Test
        @DisplayName("Frozen task - all edit operations blocked for student")
        void frozenTask_allEditOperationsBlockedForStudent() {
            taskTask.setFrozen(true);
            taskTask.setAssignee(null); // for self-assign test
            
            assertFalse(accessChecker.canEditStatus(taskTask, "student-id"));
            assertFalse(accessChecker.canEditSprint(taskTask, "student-id"));
            assertFalse(accessChecker.canEditType(taskTask, "student-id"));
            assertFalse(accessChecker.canEditEstimation(taskTask, "student-id"));
            assertFalse(accessChecker.canDeleteTask(taskTask, "student-id"));
            assertFalse(accessChecker.canSelfAssign(taskTask, "student-id"));
            assertFalse(accessChecker.canComment(taskTask, "student-id"));
        }

        @Test
        @DisplayName("Frozen task - professor CAN still edit")
        void frozenTask_professorCanStillEdit() {
            taskTask.setFrozen(true);
            taskTask.setStatus(TaskStatus.TODO);
            
            assertTrue(accessChecker.canEditStatus(taskTask, "professor-id"));
            assertTrue(accessChecker.canEditSprint(taskTask, "professor-id"));
            assertTrue(accessChecker.canEditType(taskTask, "professor-id"));
            assertTrue(accessChecker.canEditEstimation(taskTask, "professor-id"));
            assertTrue(accessChecker.canDeleteTask(taskTask, "professor-id"));
            assertTrue(accessChecker.canFreeze(taskTask, "professor-id"));
            assertTrue(accessChecker.canComment(taskTask, "professor-id"));
        }

        @Test
        @DisplayName("USER_STORY constraints cannot be bypassed")
        void userStoryConstraints_cannotBeBypassed() {
            // Status never editable
            assertFalse(accessChecker.canEditStatus(userStoryTask, "admin-id"));
            
            // Type never editable
            assertFalse(accessChecker.canEditType(userStoryTask, "admin-id"));
            
            // Estimation never editable
            assertFalse(accessChecker.canEditEstimation(userStoryTask, "admin-id"));
            
            // Delete blocked if has subtasks
            Task subtask = createTask(10L, "Subtask", TaskType.TASK, TaskStatus.TODO);
            ReflectionTestUtils.setField(userStoryTask, "childTasks", List.of(subtask));
            assertFalse(accessChecker.canDeleteTask(userStoryTask, "admin-id"));
        }

        @Test
        @DisplayName("BUG type constraints cannot be bypassed")
        void bugTypeConstraints_cannotBeBypassed() {
            // Type never editable for BUG
            assertFalse(accessChecker.canEditType(bugTask, "admin-id"));
            assertFalse(accessChecker.canEditType(bugTask, "professor-id"));
            assertFalse(accessChecker.canEditType(bugTask, "student-id"));
        }
    }
}
