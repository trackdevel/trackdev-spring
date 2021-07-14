package org.udg.trackdev.spring.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.udg.trackdev.spring.configuration.UserType;
import org.udg.trackdev.spring.entity.*;
import org.udg.trackdev.spring.model.EditTask;

import java.util.*;

@Component
public class DemoDataSeeder {

    private final Logger logger = LoggerFactory.getLogger(Global.class);
    private final List<Integer> possibleEstimationPoints = Arrays.asList(1, 2, 3, 4, 5, 8, 13, 20, 40, 100);

    @Autowired
    Global global;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseYearService courseYearService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private IterationService iterationService;

    @Autowired
    private SprintService sprintService;

    @Autowired
    private BacklogService backlogService;

    @Autowired
    private InviteService inviteService;

    @Autowired
    private TaskService taskService;

    public void seedDemoData() {
        logger.info("Starting populating database ...");
        // users
        User admin = userService.addUserInternal("neich", "ignacio.martin@udg.edu", global.getPasswordEncoder().encode("123456"), List.of(UserType.ADMIN, UserType.PROFESSOR));
        User student1 = userService.addUserInternal("student1", "student1@trackdev.com", global.getPasswordEncoder().encode("0000"), List.of(UserType.STUDENT));
        User student2 = userService.addUserInternal("student2", "student2@trackdev.com", global.getPasswordEncoder().encode("2222"), List.of(UserType.STUDENT));
        User professor2 = userService.addUserInternal("professor2", "professor2@trackdev.com", global.getPasswordEncoder().encode("2222"), List.of(UserType.PROFESSOR));
        List<User> enrolledStudents = createDemoStudents();
        enrolledStudents.add(student1);
        enrolledStudents.add(student2);
        // invites to application
        Invite inviteStudent = inviteService.createInvite("student3@trackdev.com", List.of(UserType.STUDENT), admin.getId());
        Invite inviteUpgradeToAdmin = inviteService.createInvite(professor2.getEmail(), List.of(UserType.ADMIN), admin.getId());
        // courses
        Course course = courseService.createCourse("Test course", admin.getId());
        CourseYear courseYear = courseYearService.createCourseYear(course.getId(), 2021, admin.getId());
        for(int i = 3; i <= 10; i++) {
            Invite inviteCourse = courseYearService.createInvite("student" + i + "@trackdev.com", courseYear.getId(), admin.getId());
        }
        inviteAndEnroll(courseYear, enrolledStudents, admin);
        // one course set up
        Iteration iteration = iterationService.create("First iteration", courseYear.getId());
        populateGroup(admin, courseYear, iteration, "Movie reviews", Arrays.asList(student1, student2));
        populateGroup(admin, courseYear, iteration, "Calendar", enrolledStudents.subList(0,4));
        logger.info("Done populating database");
    }

    private void populateGroup(User admin, CourseYear courseYear, Iteration iteration, String groupName, List<User> users) {
        List<String> usernames = new ArrayList<>();
        for(User user: users) {
            usernames.add(user.getUsername());
        }
        Group group = groupService.createGroup(groupName, usernames, courseYear.getId(), admin.getId());
        Sprint sprint = sprintService.create(groupName, iteration.getId(), group.getId());
        Backlog backlog = backlogService.create(group.getId());
        Random random = new Random();
        for(int i = 0; i <= 15; i++) {
            User reporter = users.get(random.nextInt(users.size()));
            Task task = taskService.createTask(backlog.getId(), "Lorem ipsum dolor sit amet", reporter.getId());

            if(random.nextBoolean()) {
                EditTask editTask = buildEditTask(users, random);
                taskService.editTask(task.getId(), editTask, reporter.getId());
            }
        }
        backlog = backlogService.create(group.getId());
    }

    private EditTask buildEditTask(List<User> users, Random random) {
        Integer points = possibleEstimationPoints.get(random.nextInt(possibleEstimationPoints.size()));
        User assignee = users.get(random.nextInt(users.size()));
        TaskStatus status = getRandomStatus(random);

        EditTask editTask = new EditTask();
        editTask.assignee = assignee.getUsername();
        editTask.estimationPoints = points;
        editTask.status = status;
        return editTask;
    }

    private TaskStatus getRandomStatus(Random random) {
        TaskStatus status = TaskStatus.CREATED;
        if(random.nextBoolean()) {
            int index = random.nextInt(3);
            switch (index) {
                case 0:
                    status = TaskStatus.TODO;
                    break;
                case 1:
                    status = TaskStatus.INPROGRESS;
                    break;
                case 2:
                    status = TaskStatus.DONE;
                    break;
            }
        }
        return status;
    }

    private List<User> createDemoStudents() {
        List<String> names = Arrays.asList(
            "Blanca", "Said", "Carles", "Ferran", "Joanot", "Hassen", "Malek", "Osman", "Mahomet", "Guillem", "Roc", // mar i cel
            "Hamlet", "Claudius", "Gertrude", "The Ghost", "Polonius", "Ophelia", "Horatio", "Laertes", "Rosencrantz", "Guildenstern", "Fortinbras" // hamlet
        );
        List<User> users = new ArrayList<>();
        for(String name: names) {
            String username = name.toLowerCase(Locale.ROOT).replace(" ", ".");
            String encodedPassword = global.getPasswordEncoder().encode(username + "1234");
            User user = userService.addUserInternal(username, username + "@trackdev.com", encodedPassword, List.of(UserType.STUDENT));
            users.add(user);
        }
        return users;
    }

    private void inviteAndEnroll(CourseYear courseYear, List<User> users, User admin) {
        for(User user: users) {
            Invite inviteCourse = courseYearService.createInvite(user.getEmail(), courseYear.getId(), admin.getId());
            inviteService.acceptInvite(inviteCourse.getId(), user.getId());
        }
    }
}
