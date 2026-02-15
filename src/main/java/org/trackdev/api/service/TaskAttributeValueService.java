package org.trackdev.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.entity.TaskAttributeValue;
import org.trackdev.api.repository.TaskAttributeValueRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TaskAttributeValueService extends BaseServiceLong<TaskAttributeValue, TaskAttributeValueRepository> {

    public List<TaskAttributeValue> findByTaskId(Long taskId) {
        return repo().findByTaskId(taskId);
    }

    public Optional<TaskAttributeValue> findByTaskIdAndAttributeId(Long taskId, Long attributeId) {
        return repo().findByTaskIdAndAttributeId(taskId, attributeId);
    }

    @Transactional
    public void deleteByTaskId(Long taskId) {
        repo().deleteByTaskId(taskId);
    }

    @Transactional
    public void deleteByTaskIdAndAttributeId(Long taskId, Long attributeId) {
        repo().deleteByTaskIdAndAttributeId(taskId, attributeId);
    }
}
