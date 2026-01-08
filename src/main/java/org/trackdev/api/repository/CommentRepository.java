package org.trackdev.api.repository;

import org.springframework.stereotype.Component;
import org.trackdev.api.entity.Comment;

import java.util.List;

@Component
public interface CommentRepository extends BaseRepositoryLong<Comment>{
    List<Comment> findByTaskId(Long taskId);
}
