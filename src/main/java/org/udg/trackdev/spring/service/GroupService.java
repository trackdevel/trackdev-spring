package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.entity.Course;
import org.udg.trackdev.spring.entity.Group;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.repository.GroupRepository;

@Service
public class GroupService extends BaseService<Group, GroupRepository> {

    @Autowired
    UserService userService;

    @Autowired
    CourseService courseService;

    @Transactional
    public Group createGroup(String name, Long courseId) {
        Course course = courseService.get(courseId);
        Group group = new Group(name);
        course.addGroup(group);
        group.setCourse(course);
        return group;
    }

    @Transactional
    public void addMember(Long groupId, String userId) {
        User user = userService.get(userId);
        Group group = this.get(groupId);
        group.addMember(user);
        user.addToGroup(group);
    }
}
