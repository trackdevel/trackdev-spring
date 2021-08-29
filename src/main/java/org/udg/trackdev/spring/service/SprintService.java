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
import java.util.List;

@Service
public class SprintService extends BaseServiceLong<Sprint, SprintRepository> {

    @Autowired
    AccessChecker accessChecker;

    @Autowired
    BacklogService backlogService;

    @Autowired
    UserService userService;

    @Autowired
    SprintChangeService sprintChangeService;

    @Transactional
    public Sprint create(Long backlogId, String name, LocalDate startDate, LocalDate endDate, String userId) {
        Backlog backlog = backlogService.get(backlogId);
        accessChecker.checkCanManageBacklog(backlog, userId);

        Sprint sprint = new Sprint(name);
        sprint.setStartDate(startDate);
        sprint.setEndDate(endDate);
        sprint.setBacklog(backlog);
        backlog.addSprint(sprint);
        this.repo().save(sprint);

        return sprint;
    }

    @Transactional
    public Sprint editSprint(Long sprintId, MergePatchSprint editSprint, String userId) {
        Sprint sprint = get(sprintId);
        User user = userService.get(userId);
        accessChecker.checkCanManageBacklog(sprint.getBacklog(), user);
        List<SprintChange> changes = new ArrayList<>();
        if(editSprint.name != null) {
            String name = editSprint.name.orElseThrow(
                    () -> new ServiceException("Not possible to set name to null"));
            if(!name.equals(sprint.getName())) {
                sprint.setName(name);
                changes.add(new SprintNameChange(user, sprint, name));
            }
        }
        if(editSprint.startDate != null) {
            LocalDate startDate = editSprint.startDate.orElseThrow(
                    () -> new ServiceException("Not possible to set startDate to null"));
            if(!startDate.equals(sprint.getStartDate())) {
                sprint.setStartDate(startDate);
                changes.add(new SprintStartDateChange(user, sprint, startDate));
            }
        }
        if(editSprint.endDate != null) {
            LocalDate endDate = editSprint.endDate.orElseThrow(
                    () -> new ServiceException("Not possible to set endDate to null"));
            if(!endDate.equals(sprint.getEndDate())) {
                sprint.setEndDate(endDate);
                changes.add(new SprintEndDateChange(user, sprint, endDate));
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
        for(SprintChange change: changes) {
            sprintChangeService.store(change);
        }
        return sprint;
    }

    private void moveToTodo(Sprint sprint, User user) {

    }
}
