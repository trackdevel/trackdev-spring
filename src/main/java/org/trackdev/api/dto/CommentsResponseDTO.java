package org.trackdev.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

/**
 * DTO for comments list response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentsResponseDTO {
    private Collection<CommentDTO> comments;
    private Long taskId;
}
