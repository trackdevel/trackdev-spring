package org.trackdev.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.trackdev.api.configuration.UserType;
import org.trackdev.api.entity.*;
import org.trackdev.api.model.MergePatchSprint;
import org.trackdev.api.model.MergePatchTask;
import org.trackdev.api.model.SprintPatternRequest;
import org.trackdev.api.repository.SprintRepository;

import java.time.LocalDate;
import java.time.ZonedDateTime;
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
    private SprintRepository sprintRepository;

    @Autowired
    private SprintPatternService sprintPatternService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private GitHubRepoService gitHubRepoService;

    @Autowired
    private Environment environment;

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
        
        // Create 8 students per workspace (6 will be assigned to projects, 2 will be enrolled only)
        List<User> studentsUdG = createStudentsForWorkspace(workspaceUdG, "udg", 8);
        List<User> studentsUB = createStudentsForWorkspace(workspaceUB, "ub", 8);
        
        logger.info("Created 16 students (8 per workspace)");

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
        // 8. ENROLL STUDENTS IN COURSES
        // ============================================
        
        // Enroll UdG students in UdG courses
        for (User student : studentsUdG) {
            coursePDS.addStudent(student);
            courseTFG.addStudent(student);
        }
        courseService.save(coursePDS);
        courseService.save(courseTFG);
        
        // Enroll UB students in UB courses
        for (User student : studentsUB) {
            courseSO.addStudent(student);
            courseBD.addStudent(student);
        }
        courseService.save(courseSO);
        courseService.save(courseBD);
        
        logger.info("Enrolled students in their respective courses");

        // ============================================
        // 9. CREATE SPRINT PATTERNS
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
        
        // Create sprint patterns for all courses
        SprintPattern patternTFG = createSprintPattern(
            "Setmanal TFG",
            courseTFG,
            professorTFG,
            sprint1Start, sprint1End,
            sprint2Start, sprint2End,
            sprint3Start, sprint3End,
            sprint4Start, sprint4End
        );
        
        SprintPattern patternBD = createSprintPattern(
            "Quincenal BD",
            courseBD,
            professorBD,
            sprint1Start, sprint1End,
            sprint2Start, sprint2End,
            sprint3Start, sprint3End,
            sprint4Start, sprint4End
        );
        
        logger.info("Created 4 sprint patterns (one per course)");

        // ============================================
        // 10. CREATE PROJECTS WITH SPRINTS AND TASKS
        // ============================================
        
        // Project for PDS course (UdG) - apply sprint pattern to create sprints
        Project projectPDS = createProjectWithSprintsAndTasks(
            "pds25a",
            studentsUdG.subList(0, 3),  // First 3 students
            coursePDS,
            professorPDS,
            patternUdG
        );
        
        // Second project for PDS course
        Project projectPDS2 = createProjectWithSprintsAndTasks(
            "pds25b",
            studentsUdG.subList(3, 6),  // Last 3 students
            coursePDS,
            professorPDS,
            patternUdG
        ); 
        
        // Third project for PDS course
        Project projectPDS3 = createProjectWithSprintsAndTasks(
            "pds25c",
            studentsUdG.subList(1, 6),  // Last 3 students
            coursePDS,
            professorPDS,
            patternUdG
        );


        if (environment.getProperty("GITHUB_REPO1_URL") != null && environment.getProperty("GITHUB_REPO1_TOKEN") != null) {
            gitHubRepoService.addRepository(
                projectPDS.getId(), 
                "TrackDev nextjs",
                environment.getProperty("GITHUB_REPO1_URL"), 
                environment.getProperty("GITHUB_REPO1_TOKEN"),
                professorPDS.getId()
            );
        }
        if (environment.getProperty("GITHUB_REPO2_URL") != null && environment.getProperty("GITHUB_REPO2_TOKEN") != null) {
            gitHubRepoService.addRepository(
                projectPDS.getId(), 
                "TrackDev nextjs",
                environment.getProperty("GITHUB_REPO2_URL"), 
                environment.getProperty("GITHUB_REPO2_TOKEN"),
                professorPDS.getId()
            );
        }

        // Project for TFG course (UdG)
        Project projectTFG = createProjectWithSprintsAndTasks(
            "tfg25-group1",
            studentsUdG.subList(0, 2),  // First 2 students
            courseTFG,
            professorTFG,
            patternTFG
        );
        
        // Project for SO course (UB)
        Project projectSO = createProjectWithSprintsAndTasks(
            "so25-team1",
            studentsUB.subList(0, 3),  // First 3 students
            courseSO,
            professorSO,
            patternUB
        );
        
        // Second project for SO course
        Project projectSO2 = createProjectWithSprintsAndTasks(
            "so25-team2",
            studentsUB.subList(3, 6),  // Last 3 students
            courseSO,
            professorSO,
            patternUB
        );
        
        // Project for BD course (UB)
        Project projectBD = createProjectWithSprintsAndTasks(
            "bd25-grupo1",
            studentsUB.subList(0, 4),  // First 4 students
            courseBD,
            professorBD,
            patternBD
        );
        
        logger.info("Created 6 projects with sprints and tasks");

        // Set default current projects for professors
        userService.setCurrentProject(professorPDS, projectPDS);
        userService.setCurrentProject(professorTFG, projectTFG);
        userService.setCurrentProject(professorSO, projectSO);
        userService.setCurrentProject(professorBD, projectBD);

        // ============================================
        // 11. CREATE SAMPLE REPORT FOR PDS COURSE
        // ============================================
        
        // Create a report for PDS 2025 course with Maria Garcia as owner
        Report reportPDS = reportService.createReport("Student Sprint Progress Report", professorPDS.getId());
        reportPDS.setRowType(ReportAxisType.STUDENTS);
        reportPDS.setColumnType(ReportAxisType.SPRINTS);
        reportPDS.setElement(ReportElement.TASK);
        reportPDS.setMagnitude(ReportMagnitude.ESTIMATION_POINTS);
        reportPDS.setCourse(coursePDS);
        reportService.save(reportPDS);
        
        logger.info("Created sample report for PDS course");

        logger.info("Database seeding completed successfully!");
        logger.info("Summary:");
        logger.info("  - 2 workspaces");
        logger.info("  - 2 workspace admins");
        logger.info("  - 4 professors");
        logger.info("  - 16 students (all enrolled in courses, but 4 not assigned to any project)");
        logger.info("  - 4 subjects");
        logger.info("  - 4 courses");
        logger.info("  - 4 sprint patterns (one per course)");
        logger.info("  - 6 projects with sprints created from patterns and tasks");
        logger.info("  - 1 sample report (PDS 2025)");
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
     * Create a project with sprints (from pattern) and tasks
     */
    private Project createProjectWithSprintsAndTasks(String projectName, List<User> students,
                                                      Course course, User professor,
                                                      SprintPattern sprintPattern) {
        // Create project
        List<String> studentIds = students.stream().map(User::getId).toList();
        Project project = projectService.createProject(projectName, studentIds, course.getId(), professor.getId());

        // Apply sprint pattern to create sprints linked to pattern items
        project = projectService.applySprintPattern(project.getId(), sprintPattern.getId(), professor.getId());

        // Get sprints created from pattern (sorted by order index) - fetch from repository to avoid lazy loading issues
        List<Sprint> allSprints = sprintRepository.findByProject_IdOrderBySprintPatternItem_OrderIndexAsc(project.getId());
        
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
                // USER_STORY estimation points are calculated from subtasks, don't set manually
                storyEdit.activeSprints = Optional.of(List.of(sprint.getId()));
                storyEdit.rank = Optional.of(s + 1);

                if (isClosedSprint) {
                    storyEdit.status = Optional.of(TaskStatus.DONE);
                } else if (isActiveSprint) {
                    storyEdit.status = Optional.of(getRandomActiveStatus());
                }

                taskService.editTaskInternal(story.getId(), storyEdit, assignee.getId());

                // Create 2-4 subtasks for each story
                // Subtasks are created by the assignee (who has permission) and assigned to the same sprint
                int subtaskCount = 2 + random.nextInt(3);
                for (int t = 0; t < subtaskCount; t++) {
                    String subtaskName = subtaskPrefixes.get(t % subtaskPrefixes.size()) + " " +
                        storyName.replace("As a user, I want to ", "").toLowerCase();

                    // Create subtask with sprint assignment
                    Task subtask = taskService.createSubTask(story.getId(), subtaskName, assignee.getId(), sprint.getId());

                    User subtaskAssignee = students.get(random.nextInt(students.size()));
                    
                    // Determine subtask status first
                    TaskStatus subtaskStatus = null;
                    if (isClosedSprint) {
                        subtaskStatus = TaskStatus.DONE;
                    } else if (isActiveSprint) {
                        subtaskStatus = getRandomActiveStatus();
                    }
                    
                    // First edit: set assignee and status
                    MergePatchTask subtaskEdit = new MergePatchTask();
                    subtaskEdit.assignee = Optional.of(subtaskAssignee.getEmail());
                    if (subtaskStatus != null) {
                        subtaskEdit.status = Optional.of(subtaskStatus);
                    }
                    taskService.editTaskInternal(subtask.getId(), subtaskEdit, subtaskAssignee.getId());
                    
                    // Second edit: set estimation points only if in VERIFY or DONE
                    if (subtaskStatus == TaskStatus.VERIFY || subtaskStatus == TaskStatus.DONE) {
                        MergePatchTask estimationEdit = new MergePatchTask();
                        estimationEdit.estimationPoints = Optional.of(possibleEstimationPoints.get(random.nextInt(3)));
                        taskService.editTaskInternal(subtask.getId(), estimationEdit, subtaskAssignee.getId());
                    }
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

        // Create backlog tasks (USER_STORY type, no estimation points since they're in BACKLOG status)
        int backlogCount = 2 + random.nextInt(3);
        for (int i = 0; i < backlogCount; i++) {
            User reporter = students.get(random.nextInt(students.size()));
            String storyName = storyTemplates.get((storyIndex + i) % storyTemplates.size());

            // Create backlog task - no estimation points needed (calculated from subtasks, and subtasks can't have estimation points in BACKLOG)
            taskService.createTask(project.getId(), storyName, reporter.getId());
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
     * Convert LocalDate to ZonedDateTime at start of day in UTC
     */
    private ZonedDateTime toDate(LocalDate localDate) {
        return localDate.atStartOfDay(ZoneId.of("UTC"));
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
