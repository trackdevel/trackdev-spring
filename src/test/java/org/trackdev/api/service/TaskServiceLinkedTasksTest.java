package org.trackdev.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.Task;
import org.trackdev.api.entity.TaskStatus;
import org.trackdev.api.entity.TaskType;
import org.trackdev.api.repository.TaskRepository;
import org.trackdev.api.utils.ErrorConstants;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskService linked task management.
 *
 * Verifies:
 * - Adding a link creates a bidirectional relationship (both tasks reference each other)
 * - Removing a link clears both sides
 * - Self-linking is rejected
 * - Duplicate links are rejected
 * - Only authorised users (assignee or professor) can manage links
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceLinkedTasksTest {

    @Mock private TaskRepository taskRepository;
    @Mock private AccessChecker accessChecker;
    @Mock private UserService userService;

    private TaskService taskService;

    private Task task;
    private Task linkedTask;

    private static final Long TASK_ID = 10L;
    private static final Long LINKED_TASK_ID = 20L;
    private static final String USER_ID = "user-1";

    @BeforeEach
    void setUp() {
        taskService = new TaskService();
        ReflectionTestUtils.setField(taskService, "repo", taskRepository);
        ReflectionTestUtils.setField(taskService, "accessChecker", accessChecker);
        ReflectionTestUtils.setField(taskService, "userService", userService);

        task = new Task();
        ReflectionTestUtils.setField(task, "id", TASK_ID);
        task.setType(TaskType.TASK);
        ReflectionTestUtils.setField(task, "status", TaskStatus.TODO);
        task.setFrozen(false);
        ReflectionTestUtils.setField(task, "linkedTasks", new HashSet<>());

        linkedTask = new Task();
        ReflectionTestUtils.setField(linkedTask, "id", LINKED_TASK_ID);
        linkedTask.setType(TaskType.TASK);
        ReflectionTestUtils.setField(linkedTask, "status", TaskStatus.DONE);
        linkedTask.setFrozen(false);
        ReflectionTestUtils.setField(linkedTask, "linkedTasks", new HashSet<>());

        lenient().when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        lenient().when(taskRepository.findById(LINKED_TASK_ID)).thenReturn(Optional.of(linkedTask));
        lenient().when(accessChecker.canManageLinks(any(), eq(USER_ID))).thenReturn(true);
    }

    @Test
    @DisplayName("addLinkedTask creates a bidirectional link between both tasks")
    void addLinkedTask_createsBidirectionalLink() {
        taskService.addLinkedTask(TASK_ID, LINKED_TASK_ID, USER_ID);

        assertTrue(task.getLinkedTasks().contains(linkedTask),
                "task should contain linkedTask in its linkedTasks set");
        assertTrue(linkedTask.getLinkedTasks().contains(task),
                "linkedTask should contain task in its linkedTasks set (bidirectional)");
        verify(taskRepository).save(task);
        verify(taskRepository).save(linkedTask);
    }

    @Test
    @DisplayName("addLinkedTask throws when task tries to link to itself")
    void addLinkedTask_selfLink_throwsServiceException() {
        ServiceException ex = assertThrows(ServiceException.class,
                () -> taskService.addLinkedTask(TASK_ID, TASK_ID, USER_ID));
        assertEquals(ErrorConstants.TASK_SELF_LINK, ex.getMessage());
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("addLinkedTask throws when the link already exists")
    void addLinkedTask_alreadyLinked_throwsServiceException() {
        // Pre-link the two tasks
        task.addLinkedTask(linkedTask);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> taskService.addLinkedTask(TASK_ID, LINKED_TASK_ID, USER_ID));
        assertEquals(ErrorConstants.TASK_ALREADY_LINKED, ex.getMessage());
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("removeLinkedTask removes the link from both tasks")
    void removeLinkedTask_removesBidirectionalLink() {
        // Establish the link first
        task.addLinkedTask(linkedTask);
        linkedTask.addLinkedTask(task);

        taskService.removeLinkedTask(TASK_ID, LINKED_TASK_ID, USER_ID);

        assertFalse(task.getLinkedTasks().contains(linkedTask),
                "task should no longer contain linkedTask");
        assertFalse(linkedTask.getLinkedTasks().contains(task),
                "linkedTask should no longer contain task (bidirectional removal)");
        verify(taskRepository).save(task);
        verify(taskRepository).save(linkedTask);
    }

    @Test
    @DisplayName("addLinkedTask throws when user is not authorised to manage links")
    void addLinkedTask_unauthorizedUser_throws() {
        when(accessChecker.canManageLinks(task, USER_ID)).thenReturn(false);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> taskService.addLinkedTask(TASK_ID, LINKED_TASK_ID, USER_ID));
        assertEquals(ErrorConstants.CANNOT_MANAGE_LINKS, ex.getMessage());
        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("removeLinkedTask throws when user is not authorised to manage links")
    void removeLinkedTask_notAssignee_throws() {
        task.addLinkedTask(linkedTask);
        linkedTask.addLinkedTask(task);
        when(accessChecker.canManageLinks(task, USER_ID)).thenReturn(false);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> taskService.removeLinkedTask(TASK_ID, LINKED_TASK_ID, USER_ID));
        assertEquals(ErrorConstants.CANNOT_MANAGE_LINKS, ex.getMessage());
        verify(taskRepository, never()).save(any());
    }
}
