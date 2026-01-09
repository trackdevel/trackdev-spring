package org.trackdev.api.repository;

import org.springframework.stereotype.Component;
import org.trackdev.api.entity.Subject;

import java.util.List;

@Component
public interface SubjectRepository extends BaseRepositoryLong<Subject> {
    List<Subject> findByOwnerId(String ownerId);
}
