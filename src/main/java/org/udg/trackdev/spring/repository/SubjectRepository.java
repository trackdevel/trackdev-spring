package org.udg.trackdev.spring.repository;

import org.springframework.stereotype.Component;
import org.udg.trackdev.spring.entity.Subject;

import java.util.List;

@Component
public interface SubjectRepository extends BaseRepositoryLong<Subject> {
    //List<Subject> findByOwner(String owner);
}
