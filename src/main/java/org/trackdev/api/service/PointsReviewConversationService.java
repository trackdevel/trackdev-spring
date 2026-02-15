package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.controller.exceptions.EntityNotFound;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.*;
import org.trackdev.api.repository.PointsReviewConversationRepository;
import org.trackdev.api.repository.PointsReviewMessageRepository;
import org.trackdev.api.utils.ErrorConstants;
import org.trackdev.api.utils.HtmlSanitizer;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PointsReviewConversationService extends BaseServiceLong<PointsReviewConversation, PointsReviewConversationRepository> {

    @Autowired
    AccessChecker accessChecker;

    @Autowired
    UserService userService;

    @Autowired
    TaskService taskService;

    @Autowired
    PointsReviewMessageRepository messageRepository;

    /**
     * Create a new points review conversation on a task.
     */
    @Transactional
    public PointsReviewConversation createConversation(Long taskId, String message, Integer proposedPoints,
                                                        Collection<Long> similarTaskIds, String userId) {
        Task task = taskService.get(taskId);
        accessChecker.checkCanStartPointsReview(task, userId);

        // Check that user doesn't already have a conversation on this task
        if (repo.existsByTaskIdAndInitiatorId(taskId, userId)) {
            throw new ServiceException(ErrorConstants.POINTS_REVIEW_ALREADY_EXISTS);
        }

        User initiator = userService.get(userId);

        // Create conversation
        PointsReviewConversation conversation = new PointsReviewConversation(task, initiator, proposedPoints);

        // Set similar tasks if provided
        if (similarTaskIds != null && !similarTaskIds.isEmpty()) {
            Set<Task> similarTasks = similarTaskIds.stream()
                .map(id -> taskService.get(id))
                .collect(Collectors.toSet());
            conversation.setSimilarTasks(similarTasks);
        }

        repo.save(conversation);

        // Create the first message
        String sanitizedContent = HtmlSanitizer.sanitize(message);
        PointsReviewMessage firstMessage = new PointsReviewMessage(sanitizedContent, initiator, conversation);
        conversation.addMessage(firstMessage);
        messageRepository.save(firstMessage);

        return conversation;
    }

    /**
     * Get all conversations for a task, filtered by user access.
     * Professor sees all; students see only their own + conversations they participate in.
     */
    @Transactional(readOnly = true)
    public List<PointsReviewConversation> getConversationsForTask(Long taskId, String userId) {
        Task task = taskService.get(taskId);
        accessChecker.checkCanViewProject(task.getProject(), userId);

        // Assignee can never see conversations
        if (accessChecker.isTaskAssignee(task, userId)) {
            return Collections.emptyList();
        }

        List<PointsReviewConversation> conversations = repo.findByTaskId(taskId);

        // Professor/admin can see all
        if (accessChecker.isProfessorForTask(task, userId)) {
            // Initialize lazy collections
            for (PointsReviewConversation conv : conversations) {
                conv.getMessages().size();
                conv.getSimilarTasks().size();
                conv.getParticipants().size();
            }
            return conversations;
        }

        // Non-professor: filter to only own + participated conversations
        List<PointsReviewConversation> filtered = conversations.stream()
            .filter(conv -> conv.hasAccess(userId))
            .collect(Collectors.toList());

        // Initialize lazy collections
        for (PointsReviewConversation conv : filtered) {
            conv.getMessages().size();
            conv.getSimilarTasks().size();
            conv.getParticipants().size();
        }

        return filtered;
    }

    /**
     * Get a single conversation with all its messages.
     */
    @Transactional(readOnly = true)
    public PointsReviewConversation getConversation(Long conversationId, String userId) {
        PointsReviewConversation conversation = get(conversationId);
        accessChecker.checkCanViewPointsReviewConversation(conversation, userId);

        // Initialize lazy collections
        conversation.getMessages().size();
        conversation.getSimilarTasks().size();
        conversation.getParticipants().size();

        return conversation;
    }

    /**
     * Update proposed points and/or similar tasks (initiator only).
     */
    @Transactional
    public PointsReviewConversation updateConversation(Long conversationId, Integer proposedPoints,
                                                        Collection<Long> similarTaskIds, String userId) {
        PointsReviewConversation conversation = get(conversationId);
        accessChecker.checkCanEditPointsReviewConversation(conversation, userId);

        if (proposedPoints != null) {
            conversation.setProposedPoints(proposedPoints);
        }
        if (similarTaskIds != null) {
            Set<Task> similarTasks = similarTaskIds.stream()
                .map(id -> taskService.get(id))
                .collect(Collectors.toSet());
            conversation.setSimilarTasks(similarTasks);
        }

        return repo.save(conversation);
    }

    /**
     * Add a message to a conversation.
     */
    @Transactional
    public PointsReviewMessage addMessage(Long conversationId, String content, String userId) {
        PointsReviewConversation conversation = get(conversationId);
        accessChecker.checkCanPostInPointsReview(conversation, userId);

        User author = userService.get(userId);
        String sanitizedContent = HtmlSanitizer.sanitize(content);
        PointsReviewMessage message = new PointsReviewMessage(sanitizedContent, author, conversation);
        conversation.addMessage(message);
        messageRepository.save(message);

        return message;
    }

    /**
     * Edit a message.
     * Students: own messages, within 10 minutes.
     * Professor: any message.
     */
    @Transactional
    public PointsReviewMessage editMessage(Long messageId, String content, String userId) {
        PointsReviewMessage message = messageRepository.findById(messageId)
            .orElseThrow(() -> new EntityNotFound(ErrorConstants.POINTS_REVIEW_MESSAGE_NOT_FOUND));

        accessChecker.checkCanEditPointsReviewMessage(message, userId);

        String sanitizedContent = HtmlSanitizer.sanitize(content);
        message.setContent(sanitizedContent);
        return messageRepository.save(message);
    }

    /**
     * Delete a message (professor only).
     */
    @Transactional
    public void deleteMessage(Long messageId, String userId) {
        PointsReviewMessage message = messageRepository.findById(messageId)
            .orElseThrow(() -> new EntityNotFound(ErrorConstants.POINTS_REVIEW_MESSAGE_NOT_FOUND));

        accessChecker.checkCanDeletePointsReviewMessage(message, userId);

        PointsReviewConversation conversation = message.getConversation();
        conversation.getMessages().remove(message);
        messageRepository.delete(message);
    }

    /**
     * Add a participant to a conversation (professor only).
     * Cannot add the task assignee.
     */
    @Transactional
    public PointsReviewConversation addParticipant(Long conversationId, String participantUserId, String userId) {
        PointsReviewConversation conversation = get(conversationId);
        accessChecker.checkCanManagePointsReviewParticipants(conversation, userId);

        Task task = conversation.getTask();

        // Cannot add the task assignee
        if (accessChecker.isTaskAssignee(task, participantUserId)) {
            throw new ServiceException(ErrorConstants.POINTS_REVIEW_CANNOT_ADD_ASSIGNEE);
        }

        // Check user is a project member
        User participant = userService.get(participantUserId);
        if (!task.getProject().isMember(participantUserId) && !accessChecker.isProfessorForTask(task, participantUserId)) {
            throw new ServiceException(ErrorConstants.UNAUTHORIZED);
        }

        // Check not already a participant
        if (conversation.isParticipant(participantUserId) || conversation.isInitiator(participantUserId)) {
            throw new ServiceException(ErrorConstants.POINTS_REVIEW_PARTICIPANT_ALREADY_ADDED);
        }

        conversation.addParticipant(participant);
        return repo.save(conversation);
    }

    /**
     * Remove a participant from a conversation (professor only).
     */
    @Transactional
    public PointsReviewConversation removeParticipant(Long conversationId, String participantUserId, String userId) {
        PointsReviewConversation conversation = get(conversationId);
        accessChecker.checkCanManagePointsReviewParticipants(conversation, userId);

        User participant = userService.get(participantUserId);
        conversation.removeParticipant(participant);
        return repo.save(conversation);
    }

    /**
     * Count conversations for a task (used for badge count).
     */
    public int countConversationsForTask(Long taskId) {
        return repo.countByTaskId(taskId);
    }

    /**
     * Check if user already has a conversation on this task.
     */
    public boolean hasExistingConversation(Long taskId, String userId) {
        return repo.existsByTaskIdAndInitiatorId(taskId, userId);
    }
}
