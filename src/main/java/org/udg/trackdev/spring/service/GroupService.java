package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.CourseYear;
import org.udg.trackdev.spring.entity.Group;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.repository.GroupRepository;

import java.util.Collection;

@Service
public class GroupService extends BaseService<Group, GroupRepository> {

    @Autowired
    UserService userService;

    @Autowired
    CourseYearService courseYearService;

    @Autowired
    CourseService courseService;

    @Transactional
    public Group createGroup(String name, Collection<String> usernames, Long courseYearId,
                             String loggedInUserId) {
        CourseYear course = courseYearService.get(courseYearId);
        if(!courseService.canManageCourse(course.getCourse(), loggedInUserId)) {
            throw new ServiceException("User cannot manage this course");
        }
        Group group = new Group(name);
        course.addGroup(group);
        group.setCourseYear(course);

        if(usernames != null && usernames.size() > 0) {
            addMembers(course, group, usernames);
        }

        return group;
    }

    @Transactional
    public void addMember(Long groupId, String userId) {
        User user = userService.get(userId);
        Group group = this.get(groupId);
        group.addMember(user);
        user.addToGroup(group);
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
}
