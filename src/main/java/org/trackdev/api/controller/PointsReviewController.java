package org.trackdev.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.trackdev.api.dto.PointsReviewConversationDTO;
import org.trackdev.api.dto.PointsReviewConversationSummaryDTO;
import org.trackdev.api.dto.PointsReviewMessageDTO;
import org.trackdev.api.entity.PointsReviewConversation;
import org.trackdev.api.entity.PointsReviewMessage;
import org.trackdev.api.mapper.PointsReviewConversationMapper;
import org.trackdev.api.model.AddPointsReviewParticipantRequest;
import org.trackdev.api.model.CreatePointsReviewMessageRequest;
import org.trackdev.api.model.CreatePointsReviewRequest;
import org.trackdev.api.model.UpdatePointsReviewRequest;
import org.trackdev.api.service.PointsReviewConversationService;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "10. Points Review")
@RestController
@RequestMapping(path = "/tasks/{taskId}/points-reviews")
public class PointsReviewController extends BaseController {

    @Autowired
    private PointsReviewConversationService conversationService;

    @Autowired
    private PointsReviewConversationMapper mapper;

    @Operation(summary = "Create a points review conversation", description = "Start a new points review conversation on a DONE task")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PointsReviewConversationDTO createConversation(
            Principal principal,
            @PathVariable(name = "taskId") Long taskId,
            @Valid @RequestBody CreatePointsReviewRequest request) {
        String userId = getUserId(principal);
        PointsReviewConversation conversation = conversationService.createConversation(
                taskId, request.getContent(), request.getProposedPoints(),
                request.getSimilarTaskIds(), userId);
        return mapper.toDTO(conversation, userId);
    }

    @Operation(summary = "List points review conversations", description = "Get all points review conversations for a task (filtered by access)")
    @GetMapping
    public List<PointsReviewConversationSummaryDTO> listConversations(
            Principal principal,
            @PathVariable(name = "taskId") Long taskId) {
        String userId = getUserId(principal);
        List<PointsReviewConversation> conversations = conversationService.getConversationsForTask(taskId, userId);
        return mapper.toSummaryDTOList(conversations);
    }

    @Operation(summary = "Get a points review conversation", description = "Get a specific points review conversation with all messages")
    @GetMapping(path = "/{conversationId}")
    public PointsReviewConversationDTO getConversation(
            Principal principal,
            @PathVariable(name = "taskId") Long taskId,
            @PathVariable(name = "conversationId") Long conversationId) {
        String userId = getUserId(principal);
        PointsReviewConversation conversation = conversationService.getConversation(conversationId, userId);
        return mapper.toDTO(conversation, userId);
    }

    @Operation(summary = "Update a points review conversation", description = "Update proposed points and/or similar tasks (initiator only)")
    @PatchMapping(path = "/{conversationId}")
    public PointsReviewConversationDTO updateConversation(
            Principal principal,
            @PathVariable(name = "taskId") Long taskId,
            @PathVariable(name = "conversationId") Long conversationId,
            @Valid @RequestBody UpdatePointsReviewRequest request) {
        String userId = getUserId(principal);
        PointsReviewConversation conversation = conversationService.updateConversation(
                conversationId, request.getProposedPoints(), request.getSimilarTaskIds(), userId);
        return mapper.toDTO(conversation, userId);
    }

    @Operation(summary = "Add a message to a conversation", description = "Post a new message in a points review conversation")
    @PostMapping(path = "/{conversationId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public PointsReviewMessageDTO addMessage(
            Principal principal,
            @PathVariable(name = "taskId") Long taskId,
            @PathVariable(name = "conversationId") Long conversationId,
            @Valid @RequestBody CreatePointsReviewMessageRequest request) {
        String userId = getUserId(principal);
        PointsReviewMessage message = conversationService.addMessage(conversationId, request.getContent(), userId);
        return mapper.toMessageDTO(message, userId);
    }

    @Operation(summary = "Edit a message", description = "Edit a message in a points review conversation. Students can only edit within 10 minutes.")
    @PatchMapping(path = "/{conversationId}/messages/{messageId}")
    public PointsReviewMessageDTO editMessage(
            Principal principal,
            @PathVariable(name = "taskId") Long taskId,
            @PathVariable(name = "conversationId") Long conversationId,
            @PathVariable(name = "messageId") Long messageId,
            @Valid @RequestBody CreatePointsReviewMessageRequest request) {
        String userId = getUserId(principal);
        PointsReviewMessage message = conversationService.editMessage(messageId, request.getContent(), userId);
        return mapper.toMessageDTO(message, userId);
    }

    @Operation(summary = "Delete a message", description = "Delete a message from a points review conversation (professor only)")
    @DeleteMapping(path = "/{conversationId}/messages/{messageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMessage(
            Principal principal,
            @PathVariable(name = "taskId") Long taskId,
            @PathVariable(name = "conversationId") Long conversationId,
            @PathVariable(name = "messageId") Long messageId) {
        String userId = getUserId(principal);
        conversationService.deleteMessage(messageId, userId);
    }

    @Operation(summary = "Add a participant", description = "Add a project member as participant to a points review conversation (professor only)")
    @PostMapping(path = "/{conversationId}/participants")
    @ResponseStatus(HttpStatus.CREATED)
    public PointsReviewConversationDTO addParticipant(
            Principal principal,
            @PathVariable(name = "taskId") Long taskId,
            @PathVariable(name = "conversationId") Long conversationId,
            @Valid @RequestBody AddPointsReviewParticipantRequest request) {
        String userId = getUserId(principal);
        PointsReviewConversation conversation = conversationService.addParticipant(
                conversationId, request.getUserId(), userId);
        return mapper.toDTO(conversation, userId);
    }

    @Operation(summary = "Remove a participant", description = "Remove a participant from a points review conversation (professor only)")
    @DeleteMapping(path = "/{conversationId}/participants/{participantUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeParticipant(
            Principal principal,
            @PathVariable(name = "taskId") Long taskId,
            @PathVariable(name = "conversationId") Long conversationId,
            @PathVariable(name = "participantUserId") String participantUserId) {
        String userId = getUserId(principal);
        conversationService.removeParticipant(conversationId, participantUserId, userId);
    }
}
