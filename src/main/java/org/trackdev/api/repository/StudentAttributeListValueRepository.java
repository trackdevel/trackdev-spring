package org.trackdev.api.repository;

import org.trackdev.api.entity.StudentAttributeListValue;

import java.util.List;

public interface StudentAttributeListValueRepository extends BaseRepositoryLong<StudentAttributeListValue> {

    List<StudentAttributeListValue> findByUserIdAndAttributeIdOrderByOrderIndex(String userId, Long attributeId);

    List<StudentAttributeListValue> findByUserId(String userId);

    void deleteByUserIdAndAttributeId(String userId, Long attributeId);

    void deleteByUserId(String userId);

    boolean existsByAttributeId(Long attributeId);

    boolean existsByAttributeIdAndEnumValue(Long attributeId, String enumValue);
}
