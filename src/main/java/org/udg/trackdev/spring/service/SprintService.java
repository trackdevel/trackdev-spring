package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.*;
import org.udg.trackdev.spring.repository.GroupRepository;
import org.udg.trackdev.spring.repository.SprintRepository;

import java.util.Optional;

@Service
public class SprintService extends BaseServiceLong<Sprint, SprintRepository> {

    @Autowired
    IterationService iterationService;

    @Autowired
    GroupService groupService;


    @Transactional
    public Sprint create(String name, Long iterationId, Long groupId) {
        Iteration iteration = iterationService.get(iterationId);
        Group group = groupService.get(groupId);
        Sprint sprint= new Sprint(name);
        sprint.setIteration(iteration);
        sprint.setGroup(group);
        this.repo.save(sprint);
        return sprint;
    }

}
