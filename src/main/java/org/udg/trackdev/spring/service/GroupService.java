package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.entity.CourseYear;
import org.udg.trackdev.spring.entity.Group;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.repository.GroupRepository;

@Service
public class GroupService extends BaseService<Group, GroupRepository> {

    @Autowired
    UserService userService;

    @Autowired
    CourseYearService courseYearService;

    @Transactional
    public Group createGroup(String name, Long courseYearId) {
        CourseYear course = courseYearService.get(courseYearId);
        Group group = new Group(name);
        course.addGroup(group);
        group.setCourseYear(course);
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
