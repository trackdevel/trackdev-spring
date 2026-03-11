package org.trackdev.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.trackdev.api.dto.TaskBasicDTO;
import org.trackdev.api.dto.TaskEventDTO;
import org.trackdev.api.entity.Sprint;
import org.trackdev.api.entity.Task;
import org.trackdev.api.entity.User;
import org.trackdev.api.mapper.TaskMapper;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@Service
public class SseEmitterService {

    private static final Logger log = LoggerFactory.getLogger(SseEmitterService.class);
    private static final long EMITTER_TIMEOUT = 30 * 60 * 1000L; // 30 minutes
    private static final long HEARTBEAT_INTERVAL = 30L; // seconds

    private final ConcurrentHashMap<Long, Set<SseConnection>> emitters = new ConcurrentHashMap<>();
    private final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "sse-heartbeat");
        t.setDaemon(true);
        return t;
    });
    private final ObjectMapper objectMapper;

    @Autowired
    private TaskMapper taskMapper;

    public SseEmitterService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Publish a task event to all SSE subscribers on affected sprints.
     * Collects sprint IDs from the task (and parent if applicable),
     * builds the DTO, and broadcasts to each sprint channel.
     */
    public void publishTaskEvent(Task task, User actor, String eventType) {
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
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);
        SseConnection connection = new SseConnection(emitter, userId);

        emitters.computeIfAbsent(sprintId, k -> ConcurrentHashMap.newKeySet()).add(connection);

        // Schedule per-emitter heartbeat
        ScheduledFuture<?> heartbeat = heartbeatScheduler.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event().name("heartbeat").data(""));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);

        // Cleanup on completion, timeout, or error
        Runnable cleanup = () -> {
            heartbeat.cancel(false);
            removeConnection(sprintId, connection);
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

        log.debug("SSE subscriber added for sprint {} (user {})", sprintId, userId);
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
    }

    private record SseConnection(SseEmitter emitter, String userId) {}
}
