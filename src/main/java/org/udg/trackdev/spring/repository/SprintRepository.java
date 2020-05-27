package org.udg.trackdev.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.udg.trackdev.spring.entity.Group;
import org.udg.trackdev.spring.entity.Sprint;

@Component
public interface SprintRepository extends BaseRepositoryLong<Sprint> {
}
