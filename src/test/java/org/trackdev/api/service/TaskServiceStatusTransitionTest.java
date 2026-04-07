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
    }
}
