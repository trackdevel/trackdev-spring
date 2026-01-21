package org.trackdev.api.dto;

import lombok.Data;

import java.time.ZonedDateTime;

/**
 * DTO for Comment - Basic level
 */
@Data
public class CommentDTO {
    private Long id;
    private String content;
    private UserSummaryDTO author;
    private ZonedDateTime createdAt;
}
