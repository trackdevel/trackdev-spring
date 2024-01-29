package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.PullRequest;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.repository.PullRequestRepository;

import java.util.Optional;

@Service
public class PullRequestService extends BaseServiceUUID<PullRequest, PullRequestRepository> {

    @Autowired
    TaskService taskService;

    @Autowired
    UserService userService;

    @Transactional
    public PullRequest create(String prNodeId, String url, Long taskId) {
        PullRequest pr = new PullRequest(url, prNodeId);
        if (taskId != null) {
            Task task = taskService.get(taskId);
            pr.setTask(task);
        }
        this.repo.save(pr);
        return pr;
    }

    @Transactional
    public PullRequest findOrCreateByNodeId(String url, String nodeId, String login) {
        User author = userService.getByUsername(login);
        Optional<PullRequest> opr = this.repo().findByNodeId(nodeId);
        if (opr.isEmpty()) {
            PullRequest pr = new PullRequest(url, nodeId);
            pr.setAuthor(author);
            this.repo().save(pr);
            return pr;
        }
        return opr.get();
    }

    public PullRequest getByNodeId(String prNodeId) {
        return this.repo().findByNodeId(prNodeId).orElseThrow(
                () -> new ServiceException(String.format("PullRequest wit node_id = %s does not exists", prNodeId)));
    }
}
