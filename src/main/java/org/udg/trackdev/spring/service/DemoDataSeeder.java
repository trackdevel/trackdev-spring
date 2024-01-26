package org.udg.trackdev.spring.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.udg.trackdev.spring.configuration.UserType;
import org.udg.trackdev.spring.entity.*;
import org.udg.trackdev.spring.model.MergePatchSprint;
import org.udg.trackdev.spring.model.MergePatchTask;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Component
public class DemoDataSeeder {

    private final Logger logger = LoggerFactory.getLogger(Global.class);
    private final List<Integer> possibleEstimationPoints = Arrays.asList(1, 2, 3, 5, 8, 13, 20, 40, 100);

    @Autowired
    Global global;

    @Autowired
    private UserService userService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SprintService sprintService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private CommentService commentService;

    public void seedDemoData() {
        logger.info("Starting populating database ...");
        // users
        List<User> enrolledStudents = createDemoStudents();
        User nacho = userService.addUserInternal("Ignacio Martín", "ignacio.martin@udg.edu ", global.getPasswordEncoder().encode("N123123n"), List.of(UserType.ADMIN, UserType.PROFESSOR));
        User gerard = userService.addUserInternal("Gerard Rovellat", "gerard.rovellat@gmail.com", global.getPasswordEncoder().encode("G123123r"), List.of(UserType.ADMIN));
        User marc = userService.addUserInternal("Marc Got", "gotcritgmarc@gmail.com", global.getPasswordEncoder().encode("M123123g"), List.of(UserType.ADMIN));
        User admin = userService.addUserInternal("TrackDev Administrator", "admin@trackdev.com", global.getPasswordEncoder().encode("admin"), List.of(UserType.ADMIN, UserType.PROFESSOR));

        User student1 = userService.addUserInternal("Steve Jobs", "student1@trackdev.com", global.getPasswordEncoder().encode("1111"), List.of(UserType.STUDENT));
        User student2 = userService.addUserInternal("Mark Zuckerberg", "student2@trackdev.com", global.getPasswordEncoder().encode("2222"), List.of(UserType.STUDENT));
        User student3 = userService.addUserInternal("Jeff Bezos", "student3@trackdev.com", global.getPasswordEncoder().encode("3333"), List.of(UserType.STUDENT));
        User student4 = userService.addUserInternal("Elon Musk", "student4@trackdev.com", global.getPasswordEncoder().encode("4444"), List.of(UserType.STUDENT));

        //DEMO PFG
        User demo1 = userService.addUserInternal("Bill Gates", "demo1@trackdev.com", global.getPasswordEncoder().encode("B123123g"), List.of(UserType.STUDENT));
        User demo2 = userService.addUserInternal("Alan Turing", "demo2@trackdev.com", global.getPasswordEncoder().encode("A123123t"), List.of(UserType.STUDENT));
        User demo3 = userService.addUserInternal("Michael Sipser", "demo3@trackdev.com", global.getPasswordEncoder().encode("M123123s"), List.of(UserType.STUDENT));
        User demo4 = userService.addUserInternal("James Gosling", "demo4@trackdev.com", global.getPasswordEncoder().encode("J123123g"), List.of(UserType.STUDENT));

        enrolledStudents.add(student1);
        enrolledStudents.add(student2);
        enrolledStudents.add(student3);
        enrolledStudents.add(student4);

        // Subject
        Subject subject = subjectService.createSubject("PDS2024","PDS" ,admin.getId());
        Subject subject1 = subjectService.createSubject("Projecte web","PW" ,admin.getId());
        Subject subject2 = subjectService.createSubject("Sistema de Computadors I","SO1" ,admin.getId());
        Subject subject3 = subjectService.createSubject("Sistema de Computadors II","SO2" ,admin.getId());
        Subject subject4 = subjectService.createSubject("Projectes final de grau","PFG" ,admin.getId());

        // Course
        Course course = courseService.createCourse(subject.getId(), 2024,null, admin.getId());
        Course course4 = courseService.createCourse(subject.getId(), 2025,null, admin.getId());
        Course course1 = courseService.createCourse(subject1.getId(), 2026,null, admin.getId());
        Course course2 = courseService.createCourse(subject2.getId(), 2027,null, admin.getId());
        Course course3 = courseService.createCourse(subject3.getId(), 2028,null, admin.getId());

        // one subject set up
        populateProject(admin, course, "Movie reviews", enrolledStudents.subList(0,12));
        populateProject(admin, course, "Calendar", enrolledStudents.subList(12,22));
        populateProject(admin, course, "FaceNotes", enrolledStudents.subList(22,26));
        populateProject(admin, course, "FaceNotes", enrolledStudents.subList(22,26));

        //DEMO PFG
        Course course5 = courseService.createCourse(subject4.getId(), 2023,null, admin.getId());
        populateProjectDemo(
                admin,
                course5,
                "Aplicació web per la gestió de projectes en metodologia Agile",
                new ArrayList<>(Arrays.asList(nacho, gerard, marc, demo1, demo2, demo3, demo4))
        );

        logger.info("Done populating database");
    }

    private void populateProjectDemo(User admin, Course course, String projectName, List<User> users){
        List<String> emails = new ArrayList<>();
        for(User user: users) {
            emails.add(user.getEmail());
        }
        Project project = projectService.createProject(projectName, emails, course.getId(), admin.getId());
        //PRIMER SPRINT
        LocalDate start1 = LocalDate.of(2024,1,29);
        LocalDate end2 = start1.plusDays(14);
        User sprintCreator1 = users.get(1);
        Sprint sprint1 = sprintService.create(project, "Sprint 1", Date.from(start1.atStartOfDay(ZoneId.systemDefault()).toInstant()), Date.from(end2.atStartOfDay(ZoneId.systemDefault()).toInstant()), sprintCreator1.getId());
        //Task1
        Task task1 = taskService.createTask(project.getId(), "Anàlisis i disseny de la base de dades", users.get(2).getId());
        MergePatchTask editTask1 = new MergePatchTask();
        editTask1.description = Optional.of("S'ha de fer una anàlisi i disseny de la base de dades pel desenvolupament del projecte final de grau. S'ha de fer un diagrama entitat-relació i un diagrama de classes.");
        editTask1.assignee = Optional.of(users.get(2).getEmail());
        editTask1.estimationPoints = Optional.of(5);
        editTask1.activeSprints = Optional.of(new ArrayList<>(Collections.singletonList(sprint1.getId())));
        taskService.editTask(task1.getId(), editTask1, users.get(2).getId());
        commentService.addComment( "Algú sap si necessitarem crear un xat?", users.get(2),task1);
        commentService.addComment( "No ho tinc clar, és un stopper? ", users.get(3),task1);
        commentService.addComment( "Sí, no sé si haig de crear l'objecte \"comentari\"", users.get(2),task1);
        commentService.addComment( "Jo crec que sí que el crearem, fem-ho", users.get(4),task1);
        commentService.addComment( "Agree", users.get(5),task1);
        commentService.addComment( "Em sembla bé també", users.get(6),task1);
        commentService.addComment( "Recordeu que una funcionalitat no completada no contarà per l'sprint", users.get(0),task1);
        Task subtask11 = taskService.createSubTask(task1.getId(), "Diagrama entitat-relació", users.get(2).getId());
        Task subtask12 = taskService.createSubTask(task1.getId(), "Diagrama classes", users.get(2).getId());
        MergePatchTask editTaskSub11 = new MergePatchTask();
        MergePatchTask editTaskSub12 = new MergePatchTask();
        editTaskSub11.assignee = Optional.of(users.get(2).getEmail());
        editTaskSub12.assignee = Optional.of(users.get(2).getEmail());
        editTaskSub11.activeSprints = Optional.of(new ArrayList<>(Collections.singletonList(sprint1.getId())));
        editTaskSub12.activeSprints = Optional.of(new ArrayList<>(Collections.singletonList(sprint1.getId())));
        taskService.editTask(subtask11.getId(), editTaskSub11, users.get(0).getId());
        taskService.editTask(subtask12.getId(), editTaskSub12, users.get(0).getId());

        userService.setCurrentProject(admin,project);
    }

    private void populateProject(User admin, Course course, String projectName, List<User> users) {
        List<String> emails = new ArrayList<>();
        for(User user: users) {
            emails.add(user.getEmail());
        }

        Project project = projectService.createProject(projectName, emails, course.getId(), admin.getId());

        Random random = new Random();
        LocalDate start = LocalDate.of(2021,3,1);
        LocalDate end = start.plusDays(14);
        populatePastSprint(project, "First iteration", start, end, users, true);
        start = end;
        end = start.plusDays(14);
        //populatePastSprint(backlog.getId(), "Second iteration", start, end, users, true);
        start = end;
        end = start.plusDays(14);
        //populatePastSprint(backlog.getId(), "Third iteration", start, end, users, false);

        for(int i = 0; i <= 15; i++) {
            User reporter = users.get(random.nextInt(users.size()));
            Task task = taskService.createTask(project.getId(), "Lorem ipsum dolor sit amet", reporter.getId());

            if(random.nextBoolean()) {
                MergePatchTask editTask = buildBacklogEditTask(users, random);
                taskService.editTask(task.getId(), editTask, reporter.getId());
            }
            if(i == 0) {
                taskService.createSubTask(task.getId(), "Create endpoint GET /items", reporter.getId());
                taskService.createSubTask(task.getId(), "Show list of items in UI", reporter.getId());
                taskService.createSubTask(task.getId(), "Style items", reporter.getId());
            }
        }
    }

    private void populatePastSprint(Project project, String name, LocalDate start, LocalDate end, List<User> users, boolean close) {
        Random random = new Random();
        User sprintCreator = users.get(random.nextInt(users.size()));
        Sprint sprint = sprintService.create(project, name, Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant()), Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant()), sprintCreator.getId());

        List<Task> tasks = createTasks(project.getId(), 5, users, random);
        User editor = users.get(random.nextInt(users.size()));
        Integer rank = 1;
        for(Task task : tasks) {
            // Add to sprint
            MergePatchTask change = new MergePatchTask();
            Collection<Long> sprints = new ArrayList<>();
            sprints.add(sprint.getId());
            change.activeSprints = Optional.of(sprints);
            change.rank = Optional.of(rank);
            taskService.editTask(task.getId(), change, editor.getId());

            // Random change
            MergePatchTask editTask = buildBacklogEditTask(users, random);
            taskService.editTask(task.getId(), editTask, editor.getId());

            rank++;
        }
        saveOpenSprint(sprintCreator, sprint);

        if(close) {
            for(Task task : tasks) {
                if(task.getStatus() != TaskStatus.DONE) {
                    MergePatchTask change = new MergePatchTask();
                    change.status = Optional.of(TaskStatus.DONE);
                    taskService.editTask(task.getId(), change, editor.getId());
                }
            }
            saveCloseSprint(sprintCreator, sprint);
        } else {
            for(Task task : tasks) {
                if(random.nextBoolean()) {
                    MergePatchTask change = new MergePatchTask();
                    change.status = Optional.of(getRandomStatus(random));
                    taskService.editTask(task.getId(), change, editor.getId());
                }
            }
        }
    }

    private void saveOpenSprint(User sprintCreator, Sprint sprint) {
        MergePatchSprint sprintChange = new MergePatchSprint();
        sprintChange.status = Optional.of(SprintStatus.ACTIVE);
        sprintService.editSprint(sprint.getId(), sprintChange, sprintCreator.getId());
    }

    private void saveCloseSprint(User sprintCreator, Sprint sprint) {
        MergePatchSprint sprintChange = new MergePatchSprint();
        sprintChange.status = Optional.of(SprintStatus.CLOSED);
        sprintService.editSprint(sprint.getId(), sprintChange, sprintCreator.getId());
    }

    private List<Task> createTasks(Long projectId, int amount, List<User> users, Random random) {
        List<Task> tasks = new ArrayList<>();
        for(int i = 0; i <= amount; i++) {
            User reporter = users.get(random.nextInt(users.size()));
            Task task = taskService.createTask(projectId, "Lorem ipsum dolor sit amet", reporter.getId());
            tasks.add(task);
        }
        return tasks;
    }

    private MergePatchTask buildBacklogEditTask(List<User> users, Random random) {
        Integer points = possibleEstimationPoints.get(random.nextInt(possibleEstimationPoints.size()));
        User assignee = users.get(random.nextInt(users.size()));

        MergePatchTask editTask = new MergePatchTask();
        editTask.assignee = Optional.of(assignee.getEmail());
        editTask.estimationPoints = Optional.of(points);
        return editTask;
    }

    private TaskStatus getRandomStatus(Random random) {
        TaskStatus status = TaskStatus.BACKLOG;
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

}
