package org.trackdev.api.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.trackdev.api.entity.TaskAttributeValue;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskAttributeValueRepository extends BaseRepositoryLong<TaskAttributeValue> {

    List<TaskAttributeValue> findByTaskId(Long taskId);

    List<TaskAttributeValue> findByTaskIdIn(List<Long> taskIds);

    Optional<TaskAttributeValue> findByTaskIdAndAttributeId(Long taskId, Long attributeId);

    @Modifying
    @Query("DELETE FROM TaskAttributeValue v WHERE v.task.id = :taskId")
    void deleteByTaskId(@Param("taskId") Long taskId);

    @Modifying
    @Query("DELETE FROM TaskAttributeValue v WHERE v.task.id = :taskId AND v.attribute.id = :attributeId")
    void deleteByTaskIdAndAttributeId(@Param("taskId") Long taskId, @Param("attributeId") Long attributeId);

    boolean existsByAttributeId(Long attributeId);

    boolean existsByAttributeIdAndValue(Long attributeId, String value);

    boolean existsByAttributeIdAndValueB(Long attributeId, String valueB);

    long countByAttributeId(Long attributeId);

    List<TaskAttributeValue> findByAttributeId(Long attributeId);

    @Modifying
    @Query("DELETE FROM TaskAttributeValue v WHERE v.attribute.id = :attributeId")
    void deleteByAttributeId(@Param("attributeId") Long attributeId);
}