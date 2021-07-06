package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.entity.PRNote;
import org.udg.trackdev.spring.entity.PullRequest;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.repository.PRNoteRepository;

@Service
public class PRNoteService extends BaseServiceLong<PRNote, PRNoteRepository> {

    @Autowired
    PullRequestService prService;

    @Autowired
    UserService userService;

    @Transactional
    public PRNote create(String prNodeId, String subjectId, String authorId, String url, Integer level, String type) {
        PullRequest pr = prService.getByNodeId(prNodeId);
        User subject = userService.getByGithubName(subjectId);
        User author = userService.getByGithubName(authorId);
        PRNote prNote = new PRNote(pr, subject, author, url, level, type);
        // pr.setTask(task);
        this.repo().save(prNote);
        return prNote;
    }

}
