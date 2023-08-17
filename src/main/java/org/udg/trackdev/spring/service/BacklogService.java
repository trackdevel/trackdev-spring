package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.entity.Backlog;
import org.udg.trackdev.spring.entity.Project;
import org.udg.trackdev.spring.repository.BacklogRepository;

@Service
public class BacklogService extends BaseServiceLong<Backlog, BacklogRepository> {

    @Autowired
    ProjectService projectService;

    @Transactional
    public Backlog create(Long groupId) {
        Project project = projectService.get(groupId);
        Backlog backlog = new Backlog();
        backlog.setGroup(project);
        this.repo.save(backlog);
        return backlog;
    }

//    @Transactional
//    public void addMember(Long groupId, String userId) {
//        User user = userService.getUser(userId);
//        Project group = this.get(groupId);
//        group.addMember(user);
//        user.addToGroup(group);
//    }
}
