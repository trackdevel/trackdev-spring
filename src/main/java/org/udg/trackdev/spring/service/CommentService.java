package org.udg.trackdev.spring.service;

import org.springframework.stereotype.Service;
import org.udg.trackdev.spring.entity.Comment;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.repository.CommentRepository;

import java.util.Collection;

@Service
public class CommentService extends BaseServiceLong<Comment, CommentRepository>{

    public Comment addComment(String content, String author, Task task) {
        Comment comment = new Comment(content, author, task);
        repo.save(comment);
        return comment;
    }

    public Collection<Comment> getComments(Long taskId) {
        return repo().findByTaskId(taskId);
    }

}
