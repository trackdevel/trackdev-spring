package org.trackdev.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request model for creating a new comment on a task.
 */
public class CreateCommentRequest {
    
    @NotBlank(message = "Comment content cannot be empty")
    @Size(max = 2000, message = "Comment content cannot exceed 2000 characters")
    private String content;

    public CreateCommentRequest() {}

    public CreateCommentRequest(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
