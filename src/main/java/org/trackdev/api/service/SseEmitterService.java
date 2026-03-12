package org.trackdev.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.trackdev.api.configuration.TrackDevProperties;
import org.trackdev.api.dto.TaskBasicDTO;
import org.trackdev.api.dto.TaskEventDTO;
import org.trackdev.api.entity.Task;
import org.trackdev.api.entity.User;
import org.trackdev.api.mapper.TaskMapper;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SseEmitterService {

    private static final Logger log = LoggerFactory.getLogger(SseEmitterService.class);

    private final ConcurrentHashMap<Long, Set<SseConnection>> emitters = new ConcurrentHashMap<>();
    private final AtomicInteger totalConnections = new AtomicInteger(0);
    private final ConcurrentHashMap<String, AtomicInteger> userConnectionCounts = new ConcurrentHashMap<>();

    private ScheduledExecutorService heartbeatScheduler;
    private final ObjectMapper objectMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TrackDevProperties trackDevProperties;

    public SseEmitterService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        int poolSize = trackDevProperties.getSse().getThreadPoolSize();
        this.heartbeatScheduler = Executors.newScheduledThreadPool(poolSize, r -> {
            Thread t = new Thread(r, "sse-heartbeat");
            t.setDaemon(true);
            return t;
        });
        log.info("SSE service initialized: enabled={}, maxConnections={}, maxPerUser={}, threadPoolSize={}, asyncPoolSize={}",
                trackDevProperties.getSse().isEnabled(),
                trackDevProperties.getSse().getMaxConnections(),
                trackDevProperties.getSse().getMaxConnectionsPerUser(),
                poolSize,
                trackDevProperties.getSse().getAsyncPoolSize());
    }

    /**
     * Publish a task event to all SSE subscribers on affected sprints.
     * No-op when SSE is disabled.
     */
    public void publishTaskEvent(Task task, User actor, String eventType) {
        if (!trackDevProperties.getSse().isEnabled()) {
            return;
        }

        Set<Long> affectedSprintIds = collectAffectedSprintIds(task);
        if (affectedSprintIds.isEmpty()) {
            return;
        }

        TaskEventDTO event = new TaskEventDTO();
        event.setEventType(eventType);
        event.setTaskId(task.getId());
        event.setActorUserId(actor.getId());
        event.setActorFullName(actor.getFullName());
        if (!"task_deleted".equals(eventType)) {
            TaskBasicDTO taskDTO = taskMapper.toBasicDTO(task);
            event.setTask(taskDTO);
        }

        for (Long sprintId : affectedSprintIds) {
            broadcast(sprintId, event);
        }
    }

    private Set<Long> collectAffectedSprintIds(Task task) {
        Set<Long> sprintIds = new HashSet<>();
        if (task.getActiveSprints() != null) {
            task.getActiveSprints().forEach(s -> sprintIds.add(s.getId()));
        }
        // For subtasks, also include parent's sprints (USER_STORY computed sprints)
        if (task.getParentTask() != null && task.getParentTask().getActiveSprints() != null) {
            task.getParentTask().getActiveSprints().forEach(s -> sprintIds.add(s.getId()));
        }
        return sprintIds;
    }

    public SseEmitter subscribe(Long sprintId, String userId) {
        TrackDevProperties.Sse sseConfig = trackDevProperties.getSse();

        // Kill switch
        if (!sseConfig.isEnabled()) {
            return createDisabledEmitter();
        }

        // Total connection limit
        if (totalConnections.get() >= sseConfig.getMaxConnections()) {
            log.warn("SSE max total connections reached ({}). Rejecting for user {} on sprint {}",
                    sseConfig.getMaxConnections(), userId, sprintId);
            return createRejectedEmitter("max_connections");
        }

        // Per-user connection limit
        AtomicInteger userCount = userConnectionCounts.computeIfAbsent(userId, k -> new AtomicInteger(0));
        if (userCount.get() >= sseConfig.getMaxConnectionsPerUser()) {
            log.warn("SSE max per-user connections reached ({}) for user {}",
                    sseConfig.getMaxConnectionsPerUser(), userId);
            return createRejectedEmitter("max_user_connections");
        }

        // Increment counters
        totalConnections.incrementAndGet();
        userCount.incrementAndGet();

        SseEmitter emitter = new SseEmitter(sseConfig.getEmitterTimeoutMs());
        SseConnection connection = new SseConnection(emitter, userId);

        emitters.computeIfAbsent(sprintId, k -> ConcurrentHashMap.newKeySet()).add(connection);

        // Schedule per-emitter heartbeat
        long interval = sseConfig.getHeartbeatIntervalSeconds();
        ScheduledFuture<?> heartbeat = heartbeatScheduler.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event().name("heartbeat").data(""));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }, interval, interval, TimeUnit.SECONDS);

        // Cleanup on completion, timeout, or error
        Runnable cleanup = () -> {
            heartbeat.cancel(false);
            removeConnection(sprintId, connection);
            totalConnections.decrementAndGet();
            AtomicInteger count = userConnectionCounts.get(userId);
            if (count != null) {
                int remaining = count.decrementAndGet();
                if (remaining <= 0) {
                    userConnectionCounts.remove(userId, count);
                }
            }
        };
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(t -> cleanup.run());

        // Send initial connected event
        try {
            emitter.send(SseEmitter.event().name("connected").data("{\"status\":\"connected\"}"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        log.debug("SSE subscriber added for sprint {} (user {}). Total: {}, User: {}",
                sprintId, userId, totalConnections.get(), userCount.get());
        return emitter;
    }

    private SseEmitter createDisabledEmitter() {
        SseEmitter emitter = new SseEmitter(0L);
        try {
            emitter.send(SseEmitter.event().name("disabled").data("{\"reason\":\"sse_disabled\"}"));
        } catch (IOException ignored) {}
        emitter.complete();
        return emitter;
    }

    private SseEmitter createRejectedEmitter(String reason) {
        SseEmitter emitter = new SseEmitter(0L);
        try {
            emitter.send(SseEmitter.event().name("rejected").data("{\"reason\":\"" + reason + "\"}"));
        } catch (IOException ignored) {}
        emitter.complete();
        return emitter;
    }

    public void broadcast(Long sprintId, TaskEventDTO event) {
        Set<SseConnection> connections = emitters.get(sprintId);
        if (connections == null || connections.isEmpty()) {
            return;
        }

        String jsonData;
        try {
            jsonData = objectMapper.writeValueAsString(event);
        } catch (IOException e) {
            log.error("Failed to serialize TaskEventDTO", e);
            return;
        }

        List<SseConnection> dead = new ArrayList<>();
        for (SseConnection connection : connections) {
            try {
                connection.emitter().send(SseEmitter.event().name("task_event").data(jsonData));
            } catch (IOException e) {
                dead.add(connection);
            }
        }
        dead.forEach(c -> removeConnection(sprintId, c));
    }

    private void removeConnection(Long sprintId, SseConnection connection) {
        Set<SseConnection> connections = emitters.get(sprintId);
        if (connections != null) {
            connections.remove(connection);
            if (connections.isEmpty()) {
                emitters.remove(sprintId, connections);
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        heartbeatScheduler.shutdownNow();
        emitters.values().forEach(connections ->
                connections.forEach(c -> c.emitter().complete()));
        emitters.clear();
        totalConnections.set(0);
        userConnectionCounts.clear();
    }

    // Visible for testing
    int getTotalConnections() {
        return totalConnections.get();
    }

    // Visible for testing
    int getUserConnectionCount(String userId) {
        AtomicInteger count = userConnectionCounts.get(userId);
        return count != null ? count.get() : 0;
    }

    private record SseConnection(SseEmitter emitter, String userId) {}
}
