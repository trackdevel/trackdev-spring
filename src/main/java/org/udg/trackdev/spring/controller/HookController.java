package org.udg.trackdev.spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.entity.Subject;
import org.udg.trackdev.spring.entity.PullRequest;
import org.udg.trackdev.spring.service.SubjectService;
import org.udg.trackdev.spring.service.PRNoteService;
import org.udg.trackdev.spring.service.PullRequestService;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/hooks")
@ResponseBody
public class HookController extends CrudController<Subject, SubjectService> {

    @Autowired
    PRNoteService prNoteService;

    @Autowired
    PullRequestService pullRequestService;

    @PostMapping(path = "/github/pr")
    public PullRequest githubPullRequest(@Valid @RequestBody GithubHookPR prHook) {
        PullRequest pr = pullRequestService.findOrCreateByNodeId(prHook.pull_request.url,
                prHook.pull_request.node_id,
                prHook.pull_request.user.login);



//        PRNote prn = prNoteService.create(prHook.pull_request.node_id,
//                prHook.pull_request.body,
//                prHook.sender.login,
//                prHook.pull_request.user.login,
//                prHook.pull_request.url);
        return pr;
    }

    @PostMapping(path = "/github/issue_comment")
    public PullRequest githubIssueComment(@Valid @RequestBody GithubHookPR prHook) {
        PullRequest pr = pullRequestService.findOrCreateByNodeId(prHook.pull_request.url,
                prHook.pull_request.node_id,
                prHook.pull_request.user.login);



//        PRNote prn = prNoteService.create(prHook.pull_request.node_id,
//                prHook.pull_request.body,
//                prHook.sender.login,
//                prHook.pull_request.user.login,
//                prHook.pull_request.url);
        return pr;
    }


    static class GithubHookPR {
        public Long number;
        public String action;
        public GithubPR pull_request;
        public GithubUser sender;
        public GithubRepo repository;
    }

    static class GithubPR {
        public Long id;
        public String node_id;
        public String url;
        public String body;
        public GithubUser user;
    }

    static class GithubUser {
        public Long id;
        public String login;
    }

    static class GithubRepo {
        public Long id;
        public String full_name;
    }

}
