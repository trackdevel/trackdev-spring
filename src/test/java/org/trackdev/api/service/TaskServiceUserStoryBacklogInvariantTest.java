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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Verifies the domain invariant:
 * a USER_STORY parent must NOT remain in BACKLOG once any of its subtasks is in a sprint.
 *
 * Exercises every path that can mutate the USER_STORY / subtask / sprint relationship:
 *   1. Subtask creation with a sprint        (createSubTask)
 *   2. Subtask sprint assignment via patch   (editTask, activeSprints) — regression for the bug
 *   3. Subtask status change                 (editTask, status)
 *   4. Subtask deletion                      (deleteTask)
 *   5. USER_STORY direct sprint assignment   (editTask, activeSprints on the parent)
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceUserStoryBacklogInvariantTest {

    @Mock private TaskRepository taskRepository;
    @Mock private UserService userService;
    @Mock private AccessChecker accessChecker;
    @Mock private TaskChangeService taskChangeService;
    @Mock private ActivityService activityService;
    @Mock private SseEmitterService sseEmitterService;
    @Mock private SprintService sprintService;
    @Mock private CommentService commentService;
    @Mock private TaskAttributeValueService taskAttributeValueService;
    @Mock private ProjectAnalysisService projectAnalysisService;

    private TaskService taskService;

    private static final String PROFESSOR_ID = "professor-id";

    private User professor;
    private Project project;
    private Sprint activeSprint;

    @BeforeEach
    void setUp() {
        taskService = new TaskService();
        ReflectionTestUtils.setField(taskService, "repo", taskRepository);
        ReflectionTestUtils.setField(taskService, "userService", userService);
        ReflectionTestUtils.setField(taskService, "accessChecker", accessChecker);
        ReflectionTestUtils.setField(taskService, "taskChangeService", taskChangeService);
        ReflectionTestUtils.setField(taskService, "activityService", activityService);
        ReflectionTestUtils.setField(taskService, "sseEmitterService", sseEmitterService);
        ReflectionTestUtils.setField(taskService, "sprintService", sprintService);
        ReflectionTestUtils.setField(taskService, "commentService", commentService);
        ReflectionTestUtils.setField(taskService, "taskAttributeValueService", taskAttributeValueService);
        ReflectionTestUtils.setField(taskService, "projectAnalysisService", projectAnalysisService);

        professor = new User();
        ReflectionTestUtils.setField(professor, "id", PROFESSOR_ID);

        project = new Project("Test Project");
        ReflectionTestUtils.setField(project, "id", 1L);

        activeSprint = new Sprint();
        ReflectionTestUtils.setField(activeSprint, "id", 100L);
        activeSprint.setStatus(SprintStatus.ACTIVE);
        activeSprint.setStartDate(ZonedDateTime.now().minusDays(1));
        activeSprint.setEndDate(ZonedDateTime.now().plusDays(13));
        ReflectionTestUtils.setField(activeSprint, "project", project);
        ReflectionTestUtils.setField(activeSprint, "activeTasks", new ArrayList<>());

        lenient().when(userService.get(PROFESSOR_ID)).thenReturn(professor);
        lenient().when(accessChecker.isProfessorForTask(any(), eq(PROFESSOR_ID))).thenReturn(true);
        lenient().when(accessChecker.isProfessorForProject(any(), eq(PROFESSOR_ID))).thenReturn(true);
        lenient().when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Task makeTask(Long id, TaskType type, TaskStatus status) {
        Task t = new Task();
        ReflectionTestUtils.setField(t, "id", id);
        t.setName("task-" + id);
        t.setType(type);
        ReflectionTestUtils.setField(t, "status", status);
        t.setFrozen(false);
        t.setProject(project);
        t.setReporter(professor);
        t.setAssignee(professor);
        ReflectionTestUtils.setField(t, "activeSprints", new ArrayList<>());
        ReflectionTestUtils.setField(t, "childTasks", new ArrayList<>());
        return t;
    }

    private Task makeUserStoryWithSubtask(Task subtask) {
        Task story = makeTask(20L, TaskType.USER_STORY, TaskStatus.BACKLOG);
        ReflectionTestUtils.setField(story, "childTasks", new ArrayList<>(List.of(subtask)));
        subtask.setParentTask(story);
        return story;
    }

    private void assertInvariant(Task story) {
        boolean anySubtaskInSprint = story.getChildTasks() != null && story.getChildTasks().stream()
                .anyMatch(s -> s.getActiveSprints() != null && !s.getActiveSprints().isEmpty());
        if (anySubtaskInSprint) {
            assertNotEquals(TaskStatus.BACKLOG, story.getStatus(),
                    "USER_STORY must not stay in BACKLOG when any subtask is in a sprint");
        }
    }

    @Nested
    @DisplayName("Subtask creation")
    class SubtaskCreation {

        @Test
        @DisplayName("Creating a subtask with a target sprint moves the BACKLOG parent to TODO")
        void createSubtaskWithSprint_movesParentBacklogToTodo() {
            Task story = makeTask(20L, TaskType.USER_STORY, TaskStatus.BACKLOG);
            ReflectionTestUtils.setField(story, "childTasks", new ArrayList<>());

            when(taskRepository.findById(20L)).thenReturn(Optional.of(story));
            when(sprintService.get(100L)).thenReturn(activeSprint);

            taskService.createSubTask(20L, "child", null, PROFESSOR_ID, 100L, TaskType.TASK, null);

            assertEquals(TaskStatus.TODO, story.getStatus(),
                    "parent USER_STORY must transition BACKLOG -> TODO on sprinted subtask creation");
            assertInvariant(story);
        }

        @Test
        @DisplayName("Creating a subtask without a sprint keeps the parent in BACKLOG")
        void createSubtaskWithoutSprint_keepsParentInBacklog() {
            Task story = makeTask(20L, TaskType.USER_STORY, TaskStatus.BACKLOG);
            ReflectionTestUtils.setField(story, "childTasks", new ArrayList<>());

            when(taskRepository.findById(20L)).thenReturn(Optional.of(story));

            taskService.createSubTask(20L, "child", null, PROFESSOR_ID, null, TaskType.TASK, null);

            assertEquals(TaskStatus.BACKLOG, story.getStatus(),
                    "parent stays BACKLOG when subtask has no sprint");
            assertInvariant(story);
        }
    }

    @Nested
    @DisplayName("Subtask sprint patch (regression: USER_STORY stuck in BACKLOG)")
    class SubtaskSprintPatch {

        @Test
        @DisplayName("Assigning a sprint to a BACKLOG subtask moves the BACKLOG parent to TODO")
        void editSubtaskAddSprint_movesParentBacklogToTodo() {
            Task subtask = makeTask(10L, TaskType.TASK, TaskStatus.BACKLOG);
            Task story = makeUserStoryWithSubtask(subtask);

            when(taskRepository.findById(10L)).thenReturn(Optional.of(subtask));
            when(sprintService.getSprintsByIds(any())).thenReturn(List.of(activeSprint));

            MergePatchTask patch = new MergePatchTask();
            patch.activeSprints = Optional.of(List.of(100L));

            taskService.editTask(10L, patch, PROFESSOR_ID);

            assertEquals(TaskStatus.TODO, subtask.getStatus(),
                    "subtask must transition BACKLOG -> TODO when added to a sprint");
            assertEquals(TaskStatus.TODO, story.getStatus(),
                    "parent USER_STORY must NOT stay in BACKLOG once subtask is in a sprint");
            assertInvariant(story);
        }

        @Test
        @DisplayName("Switching subtask between sprints leaves the already-TODO parent unchanged")
        void editSubtaskSwitchSprint_keepsTodoParentTodo() {
            Sprint otherSprint = new Sprint();
            ReflectionTestUtils.setField(otherSprint, "id", 101L);
            otherSprint.setStatus(SprintStatus.DRAFT);
            otherSprint.setStartDate(ZonedDateTime.now().plusDays(10));
            otherSprint.setEndDate(ZonedDateTime.now().plusDays(20));
            ReflectionTestUtils.setField(otherSprint, "project", project);
            ReflectionTestUtils.setField(otherSprint, "activeTasks", new ArrayList<>());

            Task subtask = makeTask(10L, TaskType.TASK, TaskStatus.TODO);
            ReflectionTestUtils.setField(subtask, "activeSprints", new ArrayList<>(List.of(activeSprint)));
            Task story = makeUserStoryWithSubtask(subtask);
            ReflectionTestUtils.setField(story, "status", TaskStatus.TODO);

            when(taskRepository.findById(10L)).thenReturn(Optional.of(subtask));
            when(sprintService.getSprintsByIds(any())).thenReturn(List.of(otherSprint));

            MergePatchTask patch = new MergePatchTask();
            patch.activeSprints = Optional.of(List.of(101L));

            taskService.editTask(10L, patch, PROFESSOR_ID);

            assertEquals(TaskStatus.TODO, story.getStatus(),
                    "TODO parent stays TODO when subtask sprint changes between sprints");
            assertInvariant(story);
        }
    }

    @Nested
    @DisplayName("Subtask status change preserves the invariant")
    class SubtaskStatusChange {

        @Test
        @DisplayName("Subtask TODO -> INPROGRESS does not move parent back to BACKLOG")
        void subtaskTodoToInProgress_doesNotRevertParent() {
            Task subtask = makeTask(10L, TaskType.TASK, TaskStatus.TODO);
            ReflectionTestUtils.setField(subtask, "activeSprints", new ArrayList<>(List.of(activeSprint)));
            Task story = makeUserStoryWithSubtask(subtask);
            ReflectionTestUtils.setField(story, "status", TaskStatus.TODO);

            when(taskRepository.findById(10L)).thenReturn(Optional.of(subtask));

            MergePatchTask patch = new MergePatchTask();
            patch.status = Optional.of(TaskStatus.INPROGRESS);

            taskService.editTask(10L, patch, PROFESSOR_ID);

            assertEquals(TaskStatus.INPROGRESS, subtask.getStatus());
            assertInvariant(story);
        }
    }

    @Nested
    @DisplayName("Subtask deletion preserves the invariant")
    class SubtaskDeletion {

        @Test
        @DisplayName("Deleting a non-sprinted subtask while a sibling is in a sprint keeps parent out of BACKLOG")
        void deleteOneSubtask_remainingSibling_inSprint_invariantHolds() {
            Task sprintedSibling = makeTask(11L, TaskType.TASK, TaskStatus.TODO);
            ReflectionTestUtils.setField(sprintedSibling, "activeSprints", new ArrayList<>(List.of(activeSprint)));

            Task victim = makeTask(10L, TaskType.TASK, TaskStatus.BACKLOG);

            Task story = makeTask(20L, TaskType.USER_STORY, TaskStatus.TODO);
            ReflectionTestUtils.setField(story, "childTasks", new ArrayList<>(List.of(sprintedSibling, victim)));
            sprintedSibling.setParentTask(story);
            victim.setParentTask(story);

            when(taskRepository.findById(10L)).thenReturn(Optional.of(victim));

            taskService.deleteTask(10L, PROFESSOR_ID);

            story.getChildTasks().remove(victim);

            assertNotEquals(TaskStatus.BACKLOG, story.getStatus(),
                    "deletion must not leave parent in BACKLOG while a sibling is sprinted");
            assertInvariant(story);
        }
    }

    @Nested
    @DisplayName("USER_STORY direct sprint assignment")
    class UserStoryDirectSprintPatch {

        @Test
        @DisplayName("Assigning a sprint directly to a BACKLOG USER_STORY moves it to TODO and cascades to subtasks")
        void editUserStoryAddSprint_movesParentAndChildren() {
            Task subtask = makeTask(10L, TaskType.TASK, TaskStatus.BACKLOG);
            Task story = makeUserStoryWithSubtask(subtask);

            when(taskRepository.findById(20L)).thenReturn(Optional.of(story));
            when(sprintService.getSprintsByIds(any())).thenReturn(List.of(activeSprint));

            MergePatchTask patch = new MergePatchTask();
            patch.activeSprints = Optional.of(List.of(100L));

            taskService.editTask(20L, patch, PROFESSOR_ID);

            assertEquals(TaskStatus.TODO, story.getStatus(),
                    "USER_STORY must transition BACKLOG -> TODO when assigned to a sprint");
            assertEquals(TaskStatus.TODO, subtask.getStatus(),
                    "subtasks cascade from BACKLOG -> TODO when parent USER_STORY is sprinted");
            assertInvariant(story);
        }
    }
}