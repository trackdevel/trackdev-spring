package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.*;
import org.udg.trackdev.spring.entity.sprintchanges.*;
import org.udg.trackdev.spring.model.MergePatchSprint;
import org.udg.trackdev.spring.repository.SprintRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Service
public class SprintService extends BaseServiceLong<Sprint, SprintRepository> {

    @Autowired
    AccessChecker accessChecker;

    @Autowired
    UserService userService;

    @Transactional
    public Sprint create(Project project, String name, Date startDate, Date endDate, String userId) {
        User user = userService.get(userId);
        Sprint sprint = new Sprint(name);
        sprint.setStartDate(startDate, user);
        sprint.setEndDate(endDate, user);
        sprint.setProject(project);
        this.repo().save(sprint);

        return sprint;
    }

    @Transactional
    public Sprint editSprint(Long sprintId, MergePatchSprint editSprint, String userId) {
        Sprint sprint = get(sprintId);
        User user = userService.get(userId);
        //accessChecker.checkCanManageBacklog(sprint.getBacklog(), user);
        List<SprintChange> changes = new ArrayList<>();
        if(editSprint.name != null) {
            String name = editSprint.name.orElseThrow(
                    () -> new ServiceException("Not possible to set name to null"));
            if(!name.equals(sprint.getName())) {
                sprint.setName(name, user);
            }
        }
        if(editSprint.startDate != null) {
            Date startDate = editSprint.startDate.orElseThrow(
                    () -> new ServiceException("Not possible to set startDate to null"));
            if(!startDate.equals(sprint.getStartDate())) {
                sprint.setStartDate(startDate, user);
            }
        }
        if(editSprint.endDate != null) {
            Date endDate = editSprint.endDate.orElseThrow(
                    () -> new ServiceException("Not possible to set endDate to null"));
            if(!endDate.equals(sprint.getEndDate())) {
                sprint.setEndDate(endDate, user);
            }
        }
        if(editSprint.status != null) {
            SprintStatus status = editSprint.status.orElseThrow(
                    () -> new ServiceException("Not possible to set status to null"));
            if(status != sprint.getStatus()) {
                sprint.setStatus(status, user);
            }
        }
        repo().save(sprint);
        return sprint;
    }

    public Collection<Sprint> getSpritnsByIds(Collection<Long> sprintIds) {
        return repo.findAllById(sprintIds);
    }
}
