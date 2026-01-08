package org.trackdev.api.dto;

import lombok.Data;

import java.util.Date;

/**
 * DTO for Comment - Basic level
 */
@Data
public class CommentDTO {
    private Long id;
    private String content;
    private UserSummaryDTO author;
    private Date createdAt;
}
