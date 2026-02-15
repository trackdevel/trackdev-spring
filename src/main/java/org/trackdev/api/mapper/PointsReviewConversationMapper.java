package org.trackdev.api.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.trackdev.api.dto.*;
import org.trackdev.api.entity.PointsReviewConversation;
import org.trackdev.api.entity.PointsReviewMessage;
import org.trackdev.api.service.AccessChecker;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manual mapper for PointsReviewConversation DTOs.
 * Cannot use MapStruct because canEdit/canDelete on messages depend on the current userId.
 */
@Component
public class PointsReviewConversationMapper {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private AccessChecker accessChecker;

    /**
     * Map a conversation to a full DTO with messages and permission flags.
     */
    public PointsReviewConversationDTO toDTO(PointsReviewConversation conv, String userId) {
        PointsReviewConversationDTO dto = new PointsReviewConversationDTO();
        dto.setId(conv.getId());
        dto.setInitiator(userMapper.toSummaryDTO(conv.getInitiator()));
        dto.setProposedPoints(conv.getProposedPoints());
        dto.setCreatedAt(conv.getCreatedAt());
        dto.setUpdatedAt(conv.getUpdatedAt());

        // Map similar tasks
        Collection<TaskBasicDTO> similarTaskDTOs = conv.getSimilarTasks().stream()
            .map(taskMapper::toBasicDTO)
            .collect(Collectors.toList());
        dto.setSimilarTasks(similarTaskDTOs);

        // Map messages with per-user permission flags
        List<PointsReviewMessageDTO> messageDTOs = conv.getMessages().stream()
            .map(msg -> toMessageDTO(msg, userId))
            .collect(Collectors.toList());
        dto.setMessages(messageDTOs);

        // Map participants
        Set<UserSummaryDTO> participantDTOs = conv.getParticipants().stream()
            .map(userMapper::toSummaryDTO)
            .collect(Collectors.toSet());
        dto.setParticipants(participantDTOs);

        // Permission flags
        dto.setCanEdit(conv.isInitiator(userId));
        dto.setCanAddParticipant(accessChecker.isProfessorForTask(conv.getTask(), userId));

        return dto;
    }

    /**
     * Map a conversation to a summary DTO (no messages).
     */
    public PointsReviewConversationSummaryDTO toSummaryDTO(PointsReviewConversation conv) {
        PointsReviewConversationSummaryDTO dto = new PointsReviewConversationSummaryDTO();
        dto.setId(conv.getId());
        dto.setInitiator(userMapper.toSummaryDTO(conv.getInitiator()));
        dto.setProposedPoints(conv.getProposedPoints());
        dto.setMessageCount(conv.getMessageCount());
        dto.setCreatedAt(conv.getCreatedAt());
        dto.setLastMessageAt(conv.getLastMessageAt());
        return dto;
    }

    /**
     * Map a message to DTO with per-user permission flags.
     */
    public PointsReviewMessageDTO toMessageDTO(PointsReviewMessage msg, String userId) {
        PointsReviewMessageDTO dto = new PointsReviewMessageDTO();
        dto.setId(msg.getId());
        dto.setAuthor(userMapper.toSummaryDTO(msg.getAuthor()));
        dto.setContent(msg.getContent());
        dto.setCreatedAt(msg.getCreatedAt());
        dto.setCanEdit(accessChecker.canEditPointsReviewMessage(msg, userId));
        dto.setCanDelete(accessChecker.canDeletePointsReviewMessage(msg, userId));
        return dto;
    }

    /**
     * Map a list of conversations to full DTOs.
     */
    public List<PointsReviewConversationDTO> toDTOList(List<PointsReviewConversation> conversations, String userId) {
        return conversations.stream()
            .map(conv -> toDTO(conv, userId))
            .collect(Collectors.toList());
    }

    /**
     * Map a list of conversations to summary DTOs.
     */
    public List<PointsReviewConversationSummaryDTO> toSummaryDTOList(List<PointsReviewConversation> conversations) {
        return conversations.stream()
            .map(this::toSummaryDTO)
            .collect(Collectors.toList());
    }
}
