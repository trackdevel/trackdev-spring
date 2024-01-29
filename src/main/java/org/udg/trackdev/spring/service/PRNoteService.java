package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.udg.trackdev.spring.entity.PRNote;
import org.udg.trackdev.spring.repository.PRNoteRepository;

@Service
public class PRNoteService extends BaseServiceLong<PRNote, PRNoteRepository> {

    @Autowired
    PullRequestService prService;

    @Autowired
    UserService userService;

}
