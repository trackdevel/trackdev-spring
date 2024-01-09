package org.udg.trackdev.spring.service;

import org.springframework.stereotype.Service;
import org.udg.trackdev.spring.entity.sprintchanges.SprintChange;
import org.udg.trackdev.spring.repository.SprintChangeRepository;

@Service
public class SprintChangeService extends BaseServiceLong<SprintChange, SprintChangeRepository> {

    public void store(SprintChange sprintChange) {
        repo().save(sprintChange);
    }

}
