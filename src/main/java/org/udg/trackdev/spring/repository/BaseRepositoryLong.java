package org.udg.trackdev.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.udg.trackdev.spring.entity.BaseEntityLong;

public interface BaseRepositoryLong<T extends BaseEntityLong> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
}
