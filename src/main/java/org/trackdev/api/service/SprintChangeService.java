package org.trackdev.api.service;

import org.springframework.stereotype.Service;
import org.trackdev.api.entity.sprintchanges.SprintChange;
import org.trackdev.api.repository.SprintChangeRepository;

@Service
public class SprintChangeService extends BaseServiceLong<SprintChange, SprintChangeRepository> {

    public void store(SprintChange sprintChange) {
        repo().save(sprintChange);
    }

}
