package org.trackdev.api.repository;

import org.springframework.stereotype.Component;
import org.trackdev.api.entity.Comment;
import org.trackdev.api.entity.Task;

import java.util.List;

@Component
public interface CommentRepository extends BaseRepositoryLong<Comment>{
    List<Comment> findByTaskId(Long taskId);
    void deleteByTask(Task task);
}
