package org.udg.trackdev.spring.repository;

import org.springframework.stereotype.Component;
import org.udg.trackdev.spring.entity.Comment;

import java.util.List;

@Component
public interface CommentRepository extends BaseRepositoryLong<Comment>{
    List<Comment> findByTaskId(Long taskId);
}
