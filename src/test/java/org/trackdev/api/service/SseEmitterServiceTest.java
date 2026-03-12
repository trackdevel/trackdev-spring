package org.trackdev.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.trackdev.api.configuration.TrackDevProperties;
import org.trackdev.api.entity.Sprint;
import org.trackdev.api.entity.Task;
import org.trackdev.api.entity.User;
import org.trackdev.api.mapper.TaskMapper;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SseEmitterServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private TaskMapper taskMapper;

    private SseEmitterService sseEmitterService;
    private TrackDevProperties trackDevProperties;

    @BeforeEach
    void setUp() {
        trackDevProperties = new TrackDevProperties();
        trackDevProperties.getSse().setEnabled(true);
        trackDevProperties.getSse().setMaxConnections(10);
        trackDevProperties.getSse().setMaxConnectionsPerUser(2);
        trackDevProperties.getSse().setEmitterTimeoutMs(60000L);
        trackDevProperties.getSse().setHeartbeatIntervalSeconds(30);
        trackDevProperties.getSse().setThreadPoolSize(2);

        sseEmitterService = new SseEmitterService(objectMapper);
        ReflectionTestUtils.setField(sseEmitterService, "taskMapper", taskMapper);
        ReflectionTestUtils.setField(sseEmitterService, "trackDevProperties", trackDevProperties);

        // Call @PostConstruct manually
        sseEmitterService.init();
    }

    @AfterEach
    void tearDown() {
        sseEmitterService.shutdown();
    }

    @Nested
    @DisplayName("subscribe()")
    class Subscribe {

        @Test
        @DisplayName("when disabled, returns completed emitter")
        void whenDisabled_returnsCompletedEmitter() {
            trackDevProperties.getSse().setEnabled(false);

            AtomicBoolean completed = new AtomicBoolean(false);
            SseEmitter emitter = sseEmitterService.subscribe(1L, "user1");
            emitter.onCompletion(() -> completed.set(true));

            // Disabled emitter completes immediately — total connections should not increment
            assertEquals(0, sseEmitterService.getTotalConnections());
        }

        @Test
        @DisplayName("when enabled, returns active emitter and increments counters")
        void whenEnabled_returnsActiveEmitter() {
            SseEmitter emitter = sseEmitterService.subscribe(1L, "user1");

            assertNotNull(emitter);
            assertEquals(1, sseEmitterService.getTotalConnections());
            assertEquals(1, sseEmitterService.getUserConnectionCount("user1"));
        }

        @Test
        @DisplayName("when max total connections reached, rejects new connection")
        void whenMaxConnectionsReached_rejects() {
            trackDevProperties.getSse().setMaxConnections(2);

            sseEmitterService.subscribe(1L, "user1");
            sseEmitterService.subscribe(1L, "user2");

            // Third connection should be rejected
            SseEmitter rejected = sseEmitterService.subscribe(1L, "user3");
            assertNotNull(rejected);

            // Counter should not have incremented for the rejected connection
            assertEquals(2, sseEmitterService.getTotalConnections());
            assertEquals(0, sseEmitterService.getUserConnectionCount("user3"));
        }

        @Test
        @DisplayName("when max per-user connections reached, rejects for that user but allows others")
        void whenMaxPerUserReached_rejectsForThatUser() {
            trackDevProperties.getSse().setMaxConnectionsPerUser(1);

            sseEmitterService.subscribe(1L, "userA");

            // Second connection for same user should be rejected
            SseEmitter rejected = sseEmitterService.subscribe(1L, "userA");
            assertNotNull(rejected);
            assertEquals(1, sseEmitterService.getUserConnectionCount("userA"));

            // Different user should succeed
            sseEmitterService.subscribe(1L, "userB");
            assertEquals(1, sseEmitterService.getUserConnectionCount("userB"));
            assertEquals(2, sseEmitterService.getTotalConnections());
        }
    }

    @Nested
    @DisplayName("publishTaskEvent()")
    class PublishTaskEvent {

        @Test
        @DisplayName("when disabled, does not serialize or broadcast")
        void whenDisabled_isNoOp() throws Exception {
            trackDevProperties.getSse().setEnabled(false);

            Task task = new Task();
            ReflectionTestUtils.setField(task, "id", 1L);
            Sprint sprint = new Sprint();
            ReflectionTestUtils.setField(sprint, "id", 1L);
            task.setActiveSprints(List.of(sprint));

            User actor = new User();
            ReflectionTestUtils.setField(actor, "id", "actor1");

            sseEmitterService.publishTaskEvent(task, actor, "task_updated");

            verify(objectMapper, never()).writeValueAsString(any());
        }

        @Test
        @DisplayName("when enabled with subscriber, serializes and broadcasts")
        void whenEnabled_broadcasts() throws Exception {
            // Subscribe first
            sseEmitterService.subscribe(1L, "user1");

            Task task = new Task();
            ReflectionTestUtils.setField(task, "id", 1L);
            Sprint sprint = new Sprint();
            ReflectionTestUtils.setField(sprint, "id", 1L);
            task.setActiveSprints(List.of(sprint));

            User actor = new User();
            ReflectionTestUtils.setField(actor, "id", "actor1");

            when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\":true}");

            sseEmitterService.publishTaskEvent(task, actor, "task_updated");

            verify(objectMapper).writeValueAsString(any());
        }
    }

    @Nested
    @DisplayName("cleanup and shutdown")
    class CleanupAndShutdown {

        @Test
        @DisplayName("shutdown completes all emitters and resets counters")
        void shutdown_completesAllEmitters() {
            sseEmitterService.subscribe(1L, "user1");
            sseEmitterService.subscribe(2L, "user2");

            assertEquals(2, sseEmitterService.getTotalConnections());

            sseEmitterService.shutdown();

            assertEquals(0, sseEmitterService.getTotalConnections());
            assertEquals(0, sseEmitterService.getUserConnectionCount("user1"));
            assertEquals(0, sseEmitterService.getUserConnectionCount("user2"));
        }
    }
}
