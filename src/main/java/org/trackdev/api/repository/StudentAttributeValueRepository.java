package org.trackdev.api.repository;

import org.trackdev.api.entity.StudentAttributeValue;

import java.util.List;
import java.util.Optional;

public interface StudentAttributeValueRepository extends BaseRepositoryLong<StudentAttributeValue> {

    List<StudentAttributeValue> findByUserId(String userId);

    Optional<StudentAttributeValue> findByUserIdAndAttributeId(String userId, Long attributeId);

    void deleteByUserIdAndAttributeId(String userId, Long attributeId);

    void deleteByUserId(String userId);

    boolean existsByAttributeId(Long attributeId);
}
