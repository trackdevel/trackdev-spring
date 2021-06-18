package org.udg.trackdev.spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.udg.trackdev.spring.entity.BaseEntityUUID;

public interface BaseRepositoryUUID<T extends BaseEntityUUID> extends JpaRepository<T, String>, JpaSpecificationExecutor<T> {
}
