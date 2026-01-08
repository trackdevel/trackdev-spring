package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.trackdev.api.entity.PRNote;
import org.trackdev.api.repository.PRNoteRepository;

@Service
public class PRNoteService extends BaseServiceLong<PRNote, PRNoteRepository> {

    @Autowired
    PullRequestService prService;

    @Autowired
    UserService userService;

}
