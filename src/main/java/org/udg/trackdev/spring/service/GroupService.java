package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.Backlog;
import org.udg.trackdev.spring.entity.CourseYear;
import org.udg.trackdev.spring.entity.Group;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.repository.GroupRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class GroupService extends BaseServiceLong<Group, GroupRepository> {

    @Autowired
    UserService userService;

    @Autowired
    CourseYearService courseYearService;

    @Autowired
    AccessChecker accessChecker;

    @Transactional
    public Group createGroup(String name, Collection<String> usernames, Long courseYearId,
                             String loggedInUserId) {
        CourseYear course = courseYearService.get(courseYearId);
        accessChecker.checkCanManageCourseYear(course, loggedInUserId);
        Group group = new Group(name);
        course.addGroup(group);
        group.setCourseYear(course);

        if(usernames != null && usernames.size() > 0) {
            addMembers(course, group, usernames);
        }

        Backlog backlog = new Backlog();
        backlog.setGroup(group);
        group.addBacklog(backlog);

        return group;
    }

    @Transactional
    public Group editGroup(Long groupId, String name, Collection<String> usernames,
                          String loggedInUserId) {
        Group group = get(groupId);
        accessChecker.checkCanManageGroup(group, loggedInUserId);
        if(name != null) {
            group.setName(name);
        }
        if(usernames != null) {
            if(usernames.size() == 0 && group.getMembers().size() != 0) {
                throw new ServiceException("Cannot remove all members of a group");
            }
            editMembers(usernames, group);
        }
        repo.save(group);
        
        return group;
    }

    public void deleteGroup(Long groupId, String userId) {
        Group group = get(groupId);
        accessChecker.checkCanManageGroup(group, userId);
        repo.delete(group);
    }

    private void addMembers(CourseYear course, Group group, Collection<String> usernames) {
        for(String username: usernames) {
            User user = userService.getByUsername(username);
            addMember(course, group, user);
        }
    }

    private void addMember(CourseYear course, Group group, User user) {
        if(!course.isEnrolled(user)) {
            String message = String.format("User with name = %s is not enrolled to this course", user.getUsername());
            throw new ServiceException(message);
        }
        group.addMember(user);
        user.addToGroup(group);
    }

    private void editMembers(Collection<String> usernames, Group group) {
        for(String username: usernames) {
            User user = userService.getByUsername(username);
            if(!group.isMember(user)) {
                addMember(group.getCourseYear(), group, user);
            }
        }
        List<User> toRemove = new ArrayList<>();
        for(User user: group.getMembers()) {
            if(!usernames.contains(user.getUsername())) {
                toRemove.add(user);
            }
        }
        for(User user: toRemove) {
            group.removeMember(user);
            user.removeFromGroup(group);
        }
    }
}
