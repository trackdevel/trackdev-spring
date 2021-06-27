package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
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

    @Autowired
    CourseService courseService;

    @Transactional
    public Group createGroup(String name, Long courseYearId, String loggedInUserId) {
        CourseYear course = courseYearService.get(courseYearId);
        if(!courseService.canManageCourse(course.getCourse(), loggedInUserId)) {
            throw new ServiceException("User cannot manage this course");
        }
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
