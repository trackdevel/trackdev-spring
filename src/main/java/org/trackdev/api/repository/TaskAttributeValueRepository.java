package org.trackdev.api.repository;

import org.trackdev.api.entity.TaskAttributeValue;

import java.util.List;
import java.util.Optional;

public interface TaskAttributeValueRepository extends BaseRepositoryLong<TaskAttributeValue> {
    
    List<TaskAttributeValue> findByTaskId(Long taskId);
    
    Optional<TaskAttributeValue> findByTaskIdAndAttributeId(Long taskId, Long attributeId);
    
    void deleteByTaskIdAndAttributeId(Long taskId, Long attributeId);
    
    void deleteByTaskId(Long taskId);

    boolean existsByAttributeId(Long attributeId);

    boolean existsByAttributeIdAndValue(Long attributeId, String value);
}
