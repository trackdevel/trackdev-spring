package org.trackdev.api.service;

import org.springframework.stereotype.Service;
import org.trackdev.api.entity.Comment;
import org.trackdev.api.entity.Task;
import org.trackdev.api.entity.User;
import org.trackdev.api.repository.CommentRepository;
import org.trackdev.api.utils.HtmlSanitizer;

import java.util.Collection;

@Service
public class CommentService extends BaseServiceLong<Comment, CommentRepository>{

    public Comment addComment(String content, User author, Task task) {
        // Sanitize HTML content to prevent XSS attacks
        String sanitizedContent = HtmlSanitizer.sanitize(content);
        Comment comment = new Comment(sanitizedContent, author, task);
        repo.save(comment);
        return comment;
    }

    public Collection<Comment> getComments(Long taskId) {
        return repo().findByTaskId(taskId);
    }

}
