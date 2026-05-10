package org.trackdev.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.trackdev.api.entity.*;
import org.trackdev.api.model.MergePatchTask;
import org.trackdev.api.repository.TaskRepository;

import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskService status transition business rules.
 *
 * Verifies:
 * - Professors bypass student-only transition constraints (future sprint, VERIFY, DONE)
 * - Cascade secondary effects on parent USER_STORY are always computed
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceStatusTransitionTest {

    @Mock private TaskRepository taskRepository;
    @Mock private UserService userService;
    @Mock private AccessChecker accessChecker;
    @Mock private TaskChangeService taskChangeService;
    @Mock private ActivityService activityService;
    @Mock private SseEmitterService sseEmitterService;
    @Mock private FcmNotificationService fcmNotificationService;

    private TaskService taskService;

    private User professor;
    private Project project;
    private Sprint draftSprint;

    @BeforeEach
    void setUp() {
        taskService = new TaskService();
        ReflectionTestUtils.setField(taskService, "repo", taskRepository);
        ReflectionTestUtils.setField(taskService, "userService", userService);
        ReflectionTestUtils.setField(taskService, "accessChecker", accessChecker);
        ReflectionTestUtils.setField(taskService, "taskChangeService", taskChangeService);
        ReflectionTestUtils.setField(taskService, "activityService", activityService);
        ReflectionTestUtils.setField(taskService, "sseEmitterService", sseEmitterService);
        ReflectionTestUtils.setField(taskService, "fcmNotificationService", fcmNotificationService);

        professor = new User();
        ReflectionTestUtils.setField(professor, "id", "professor-id");

        project = new Project("Test Project");
        ReflectionTestUtils.setField(project, "id", 1L);

        draftSprint = new Sprint();
        ReflectionTestUtils.setField(draftSprint, "id", 1L);
        draftSprint.setStatus(SprintStatus.DRAFT);
        draftSprint.setStartDate(ZonedDateTime.now().plusDays(7));
        draftSprint.setEndDate(ZonedDateTime.now().plusDays(21));

        lenient().when(userService.get("professor-id")).thenReturn(professor);
        lenient().when(accessChecker.isProfessorForTask(any(), eq("professor-id"))).thenReturn(true);
        lenient().when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Task makeTask(Long id, TaskType type, TaskStatus status) {
        Task task = new Task();
        ReflectionTestUtils.setField(task, "id", id);
        task.setName("task-" + id);
        task.setType(type);
        ReflectionTestUtils.setField(task, "status", status);
        task.setFrozen(false);
        task.setProject(project);
        task.setReporter(professor);
        task.setAssignee(professor);
        ReflectionTestUtils.setField(task, "activeSprints", new ArrayList<>());
        return task;
    }

    // =========================================================================
    // Professor bypass tests
    // =========================================================================

    @Nested
    @DisplayName("Professor bypasses student-only transition constraints")
    class ProfessorBypassTests {

        @Test
        @DisplayName("Professor can change status from TODO in a FUTURE sprint")
        void professor_canChangeStatusFromTodoInFutureSprint() {
            Task task = makeTask(10L, TaskType.TASK, TaskStatus.TODO);
            ReflectionTestUtils.setField(task, "activeSprints", new ArrayList<>(List.of(draftSprint)));
            when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

            MergePatchTask patch = new MergePatchTask();
            patch.status = Optional.of(TaskStatus.INPROGRESS);

            assertDoesNotThrow(() -> taskService.editTask(10L, patch, "professor-id"));
            assertEquals(TaskStatus.INPROGRESS, task.getStatus());
        }

        @Test
        @DisplayName("Professor can move a task to VERIFY without any PR")
        void professor_canChangeStatusToVerifyWithoutPR() {
            Task task = makeTask(10L, TaskType.TASK, TaskStatus.INPROGRESS);
            // pullRequests is empty — student would be blocked
            when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

            MergePatchTask patch = new MergePatchTask();
            patch.status = Optional.of(TaskStatus.VERIFY);

            assertDoesNotThrow(() -> taskService.editTask(10L, patch, "professor-id"));
            assertEquals(TaskStatus.VERIFY, task.getStatus());
        }

        @Test
        @DisplayName("Professor can move a task to DONE without a merged PR")
        void professor_canChangeStatusToDoneWithoutMergedPR() {
            Task task = makeTask(10L, TaskType.TASK, TaskStatus.VERIFY);
            // No merged PR, no estimation — student would be blocked
            when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

            MergePatchTask patch = new MergePatchTask();
            patch.status = Optional.of(TaskStatus.DONE);

            assertDoesNotThrow(() -> taskService.editTask(10L, patch, "professor-id"));
            assertEquals(TaskStatus.DONE, task.getStatus());
        }
    }

    // =========================================================================
    // Cascade secondary-effect tests
    // =========================================================================

    @Nested
    @DisplayName("USER_STORY cascade secondary effects")
    class CascadeTests {

        @Test
        @DisplayName("Marking the last TODO subtask as DONE auto-completes the parent USER_STORY")
        void professor_markingLastSubtaskDone_triggersUserStoryAutoDone() {
            // Sibling subtask is already DONE
            Task sibling = makeTask(11L, TaskType.TASK, TaskStatus.DONE);

            // The task being changed is currently VERIFY
            Task task = makeTask(10L, TaskType.TASK, TaskStatus.VERIFY);

            // Parent USER_STORY whose status should flip to DONE
            Task story = makeTask(20L, TaskType.USER_STORY, TaskStatus.TODO);
            ReflectionTestUtils.setField(story, "childTasks", new ArrayList<>(List.of(sibling, task)));

            task.setParentTask(story);
            sibling.setParentTask(story);

            when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

            MergePatchTask patch = new MergePatchTask();
            patch.status = Optional.of(TaskStatus.DONE);

            taskService.editTask(10L, patch, "professor-id");

            assertEquals(TaskStatus.DONE, task.getStatus(), "task should be DONE");
            assertEquals(TaskStatus.DONE, story.getStatus(), "parent USER_STORY should auto-complete to DONE");
        }

        @Test
        @DisplayName("Moving a subtask away from DONE reverts the parent USER_STORY to TODO")
        void professor_markingSubtaskNonDone_revertsUserStoryToTodo() {
            // Sibling is also DONE
            Task sibling = makeTask(11L, TaskType.TASK, TaskStatus.DONE);

            // The task being changed is currently DONE
            Task task = makeTask(10L, TaskType.TASK, TaskStatus.DONE);

            // Parent USER_STORY is currently DONE
            Task story = makeTask(20L, TaskType.USER_STORY, TaskStatus.DONE);
            ReflectionTestUtils.setField(story, "childTasks", new ArrayList<>(List.of(sibling, task)));

            task.setParentTask(story);
            sibling.setParentTask(story);

            when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

            MergePatchTask patch = new MergePatchTask();
            patch.status = Optional.of(TaskStatus.INPROGRESS);

            taskService.editTask(10L, patch, "professor-id");

            assertEquals(TaskStatus.INPROGRESS, task.getStatus(), "task should be INPROGRESS");
            assertEquals(TaskStatus.TODO, story.getStatus(), "parent USER_STORY should revert to TODO");
        }

        @Test
        @DisplayName("Cascade promotes parent in BACKLOG once last subtask reaches DONE")
        void cascade_promotesParentEvenIfBacklog() {
            // Reproduces the silent-cascade bug: parent in BACKLOG with all children
            // DONE used to throw INVALID_STATUS_TRANSITION (BACKLOG → DONE not in
            // the USER_STORY graph) and roll back the whole transaction. The
            // centralized reconcile now uses forceSetStatus, so the cascade lands.
            Task task = makeTask(10L, TaskType.TASK, TaskStatus.VERIFY);
            Task story = makeTask(20L, TaskType.USER_STORY, TaskStatus.BACKLOG);
            ReflectionTestUtils.setField(story, "childTasks", new ArrayList<>(List.of(task)));
            task.setParentTask(story);

            when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

            MergePatchTask patch = new MergePatchTask();
            patch.status = Optional.of(TaskStatus.DONE);

            taskService.editTask(10L, patch, "professor-id");

            assertEquals(TaskStatus.DONE, task.getStatus(), "subtask should reach DONE");
            assertEquals(TaskStatus.DONE, story.getStatus(),
                    "parent USER_STORY should be promoted regardless of starting state");
        }

        @Test
        @DisplayName("Re-saving a DONE subtask reconciles a stale parent stuck in TODO")
        void cascade_reconcilesStaleParentOnDoneNoOp() {
            // Defensive scenario: the parent somehow drifted out of sync (e.g. a
            // historical bug or partial rollback) so it sits in TODO while every
            // child is already DONE. The cascade has to fire even when this
            // subtask's status is unchanged, so re-submitting DONE on a child
            // brings the user story back into compliance.
            Task done1 = makeTask(11L, TaskType.TASK, TaskStatus.DONE);
            Task done2 = makeTask(10L, TaskType.TASK, TaskStatus.DONE);
            Task story = makeTask(20L, TaskType.USER_STORY, TaskStatus.TODO);
            ReflectionTestUtils.setField(story, "childTasks", new ArrayList<>(List.of(done1, done2)));
            done1.setParentTask(story);
            done2.setParentTask(story);

            when(taskRepository.findById(10L)).thenReturn(Optional.of(done2));

            MergePatchTask patch = new MergePatchTask();
            patch.status = Optional.of(TaskStatus.DONE);  // no-op for the child

            taskService.editTask(10L, patch, "professor-id");

            assertEquals(TaskStatus.DONE, story.getStatus(),
                    "stale parent in TODO should be reconciled to DONE on re-save");
        }
    }

    // =========================================================================
    // Professor manual USER_STORY transitions
    // =========================================================================

    @Nested
    @DisplayName("Professor manual USER_STORY status transitions")
    class UserStoryManualTransitionTests {

        @Test
        @DisplayName("Professor can set USER_STORY TODO → DONE when all subtasks are DONE")
        void professor_canFlipUserStoryToDoneWhenChildrenDone() {
            Task done1 = makeTask(11L, TaskType.TASK, TaskStatus.DONE);
            Task done2 = makeTask(12L, TaskType.TASK, TaskStatus.DONE);
            Task story = makeTask(20L, TaskType.USER_STORY, TaskStatus.TODO);
            ReflectionTestUtils.setField(story, "childTasks", new ArrayList<>(List.of(done1, done2)));

            when(taskRepository.findById(20L)).thenReturn(Optional.of(story));

            MergePatchTask patch = new MergePatchTask();
            patch.status = Optional.of(TaskStatus.DONE);

            assertDoesNotThrow(() -> taskService.editTask(20L, patch, "professor-id"));
            assertEquals(TaskStatus.DONE, story.getStatus());
        }

        @Test
        @DisplayName("Professor cannot flip USER_STORY to DONE while children are still pending")
        void professor_cannotFlipUserStoryToDoneWithPendingChildren() {
            Task done = makeTask(11L, TaskType.TASK, TaskStatus.DONE);
            Task pending = makeTask(12L, TaskType.TASK, TaskStatus.INPROGRESS);
            Task story = makeTask(20L, TaskType.USER_STORY, TaskStatus.TODO);
            ReflectionTestUtils.setField(story, "childTasks", new ArrayList<>(List.of(done, pending)));

            when(taskRepository.findById(20L)).thenReturn(Optional.of(story));

            MergePatchTask patch = new MergePatchTask();
            patch.status = Optional.of(TaskStatus.DONE);

            assertThrows(Exception.class, () -> taskService.editTask(20L, patch, "professor-id"));
            assertEquals(TaskStatus.TODO, story.getStatus(), "story status must not change");
        }

        @Test
        @DisplayName("Professor can revert USER_STORY DONE → TODO")
        void professor_canRevertUserStoryFromDone() {
            Task done = makeTask(11L, TaskType.TASK, TaskStatus.DONE);
            Task story = makeTask(20L, TaskType.USER_STORY, TaskStatus.DONE);
            ReflectionTestUtils.setField(story, "childTasks", new ArrayList<>(List.of(done)));

            when(taskRepository.findById(20L)).thenReturn(Optional.of(story));

            MergePatchTask patch = new MergePatchTask();
            patch.status = Optional.of(TaskStatus.TODO);

            assertDoesNotThrow(() -> taskService.editTask(20L, patch, "professor-id"));
            assertEquals(TaskStatus.TODO, story.getStatus());
        }

        @Test
        @DisplayName("Professor cannot flip USER_STORY directly from BACKLOG (must add a sprint first)")
        void professor_cannotFlipUserStoryFromBacklog() {
            Task done = makeTask(11L, TaskType.TASK, TaskStatus.DONE);
            Task story = makeTask(20L, TaskType.USER_STORY, TaskStatus.BACKLOG);
            ReflectionTestUtils.setField(story, "childTasks", new ArrayList<>(List.of(done)));

            when(taskRepository.findById(20L)).thenReturn(Optional.of(story));

            MergePatchTask patch = new MergePatchTask();
            patch.status = Optional.of(TaskStatus.DONE);

            assertThrows(Exception.class, () -> taskService.editTask(20L, patch, "professor-id"));
            assertEquals(TaskStatus.BACKLOG, story.getStatus());
        }
    }
}
