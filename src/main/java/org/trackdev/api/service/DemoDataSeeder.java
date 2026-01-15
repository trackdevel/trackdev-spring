package org.trackdev.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.trackdev.api.configuration.UserType;
import org.trackdev.api.entity.*;
import org.trackdev.api.model.MergePatchSprint;
import org.trackdev.api.model.MergePatchTask;
import org.trackdev.api.model.SprintPatternRequest;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * Seeds the database with demo data for development and testing.
 * Creates a complete dataset with 2 workspaces, each with workspace admins,
 * professors, subjects, courses, projects, sprint patterns, sprints, and tasks.
 */
@Component
public class DemoDataSeeder {

    private final Logger logger = LoggerFactory.getLogger(DemoDataSeeder.class);
    private final List<Integer> possibleEstimationPoints = Arrays.asList(1, 2, 3, 5, 8, 13);
    private final Random random = new Random();

    // Story name templates for realistic task names
    private final List<String> storyTemplates = Arrays.asList(
        "As a user, I want to view my dashboard",
        "As a user, I want to manage my profile",
        "As a user, I want to search for items",
        "As a user, I want to filter results",
        "As a user, I want to export data",
        "As a user, I want to receive notifications",
        "As a user, I want to configure settings",
        "As a user, I want to share content",
        "As a user, I want to view reports",
        "As a user, I want to track progress",
        "As a user, I want to collaborate with team",
        "As a user, I want to manage permissions",
        "As a user, I want to upload files",
        "As a user, I want to download reports",
        "As a user, I want to customize views",
        "As a user, I want to archive items",
        "As a user, I want to restore data",
        "As a user, I want to audit changes",
        "As a user, I want to schedule tasks",
        "As a user, I want to set reminders",
        "As a user, I want to add comments",
        "As a user, I want to assign tasks",
        "As a user, I want to track time",
        "As a user, I want to view history"
    );

    private final List<String> subtaskPrefixes = Arrays.asList(
        "Design", "Implement", "Test", "Review", "Document", "Refactor"
    );

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private WorkspaceService workspaceService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SprintService sprintService;

    @Autowired
    private SprintPatternService sprintPatternService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private CommentService commentService;

    public void seedDemoData() {
        logger.info("Starting database seeding...");

        // ============================================
        // 1. CREATE ADMIN USER
        // ============================================
        
        User admin = userService.addUserInternal(
            "admin",
            "TrackDev Admin", 
            "admin@trackdev.com", 
            passwordEncoder.encode("admin"), 
            List.of(UserType.ADMIN)
        );
        
        // ============================================
        // 2. CREATE TWO WORKSPACES
        // ============================================
        
        Workspace workspaceUdG = workspaceService.createWorkspace("Universitat de Girona", admin.getId());
        Workspace workspaceUB = workspaceService.createWorkspace("Universitat de Barcelona", admin.getId());
        
        logger.info("Created 2 workspaces: {} and {}", workspaceUdG.getName(), workspaceUB.getName());

        // ============================================
        // 3. CREATE WORKSPACE ADMINS
        // ============================================
        
        User wsAdminUdG = createUserWithWorkspace(
            "wsadmin-udg",
            "Admin UdG",
            "wsadmin.udg@trackdev.com",
            "wsadmin",
            List.of(UserType.WORKSPACE_ADMIN),
            workspaceUdG
        );
        
        User wsAdminUB = createUserWithWorkspace(
            "wsadmin-ub",
            "Admin UB",
            "wsadmin.ub@trackdev.com",
            "wsadmin",
            List.of(UserType.WORKSPACE_ADMIN),
            workspaceUB
        );
        
        logger.info("Created 2 workspace admins");

        // ============================================
        // 4. CREATE PROFESSORS FOR EACH WORKSPACE
        // ============================================
        
        // Professors for UdG
        User professorPDS = createUserWithWorkspace(
            "maria-garcia",
            "Maria Garcia",
            "maria.garcia@trackdev.com",
            "professor",
            List.of(UserType.PROFESSOR),
            workspaceUdG
        );
        
        User professorTFG = createUserWithWorkspace(
            "joan-puig",
            "Joan Puig",
            "joan.puig@trackdev.com",
            "professor",
            List.of(UserType.PROFESSOR),
            workspaceUdG
        );
        
        // Professors for UB
        User professorSO = createUserWithWorkspace(
            "carlos-martinez",
            "Carlos Martinez",
            "carlos.martinez@trackdev.com",
            "professor",
            List.of(UserType.PROFESSOR),
            workspaceUB
        );
        
        User professorBD = createUserWithWorkspace(
            "ana-lopez",
            "Ana Lopez",
            "ana.lopez@trackdev.com",
            "professor",
            List.of(UserType.PROFESSOR),
            workspaceUB
        );
        
        logger.info("Created 4 professors (2 per workspace)");

        // ============================================
        // 5. CREATE STUDENTS FOR EACH WORKSPACE
        // ============================================
        
        List<User> studentsUdG = createStudentsForWorkspace(workspaceUdG, "udg", 6);
        List<User> studentsUB = createStudentsForWorkspace(workspaceUB, "ub", 6);
        
        logger.info("Created 12 students (6 per workspace)");

        // ============================================
        // 6. CREATE SUBJECTS FOR EACH WORKSPACE
        // ============================================
        
        // Subjects for UdG (owned by workspace admin)
        Subject subjectPDS = createSubjectWithWorkspace(
            "Projecte de Desenvolupament de Software",
            "PDS",
            wsAdminUdG,
            workspaceUdG
        );
        
        Subject subjectTFG = createSubjectWithWorkspace(
            "Treball Final de Grau",
            "TFG",
            wsAdminUdG,
            workspaceUdG
        );
        
        // Subjects for UB (owned by workspace admin)
        Subject subjectSO = createSubjectWithWorkspace(
            "Sistemas Operativos",
            "SO",
            wsAdminUB,
            workspaceUB
        );
        
        Subject subjectBD = createSubjectWithWorkspace(
            "Bases de Datos",
            "BD",
            wsAdminUB,
            workspaceUB
        );
        
        logger.info("Created 4 subjects (2 per workspace)");

        // ============================================
        // 7. CREATE COURSES FOR EACH SUBJECT
        // ============================================
        
        // Courses for UdG
        Course coursePDS = courseService.createCourse(
            subjectPDS.getId(),
            2025,
            null,
            professorPDS.getId()
        );
        
        Course courseTFG = courseService.createCourse(
            subjectTFG.getId(),
            2025,
            null,
            professorTFG.getId()
        );
        
        // Courses for UB
        Course courseSO = courseService.createCourse(
            subjectSO.getId(),
            2025,
            null,
            professorSO.getId()
        );
        
        Course courseBD = courseService.createCourse(
            subjectBD.getId(),
            2025,
            null,
            professorBD.getId()
        );
        
        logger.info("Created 4 courses (2 per workspace)");

        // ============================================
        // 8. CREATE SPRINT PATTERNS
        // ============================================
        
        // Calculate sprint dates so that the 4th sprint is active today
        LocalDate today = LocalDate.now();
        LocalDate sprint4Start = today.minusDays(7);
        LocalDate sprint4End = sprint4Start.plusDays(14);
        LocalDate sprint3Start = sprint4Start.minusDays(14);
        LocalDate sprint3End = sprint4Start;
        LocalDate sprint2Start = sprint3Start.minusDays(14);
        LocalDate sprint2End = sprint3Start;
        LocalDate sprint1Start = sprint2Start.minusDays(14);
        LocalDate sprint1End = sprint2Start;

        // Sprint pattern for UdG courses
        SprintPattern patternUdG = createSprintPattern(
            "Setmanal UdG",
            coursePDS,
            professorPDS,
            sprint1Start, sprint1End,
            sprint2Start, sprint2End,
            sprint3Start, sprint3End,
            sprint4Start, sprint4End
        );
        
        // Sprint pattern for UB courses  
        SprintPattern patternUB = createSprintPattern(
            "Quincenal UB",
            courseSO,
            professorSO,
            sprint1Start, sprint1End,
            sprint2Start, sprint2End,
            sprint3Start, sprint3End,
            sprint4Start, sprint4End
        );
        
        logger.info("Created 2 sprint patterns");

        // ============================================
        // 9. CREATE PROJECTS WITH SPRINTS AND TASKS
        // ============================================
        
        // Project for PDS course (UdG)
        Project projectPDS = createProjectWithSprintsAndTasks(
            "pds25a",
            studentsUdG.subList(0, 3),  // First 3 students
            coursePDS,
            professorPDS,
            sprint1Start, sprint1End,
            sprint2Start, sprint2End,
            sprint3Start, sprint3End,
            sprint4Start, sprint4End
        );
        
        // Second project for PDS course
        Project projectPDS2 = createProjectWithSprintsAndTasks(
            "pds25b",
            studentsUdG.subList(3, 6),  // Last 3 students
            coursePDS,
            professorPDS,
            sprint1Start, sprint1End,
            sprint2Start, sprint2End,
            sprint3Start, sprint3End,
            sprint4Start, sprint4End
        );
        
        // Project for TFG course (UdG)
        Project projectTFG = createProjectWithSprintsAndTasks(
            "tfg25-group1",
            studentsUdG.subList(0, 2),  // First 2 students
            courseTFG,
            professorTFG,
            sprint1Start, sprint1End,
            sprint2Start, sprint2End,
            sprint3Start, sprint3End,
            sprint4Start, sprint4End
        );
        
        // Project for SO course (UB)
        Project projectSO = createProjectWithSprintsAndTasks(
            "so25-team1",
            studentsUB.subList(0, 3),  // First 3 students
            courseSO,
            professorSO,
            sprint1Start, sprint1End,
            sprint2Start, sprint2End,
            sprint3Start, sprint3End,
            sprint4Start, sprint4End
        );
        
        // Second project for SO course
        Project projectSO2 = createProjectWithSprintsAndTasks(
            "so25-team2",
            studentsUB.subList(3, 6),  // Last 3 students
            courseSO,
            professorSO,
            sprint1Start, sprint1End,
            sprint2Start, sprint2End,
            sprint3Start, sprint3End,
            sprint4Start, sprint4End
        );
        
        // Project for BD course (UB)
        Project projectBD = createProjectWithSprintsAndTasks(
            "bd25-grupo1",
            studentsUB.subList(0, 4),  // First 4 students
            courseBD,
            professorBD,
            sprint1Start, sprint1End,
            sprint2Start, sprint2End,
            sprint3Start, sprint3End,
            sprint4Start, sprint4End
        );
        
        logger.info("Created 6 projects with sprints and tasks");

        // Set default current projects for professors
        userService.setCurrentProject(professorPDS, projectPDS);
        userService.setCurrentProject(professorTFG, projectTFG);
        userService.setCurrentProject(professorSO, projectSO);
        userService.setCurrentProject(professorBD, projectBD);

        logger.info("Database seeding completed successfully!");
        logger.info("Summary:");
        logger.info("  - 2 workspaces");
        logger.info("  - 2 workspace admins");
        logger.info("  - 4 professors");
        logger.info("  - 12 students");
        logger.info("  - 4 subjects");
        logger.info("  - 4 courses");
        logger.info("  - 2 sprint patterns");
        logger.info("  - 6 projects with sprints and tasks");
    }

    /**
     * Create a user and assign them to a workspace
     */
    private User createUserWithWorkspace(String username, String fullName, String email, String password, 
                                          List<UserType> roles, Workspace workspace) {
        User user = userService.addUserInternal(username, fullName, email, passwordEncoder.encode(password), roles);
        user.setWorkspace(workspace);
        userService.save(user);
        return user;
    }

    /**
     * Create students for a workspace
     */
    private List<User> createStudentsForWorkspace(Workspace workspace, String prefix, int count) {
        List<String> firstNames = Arrays.asList(
            "Alice", "Bob", "Carol", "David", "Eva", "Frank",
            "Grace", "Henry", "Iris", "James", "Kate", "Leo"
        );
        List<String> lastNames = Arrays.asList(
            "Johnson", "Smith", "Williams", "Brown", "Martinez", "Garcia",
            "Davis", "Wilson", "Anderson", "Taylor", "Thomas", "Moore"
        );
        
        List<User> students = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String firstName = firstNames.get(i % firstNames.size());
            String lastName = lastNames.get(i % lastNames.size());
            String fullName = firstName + " " + lastName;
            String username = (firstName + "-" + lastName).toLowerCase();
            String email = prefix + ".student" + (i + 1) + "@trackdev.com";
            User student = createUserWithWorkspace(
                username,
                fullName, 
                email, 
                "student" + (i + 1), 
                List.of(UserType.STUDENT),
                workspace
            );
            students.add(student);
        }
        return students;
    }

    /**
     * Create a subject and assign it to a workspace
     */
    private Subject createSubjectWithWorkspace(String name, String acronym, User owner, Workspace workspace) {
        Subject subject = subjectService.createSubject(name, acronym, owner.getId());
        subject.setWorkspace(workspace);
        subjectService.save(subject);
        return subject;
    }

    /**
     * Create a sprint pattern for a course
     */
    private SprintPattern createSprintPattern(String name, Course course, User professor,
                                               LocalDate s1Start, LocalDate s1End,
                                               LocalDate s2Start, LocalDate s2End,
                                               LocalDate s3Start, LocalDate s3End,
                                               LocalDate s4Start, LocalDate s4End) {
        SprintPatternRequest patternRequest = new SprintPatternRequest();
        patternRequest.name = name;
        patternRequest.items = new ArrayList<>();
        
        patternRequest.items.add(createPatternItem("Sprint 1", s1Start, s1End, 0));
        patternRequest.items.add(createPatternItem("Sprint 2", s2Start, s2End, 1));
        patternRequest.items.add(createPatternItem("Sprint 3", s3Start, s3End, 2));
        patternRequest.items.add(createPatternItem("Sprint 4", s4Start, s4End, 3));

        return sprintPatternService.createPattern(course.getId(), patternRequest, professor.getId());
    }

    /**
     * Create a project with sprints and tasks
     */
    private Project createProjectWithSprintsAndTasks(String projectName, List<User> students,
                                                      Course course, User professor,
                                                      LocalDate s1Start, LocalDate s1End,
                                                      LocalDate s2Start, LocalDate s2End,
                                                      LocalDate s3Start, LocalDate s3End,
                                                      LocalDate s4Start, LocalDate s4End) {
        // Create project
        List<String> studentEmails = students.stream().map(User::getEmail).toList();
        Project project = projectService.createProject(projectName, studentEmails, course.getId(), professor.getId());

        // Create sprints
        Sprint sprint1 = sprintService.create(project, "Sprint 1", toDate(s1Start), toDate(s1End), professor.getId());
        Sprint sprint2 = sprintService.create(project, "Sprint 2", toDate(s2Start), toDate(s2End), professor.getId());
        Sprint sprint3 = sprintService.create(project, "Sprint 3", toDate(s3Start), toDate(s3End), professor.getId());
        Sprint sprint4 = sprintService.create(project, "Sprint 4", toDate(s4Start), toDate(s4End), professor.getId());

        List<Sprint> allSprints = List.of(sprint1, sprint2, sprint3, sprint4);
        int storyIndex = random.nextInt(storyTemplates.size());

        for (int i = 0; i < allSprints.size(); i++) {
            Sprint sprint = allSprints.get(i);
            boolean isActiveSprint = (i == 3);
            boolean isClosedSprint = (i < 3);

            // Activate the sprint first
            activateSprint(sprint, professor.getId());

            // Create 4-6 stories per sprint
            int storiesCount = 4 + random.nextInt(3);
            for (int s = 0; s < storiesCount; s++) {
                User reporter = students.get(random.nextInt(students.size()));
                String storyName = storyTemplates.get(storyIndex % storyTemplates.size());
                storyIndex++;

                Task story = taskService.createTask(project.getId(), storyName, reporter.getId());

                User assignee = students.get(random.nextInt(students.size()));
                MergePatchTask storyEdit = new MergePatchTask();
                storyEdit.assignee = Optional.of(assignee.getEmail());
                storyEdit.estimationPoints = Optional.of(possibleEstimationPoints.get(random.nextInt(possibleEstimationPoints.size())));
                storyEdit.activeSprints = Optional.of(List.of(sprint.getId()));
                storyEdit.rank = Optional.of(s + 1);

                if (isClosedSprint) {
                    storyEdit.status = Optional.of(TaskStatus.DONE);
                } else if (isActiveSprint) {
                    storyEdit.status = Optional.of(getRandomActiveStatus());
                }

                taskService.editTaskInternal(story.getId(), storyEdit, assignee.getId());

                // Create 2-4 subtasks for each story
                // Subtasks are created by the assignee (who has permission)
                int subtaskCount = 2 + random.nextInt(3);
                for (int t = 0; t < subtaskCount; t++) {
                    String subtaskName = subtaskPrefixes.get(t % subtaskPrefixes.size()) + " " +
                        storyName.replace("As a user, I want to ", "").toLowerCase();

                    Task subtask = taskService.createSubTask(story.getId(), subtaskName, assignee.getId());

                    User subtaskAssignee = students.get(random.nextInt(students.size()));
                    MergePatchTask subtaskEdit = new MergePatchTask();
                    subtaskEdit.assignee = Optional.of(subtaskAssignee.getEmail());
                    subtaskEdit.estimationPoints = Optional.of(possibleEstimationPoints.get(random.nextInt(3)));
                    subtaskEdit.activeSprints = Optional.of(List.of(sprint.getId()));

                    if (isClosedSprint) {
                        subtaskEdit.status = Optional.of(TaskStatus.DONE);
                    } else if (isActiveSprint) {
                        subtaskEdit.status = Optional.of(getRandomActiveStatus());
                    }

                    taskService.editTaskInternal(subtask.getId(), subtaskEdit, subtaskAssignee.getId());
                }

                // Add comments to stories in active sprint
                if (isActiveSprint && random.nextBoolean()) {
                    addRandomComments(story, students);
                }
            }

            // Close sprints 1-3
            if (isClosedSprint) {
                closeSprint(sprint, professor.getId());
            }
        }

        // Create backlog tasks
        int backlogCount = 2 + random.nextInt(3);
        for (int i = 0; i < backlogCount; i++) {
            User reporter = students.get(random.nextInt(students.size()));
            String storyName = storyTemplates.get((storyIndex + i) % storyTemplates.size());

            Task backlogTask = taskService.createTask(project.getId(), storyName, reporter.getId());

            MergePatchTask backlogEdit = new MergePatchTask();
            backlogEdit.estimationPoints = Optional.of(possibleEstimationPoints.get(random.nextInt(possibleEstimationPoints.size())));
            taskService.editTaskInternal(backlogTask.getId(), backlogEdit, reporter.getId());
        }

        return project;
    }

    /**
     * Create a sprint pattern item request
     */
    private SprintPatternRequest.SprintPatternItemRequest createPatternItem(
            String name, LocalDate start, LocalDate end, int order) {
        SprintPatternRequest.SprintPatternItemRequest item = new SprintPatternRequest.SprintPatternItemRequest();
        item.name = name;
        item.startDate = toDate(start);
        item.endDate = toDate(end);
        item.orderIndex = order;
        return item;
    }

    /**
     * Convert LocalDate to Date
     */
    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Activate a sprint
     */
    private void activateSprint(Sprint sprint, String userId) {
        MergePatchSprint change = new MergePatchSprint();
        change.status = Optional.of(SprintStatus.ACTIVE);
        sprintService.editSprintInternal(sprint.getId(), change, userId);
    }

    /**
     * Close a sprint
     */
    private void closeSprint(Sprint sprint, String userId) {
        MergePatchSprint change = new MergePatchSprint();
        change.status = Optional.of(SprintStatus.CLOSED);
        sprintService.editSprintInternal(sprint.getId(), change, userId);
    }

    /**
     * Get a random status for active sprint tasks
     */
    private TaskStatus getRandomActiveStatus() {
        TaskStatus[] statuses = { TaskStatus.TODO, TaskStatus.INPROGRESS, TaskStatus.VERIFY, TaskStatus.DONE };
        return statuses[random.nextInt(statuses.length)];
    }

    /**
     * Add random comments to a task
     */
    private void addRandomComments(Task task, List<User> students) {
        List<String> comments = Arrays.asList(
            "Working on this now",
            "Need some clarification on the requirements",
            "Almost done, just finishing tests",
            "Blocked by dependency, waiting for review",
            "Completed the implementation, ready for review",
            "Found a bug, investigating",
            "Updated the design based on feedback"
        );

        int numComments = random.nextInt(3) + 1;
        for (int i = 0; i < numComments; i++) {
            User commenter = students.get(random.nextInt(students.size()));
            String comment = comments.get(random.nextInt(comments.size()));
            commentService.addComment(comment, commenter, task);
        }
    }
}
