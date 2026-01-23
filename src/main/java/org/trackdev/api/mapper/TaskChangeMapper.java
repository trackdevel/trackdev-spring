package org.trackdev.api.mapper;

import org.springframework.stereotype.Component;
import org.trackdev.api.configuration.DateFormattingConfiguration;
import org.trackdev.api.dto.TaskLogDTO;
import org.trackdev.api.entity.taskchanges.TaskChange;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting TaskChange entities to TaskLogDTO.
 * Uses the centralized ISO_FORMATTER from DateFormattingConfiguration.
 */
@Component
public class TaskChangeMapper {

    /**
     * Convert a TaskChange entity to TaskLogDTO
     * Maps the change type to a field name that the frontend can understand
     */
    public TaskLogDTO toDTO(TaskChange change) {
        if (change == null) {
            return null;
        }

        TaskLogDTO dto = new TaskLogDTO();
        dto.setId(change.getId());
        dto.setTaskId(change.getTaskId());
        dto.setField(mapTypeToField(change.getType()));
        dto.setOldValue(change.getOldValue());
        dto.setNewValue(change.getNewValue());
        dto.setTimestamp(change.getChangedAt() != null 
            ? change.getChangedAt().format(DateFormattingConfiguration.ISO_FORMATTER) 
            : null);
        dto.setUserId(change.getAuthorEmail());
        dto.setUsername(change.getAuthorUsername());
        dto.setFullName(change.getAuthorFullName());
        
        return dto;
    }

    /**
     * Convert a list of TaskChange entities to TaskLogDTO list
     */
    public List<TaskLogDTO> toDTOList(List<TaskChange> changes) {
        if (changes == null) {
            return null;
        }
        return changes.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Map the TaskChange discriminator type to a field name
     * Examples: "status_change" -> "status", "assignee_change" -> "assignee"
     */
    private String mapTypeToField(String type) {
        if (type == null) {
            return "unknown";
        }
        
        // Remove "_change" suffix if present
        if (type.endsWith("_change")) {
            return type.substring(0, type.length() - 7);
        }
        
        return type;
    }
}
