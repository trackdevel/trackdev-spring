package org.trackdev.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.trackdev.api.service.SseEmitterService;
import org.trackdev.api.service.SprintService;

import java.security.Principal;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "7. Sprints")
@RestController
@RequestMapping(path = "/sprints")
public class SprintSseController extends BaseController {

    @Autowired
    private SprintService sprintService;

    @Autowired
    private SseEmitterService sseEmitterService;

    @Operation(summary = "Subscribe to sprint events", description = "SSE endpoint for real-time task updates on a sprint board")
    @GetMapping(path = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(Principal principal, @PathVariable(name = "id") Long id) {
        String userId = super.getUserId(principal);
        // This validates the sprint exists and the user has access
        sprintService.getSprint(id, userId);
        return sseEmitterService.subscribe(id, userId);
    }
}
