package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.entity.Backlog;
import org.udg.trackdev.spring.entity.Sprint;
import org.udg.trackdev.spring.repository.SprintRepository;

import java.util.Date;

@Service
public class SprintService extends BaseServiceLong<Sprint, SprintRepository> {

    @Autowired
    AccessChecker accessChecker;

    @Autowired
    BacklogService backlogService;

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
}
