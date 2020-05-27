package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.entity.Backlog;
import org.udg.trackdev.spring.entity.Group;
import org.udg.trackdev.spring.repository.BacklogRepository;

@Service
public class BacklogService extends BaseService<Backlog, BacklogRepository> {

    @Autowired
    GroupService groupService;

    @Transactional
    public Backlog create(Long groupId) {
        Group group = groupService.get(groupId);
        Backlog backlog = new Backlog();
        backlog.setGroup(group);
        this.repo.save(backlog);
        return backlog;
    }

//    @Transactional
//    public void addMember(Long groupId, String userId) {
//        User user = userService.getUser(userId);
//        Group group = this.get(groupId);
//        group.addMember(user);
//        user.addToGroup(group);
//    }
}
