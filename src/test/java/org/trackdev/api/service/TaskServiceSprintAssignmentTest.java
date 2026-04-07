package org.trackdev.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.*;
import org.trackdev.api.model.MergePatchTask;
import org.trackdev.api.repository.TaskRepository;
import org.trackdev.api.utils.ErrorConstants;

import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskService sprint assignment business rules.
 * Tests the service-layer validation for changing a task's sprint assignment.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceSprintAssignmentTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @Mock
    private AccessChecker accessChecker;

    @Mock
    private SprintService sprintService;

    private TaskService taskService;

    private Task task;
    private User user;
    private Project project;
    private Sprint draftSprint;
    private Sprint closedSprint;

    @BeforeEach
    void setUp() {
        taskService = new TaskService();
        ReflectionTestUtils.setField(taskService, "repo", taskRepository);
        ReflectionTestUtils.setField(taskService, "userService", userService);
        ReflectionTestUtils.setField(taskService, "accessChecker", accessChecker);
        ReflectionTestUtils.setField(taskService, "sprintService", sprintService);

        user = new User();
        ReflectionTestUtils.setField(user, "id", "user-1");

        project = new Project("Test Project");
        ReflectionTestUtils.setField(project, "id", 1L);

        draftSprint = new Sprint();
        ReflectionTestUtils.setField(draftSprint, "id", 1L);
        draftSprint.setStatus(SprintStatus.DRAFT);
        draftSprint.setStartDate(ZonedDateTime.now().plusDays(7));
        draftSprint.setEndDate(ZonedDateTime.now().plusDays(21));

        closedSprint = new Sprint();
        ReflectionTestUtils.setField(closedSprint, "id", 2L);
        closedSprint.setStatus(SprintStatus.CLOSED);
        closedSprint.setStartDate(ZonedDateTime.now().minusDays(21));
        closedSprint.setEndDate(ZonedDateTime.now().minusDays(7));

        task = new Task();
        ReflectionTestUtils.setField(task, "id", 10L);
        task.setName("Test task");
        task.setType(TaskType.TASK);
        ReflectionTestUtils.setField(task, "status", TaskStatus.TODO);
        task.setFrozen(false);
        task.setProject(project);
        task.setAssignee(user);
        task.setReporter(user);
        ReflectionTestUtils.setField(task, "activeSprints", new ArrayList<>(List.of(draftSprint)));

        lenient().when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        lenient().when(userService.get("user-1")).thenReturn(user);
    }

    @Test
    @DisplayName("Task in FUTURE sprint cannot be moved to a CLOSED sprint")
    void taskInFutureSprint_cannotBeMovedToClosedSprint() {
        when(sprintService.getSprintsByIds(any())).thenReturn(List.of(closedSprint));

        MergePatchTask patch = new MergePatchTask();
        patch.activeSprints = Optional.of(List.of(2L));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> taskService.editTask(10L, patch, "user-1"));
        assertEquals(ErrorConstants.SPRINT_NOT_ACTIVE_OR_FUTURE, ex.getMessage());
    }
}
