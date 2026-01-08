package org.trackdev.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;

/**
 * DTO for Task - Complete (with discussion comments)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskCompleteDTO extends TaskBasicDTO {
    private Collection<CommentDTO> discussion;
}
