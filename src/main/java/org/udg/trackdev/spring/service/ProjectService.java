package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.*;
import org.udg.trackdev.spring.repository.GroupRepository;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.util.*;

@Service
public class ProjectService extends BaseServiceLong<Project, GroupRepository> {

    @Autowired
    UserService userService;

    @Autowired
    CourseService courseService;

    @Autowired
    SprintService sprintService;

    @Autowired
    AccessChecker accessChecker;

    @Transactional
    public Project createProject(String name, Collection<String> usernames, Long courseId,
                                 String loggedInUserId) {
        Course course = courseService.get(courseId);
        accessChecker.checkCanManageCourse(course, loggedInUserId);
        Project project = new Project(name);
        course.addProject(project);
        project.setCourse(course);

        if(usernames != null && !usernames.isEmpty()) {
            addMembers(course, project, usernames);
        }
        repo.save(project);
        return project;
    }

    //TODO: Modificar a Optionals
    @Transactional
    public Project editProject(Long projectId, String name, Collection<String> usernames,
                               String loggedInUserId) {
        Project project = get(projectId);
        accessChecker.checkCanManageProject(project, loggedInUserId);
        if(name != null) {
            project.setName(name);
        }
        if(usernames != null) {
            if(usernames.isEmpty() && !project.getMembers().isEmpty()) {
                throw new ServiceException("Cannot remove all members of a project");
            }
            editMembers(usernames, project);
        }
        repo.save(project);
        
        return project;
    }

    public void deleteProject(Long groupId, String userId) {
        Project project = get(groupId);
        accessChecker.checkCanManageProject(project, userId);
        repo.delete(project);
    }

    public Collection<Task> getProjectTasks(Project project) {
        return project.getTasks();
    }

    public void createSprint(Project project, String name, Date startDate, Date endDate, String userId) {
        accessChecker.checkCanViewProject(project, userId);
        Sprint sprint = sprintService.create(project, name, startDate, endDate, userId);
        project.addSprint(sprint);
        repo.save(project);
    }

    private void addMembers(Course course, Project project, Collection<String> usernames) {
        for(String username: usernames) {
            User user = userService.getByUsername(username);
            addMember(course, project, user);
        }
    }

    private void addMember(Course course, Project project, User user) {
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
                addMember(project.getCourse(), project, user);
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
