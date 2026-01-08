package org.trackdev.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.trackdev.api.entity.BaseEntityUUID;

public interface BaseRepositoryUUID<T extends BaseEntityUUID> extends JpaRepository<T, String>, JpaSpecificationExecutor<T> {
}
