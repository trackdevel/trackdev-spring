package org.udg.trackdev.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.udg.trackdev.spring.entity.Group;
import org.udg.trackdev.spring.entity.Iteration;

@Component
public interface IterationRepository extends BaseRepositoryLong<Iteration> {
}
