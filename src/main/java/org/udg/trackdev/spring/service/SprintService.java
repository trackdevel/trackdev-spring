package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.Backlog;
import org.udg.trackdev.spring.entity.Sprint;
import org.udg.trackdev.spring.entity.SprintStatus;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.model.MergePatchSprint;
import org.udg.trackdev.spring.repository.SprintRepository;

import java.util.Calendar;
import java.util.Date;

@Service
public class SprintService extends BaseServiceLong<Sprint, SprintRepository> {

    @Autowired
    AccessChecker accessChecker;

    @Autowired
    BacklogService backlogService;

    @Autowired
    UserService userService;

    @Transactional
    public Sprint create(Long backlogId, String name, Date startDate, Date endDate, String userId) {
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
        if(editSprint.name != null) {
            String name = editSprint.name.orElseThrow(
                    () -> new ServiceException("Not possible to set name to null"));
            sprint.setName(name);
        }
        if(editSprint.startDate != null) {
            Calendar startDate = editSprint.startDate.orElseThrow(
                    () -> new ServiceException("Not possible to set startDate to null"));
            sprint.setStartDate(startDate.getTime());
        }
        if(editSprint.endDate != null) {
            Calendar endDate = editSprint.endDate.orElseThrow(
                    () -> new ServiceException("Not possible to set endDate to null"));
            sprint.setEndDate(endDate.getTime());
        }
        if(editSprint.status != null) {
            SprintStatus status = editSprint.status.orElseThrow(
                    () -> new ServiceException("Not possible to set status to null"));
            sprint.setStatus(status);
        }
        repo().save(sprint);
        return sprint;
    }
}
