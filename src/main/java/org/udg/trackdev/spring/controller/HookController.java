package org.udg.trackdev.spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.entity.Course;
import org.udg.trackdev.spring.entity.PRNote;
import org.udg.trackdev.spring.entity.PullRequest;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.service.CourseService;
import org.udg.trackdev.spring.service.PRNoteService;
import org.udg.trackdev.spring.service.PullRequestService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/hooks")
@ResponseBody
public class HookController extends CrudController<Course, CourseService> {

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
