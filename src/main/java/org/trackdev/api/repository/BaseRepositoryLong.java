package org.trackdev.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.trackdev.api.entity.BaseEntityLong;

public interface BaseRepositoryLong<T extends BaseEntityLong> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
}
