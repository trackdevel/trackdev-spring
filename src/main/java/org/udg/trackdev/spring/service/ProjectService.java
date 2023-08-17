package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.Backlog;
import org.udg.trackdev.spring.entity.Courses;
import org.udg.trackdev.spring.entity.Project;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.repository.GroupRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class ProjectService extends BaseServiceLong<Project, GroupRepository> {

    @Autowired
    UserService userService;

    @Autowired
    CourseService courseService;

    @Autowired
    AccessChecker accessChecker;

    @Transactional
    public Project createProject(String name, Collection<String> usernames, Long courseId,
                                 String loggedInUserId) {
        Courses course = courseService.get(courseId);
        accessChecker.checkCanManageCourseYear(course, loggedInUserId);
        Project project = new Project(name);
        course.addProject(project);
        project.setCourseYear(course);

        if(usernames != null && usernames.size() > 0) {
            addMembers(course, project, usernames);
        }

        Backlog backlog = new Backlog();
        backlog.setGroup(project);
        project.addBacklog(backlog);

        return project;
    }

    @Transactional
    public Project editProject(Long projectId, String name, Collection<String> usernames,
                               String loggedInUserId) {
        Project project = get(projectId);
        accessChecker.checkCanManageGroup(project, loggedInUserId);
        if(name != null) {
            project.setName(name);
        }
        if(usernames != null) {
            if(usernames.size() == 0 && project.getMembers().size() != 0) {
                throw new ServiceException("Cannot remove all members of a project");
            }
            editMembers(usernames, project);
        }
        repo.save(project);
        
        return project;
    }

    public void deleteProject(Long groupId, String userId) {
        Project project = get(groupId);
        accessChecker.checkCanManageGroup(project, userId);
        repo.delete(project);
    }

    private void addMembers(Courses course, Project project, Collection<String> usernames) {
        for(String username: usernames) {
            User user = userService.getByUsername(username);
            addMember(course, project, user);
        }
    }

    private void addMember(Courses course, Project project, User user) {
        if(!course.isEnrolled(user)) {
            String message = String.format("User with name = %s is not enrolled to this course", user.getUsername());
            throw new ServiceException(message);
        }
        project.addMember(user);
        user.addToGroup(project);
    }

    private void editMembers(Collection<String> usernames, Project project) {
        for(String username: usernames) {
            User user = userService.getByUsername(username);
            if(!project.isMember(user)) {
                addMember(project.getCourseYear(), project, user);
            }
        }
        List<User> toRemove = new ArrayList<>();
        for(User user: project.getMembers()) {
            if(!usernames.contains(user.getUsername())) {
                toRemove.add(user);
            }
        }
        for(User user: toRemove) {
            project.removeMember(user);
            user.removeFromGroup(project);
        }
    }
}
