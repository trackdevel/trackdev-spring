package org.trackdev.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.trackdev.api.configuration.UserType;
import org.trackdev.api.entity.*;
import org.trackdev.api.model.MergePatchSprint;
import org.trackdev.api.model.MergePatchTask;
import org.trackdev.api.model.SprintPatternRequest;
import org.trackdev.api.repository.PullRequestRepository;
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
@Lazy
public class DemoDataSeeder {

    private final Logger logger = LoggerFactory.getLogger(DemoDataSeeder.class);
    private final List<Integer> possibleEstimationPoints = Arrays.asList(1, 2, 3, 5, 8, 13);
    private final Random random = new Random();

    /**
     * Counter used to assign distinct, ascending createdAt timestamps to tasks.
     * Each task gets baseTime + (taskCreationOrder * 1 minute) so they are clearly ordered.
     */
    private int taskCreationOrder = 0;
    private static final ZonedDateTime TASK_BASE_TIME = ZonedDateTime.of(
        2025, 1, 1, 10, 0, 0, 0, ZoneId.of("UTC"));

    // Story name templates for realistic task names
    private final List<String> storyTemplates = Arrays.asList(
        "As a user, I want to 'view my dashboard",
        "As a user, I want to 'manage my profile",
        "As a user, I want to 'search for items",
        "As a user, I want to 'filter results",
        "As a user, I want to 'export data",
        "As a user, I want to 'receive notifications",
        "As a user, I want to 'configure settings",
        "As a user, I want to 'share content",
        "As a user, I want to' view reports",
        "As a user, I want to' track progress",
        "As a user, I want to' collaborate with team",
        "As a user, I want to' manage permissions",
        "As a user, I want to' upload files",
        "As a user, I want to' download reports",
        "As a user, I want to' customize views",
        "As a user, I want to' archive items",
        "As a user, I want to' restore data",
        "As a user, I want to' audit changes",
        "As a user, I want to' schedule tasks",
        "As a user, I want to' set reminders",
        "As a user, I want to' add comments",
        "As a user, I want to' assign tasks",
        "As a user, I want to' track time",
        "As a user, I want to' view history"
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
    private PullRequestRepository pullRequestRepository;

    @Autowired
    private org.trackdev.api.repository.GitHubRepoRepository gitHubRepoRepository;

    @Autowired
    private PullRequestService pullRequestService;

    @Autowired
    private org.trackdev.api.repository.ProfileRepository profileRepository;

    @Autowired
    private Environment environment;

    @Autowired
    private org.trackdev.api.configuration.TrackDevProperties trackDevProperties;

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
        
        // Calculate sprint dates so that the 3rd sprint is active today and 4th is in the future
        LocalDate today = LocalDate.now();
        LocalDate sprint3Start = today.minusDays(7);   // Sprint 3 started 7 days ago (ACTIVE)
        LocalDate sprint3End = sprint3Start.plusDays(14);  // Sprint 3 ends in 7 days
        LocalDate sprint4Start = sprint3End;           // Sprint 4 starts when sprint 3 ends (FUTURE)
        LocalDate sprint4End = sprint4Start.plusDays(14);
        LocalDate sprint2Start = sprint3Start.minusDays(14);  // Sprint 2 ended when sprint 3 started (CLOSED)
        LocalDate sprint2End = sprint3Start;
        LocalDate sprint1Start = sprint2Start.minusDays(14);  // Sprint 1 ended when sprint 2 started (CLOSED)
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
        
        // Project pds25a: HARDCODED deterministic data for Alice Johnson (udg.student1@trackdev.com)
        // Shows all possible scenarios: tasks in different sprints with various statuses
        Project projectPDS = createPds25aProject(
            studentsUdG.subList(0, 3),  // First 3 students (Alice, Bob, Carol)
            coursePDS,
            professorPDS,
            patternUdG
        );
        
        // Second project for PDS course (random data)
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

        // Add a test GitHub repo for webhook API testing (no GitHub API validation)
        GitHubRepo testRepo = new GitHubRepo("test-webhook-repo",
                "https://github.com/trackdev-test/webhook-test-repo",
                "fake-token-for-testing", projectPDS);
        testRepo.setWebhookSecret("test-webhook-secret-for-postman");
        testRepo.setWebhookActive(true);
        gitHubRepoRepository.save(testRepo);
        logger.info("Created test GitHub repo for webhook testing (owner=trackdev-test, repo=webhook-test-repo)");

        // Add hardcoded PRs to done tasks for Alice Johnson in pds25a
        if (environment.getProperty("GITHUB_REPO1_URL") != null) {
            String repoUrl = environment.getProperty("GITHUB_REPO1_URL");
            User alice = studentsUdG.get(0); // Alice Johnson
            
            // Link PRs to different DONE tasks
            createHardcodedPullRequest(projectPDS, alice, repoUrl, 8, 0);  // PR #8 to first DONE task
            createHardcodedPullRequest(projectPDS, alice, repoUrl, 22, 1); // PR #22 to second DONE task
            createHardcodedPullRequest(projectPDS, alice, repoUrl, 26, 2); // PR #26 to third DONE task
            createHardcodedPullRequest(projectPDS, alice, repoUrl, 35, 3); // PR #35 to fourth DONE task
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
        
        // Empty sprint project for testing drag/drop from backlog to empty sprint
        // Only includes student1 (udg.student1@trackdev.com)
        Project projectEmptySprint = createProjectWithSprintsOnly(
            "pds25-empty",
            List.of(studentsUdG.get(0)),  // Only student1
            coursePDS,
            professorPDS,
            patternUdG
        );
        
        logger.info("Created 7 projects with sprints and tasks");

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

        // ============================================
        // 13. CREATE DEMO PROFILE
        // ============================================
        
        Profile demoProfile = createDemoProfile(professorPDS);
        courseService.applyProfile(coursePDS.getId(), demoProfile.getId(), professorPDS.getId());
        logger.info("Created demo profile '{}' and applied to course '{}'", demoProfile.getName(), coursePDS.getSubject().getName());

        // ============================================
        // 14. CREATE PERMISSION TEST DATA
        // ============================================
        
        // Get sprints for pds25a (the hardcoded project)
        List<Sprint> pds25aSprints = sprintRepository.findByProjectIdOrderByPatternItemOrderIndex(projectPDS.getId());
        Sprint pastSprint = pds25aSprints.get(0);  // Sprint 1 - CLOSED
        Sprint activeSprint = pds25aSprints.get(2);  // Sprint 3 - ACTIVE
        Sprint futureSprint = pds25aSprints.get(3);  // Sprint 4 - DRAFT (future)
        
        createPermissionTestData(projectPDS, pastSprint, activeSprint, futureSprint, 
                                 studentsUdG.get(0),  // Alice Johnson (udg.student1)
                                 studentsUdG.get(1),  // Bob Smith (udg.student2)
                                 professorPDS);

        logger.info("Database seeding completed successfully!");
        logger.info("Summary:");
        logger.info("  - 2 workspaces");
        logger.info("  - 2 workspace admins");
        logger.info("  - 4 professors");
        logger.info("  - 16 students (all enrolled in courses, but 4 not assigned to any project)");
        logger.info("  - 4 subjects");
        logger.info("  - 4 courses");
        logger.info("  - 4 sprint patterns (one per course)");
        logger.info("  - 7 projects with sprints (1 with empty active sprint for testing)");
        logger.info("  - 1 sample report (PDS 2025)");
        logger.info("  - 1 demo profile with enums and attributes");

        // Conditionally seed stress test data
        if (trackDevProperties.getStressTest().isEnabled()) {
            seedStressTestData(workspaceUdG, wsAdminUdG);
        }
    }

    // ==========================================================================
    // STRESS TEST DATA (dev-only, disabled by default)
    // ==========================================================================

    /**
     * Seeds large-scale data for SSE/load stress testing.
     * Creates many users across multiple projects so the stress test scripts
     * can authenticate as different users and open concurrent SSE connections.
     *
     * All stress-test users share the same password (configurable).
     * Email pattern: stress{N}@trackdev.com (e.g., stress1@trackdev.com)
     */
    private void seedStressTestData(Workspace workspace, User wsAdmin) {
        var config = trackDevProperties.getStressTest();
        int userCount = config.getUserCount();
        int projectCount = config.getProjectCount();
        String sharedPassword = config.getPassword();

        logger.info("=== STRESS TEST DATA ===");
        logger.info("Creating {} users across {} projects (password: '{}')",
                userCount, projectCount, sharedPassword);

        // 1. Create a dedicated professor for stress test
        User stressProfessor = createUserWithWorkspace(
                "stress-professor",
                "Stress Test Professor",
                "stress.professor@trackdev.com",
                "professor",
                List.of(UserType.PROFESSOR),
                workspace
        );

        // 2. Create a subject and course for stress testing
        Subject stressSubject = createSubjectWithWorkspace(
                "Stress Test Subject", "STRESS", wsAdmin, workspace);
        Course stressCourse = courseService.createCourse(
                stressSubject.getId(), 2025, null, stressProfessor.getId());

        // 3. Create all stress test students
        List<User> stressStudents = new ArrayList<>();
        for (int i = 1; i <= userCount; i++) {
            String username = "stress-user-" + i;
            String fullName = "Stress User " + i;
            String email = "stress" + i + "@trackdev.com";
            User student = createUserWithWorkspace(
                    username, fullName, email,
                    sharedPassword,
                    List.of(UserType.STUDENT),
                    workspace
            );
            stressStudents.add(student);
        }
        logger.info("Created {} stress test students", stressStudents.size());

        // Enroll all students in the stress course
        for (User student : stressStudents) {
            stressCourse.addStudent(student);
        }
        courseService.save(stressCourse);

        // 4. Create sprint pattern with 4 sprints (2 closed, 1 active, 1 future)
        LocalDate today = LocalDate.now();
        LocalDate s3Start = today.minusDays(7);
        LocalDate s3End = s3Start.plusDays(14);
        LocalDate s4Start = s3End;
        LocalDate s4End = s4Start.plusDays(14);
        LocalDate s2Start = s3Start.minusDays(14);
        LocalDate s2End = s3Start;
        LocalDate s1Start = s2Start.minusDays(14);
        LocalDate s1End = s2Start;

        SprintPattern stressPattern = createSprintPattern(
                "Stress Pattern", stressCourse, stressProfessor,
                s1Start, s1End, s2Start, s2End, s3Start, s3End, s4Start, s4End);

        // 5. Distribute students across projects (round-robin)
        int studentsPerProject = (userCount + projectCount - 1) / projectCount;
        for (int p = 0; p < projectCount; p++) {
            int fromIdx = p * studentsPerProject;
            int toIdx = Math.min(fromIdx + studentsPerProject, userCount);
            if (fromIdx >= userCount) break;

            List<User> projectStudents = stressStudents.subList(fromIdx, toIdx);
            String projectName = "stress-project-" + (p + 1);

            createStressTestProject(projectName, projectStudents, stressCourse,
                    stressProfessor, stressPattern);

            logger.info("Created project '{}' with {} students (stress{}-stress{})",
                    projectName, projectStudents.size(), fromIdx + 1, toIdx);
        }

        logger.info("=== STRESS TEST DATA COMPLETE ===");
        logger.info("Login with: stress1@trackdev.com .. stress{}@trackdev.com (password: '{}')",
                userCount, sharedPassword);
    }

    /**
     * Create a stress-test project with sprints and lightweight tasks.
     * Each sprint gets 2 stories with 2 subtasks each — enough to test SSE
     * without making startup too slow.
     */
    private void createStressTestProject(String projectName, List<User> students,
                                          Course course, User professor,
                                          SprintPattern sprintPattern) {
        List<String> studentIds = students.stream().map(User::getId).toList();
        Project project = projectService.createProject(projectName, studentIds, course.getId(), professor.getId());
        project = projectService.applySprintPattern(project.getId(), sprintPattern.getId(), professor.getId());

        List<Sprint> sprints = sprintRepository.findByProjectIdOrderByPatternItemOrderIndex(project.getId());

        for (int i = 0; i < sprints.size(); i++) {
            Sprint sprint = sprints.get(i);
            boolean isFuture = (i == 3);
            boolean isClosed = (i < 2);

            if (!isFuture) {
                activateSprint(sprint, professor.getId());
            }
            if (isFuture) continue;

            // 2 stories per sprint
            for (int s = 0; s < 2; s++) {
                User reporter = students.get(s % students.size());
                String storyName = "Stress story " + (i + 1) + "-" + (s + 1) + " (" + projectName + ")";

                Task story = taskService.createTask(project.getId(), storyName, null, null, null, reporter.getId());

                User assignee = students.get((s + 1) % students.size());
                MergePatchTask storyEdit = new MergePatchTask();
                storyEdit.assignee = Optional.of(assignee.getEmail());
                storyEdit.rank = Optional.of(s + 1);
                if (isClosed) {
                    storyEdit.status = Optional.of(TaskStatus.DONE);
                } else {
                    storyEdit.status = Optional.of(s == 0 ? TaskStatus.INPROGRESS : TaskStatus.TODO);
                }
                taskService.editTaskInternal(story.getId(), storyEdit, assignee.getId());

                // 2 subtasks per story
                for (int t = 0; t < 2; t++) {
                    String subtaskName = "Subtask " + (t + 1) + " of story " + (s + 1);
                    TaskType type = (t == 0) ? TaskType.TASK : TaskType.BUG;
                    Task subtask = taskService.createSubTaskInternal(
                            story.getId(), subtaskName, null, assignee.getId(),
                            sprint.getId(), type, assignee.getId(), isClosed);

                    MergePatchTask subtaskEdit = new MergePatchTask();
                    subtaskEdit.assignee = Optional.of(assignee.getEmail());
                    if (isClosed) {
                        subtaskEdit.status = Optional.of(TaskStatus.DONE);
                    } else {
                        subtaskEdit.status = Optional.of(t == 0 ? TaskStatus.INPROGRESS : TaskStatus.TODO);
                    }
                    taskService.editTaskInternal(subtask.getId(), subtaskEdit, assignee.getId());
                }
            }

            if (isClosed) {
                closeSprint(sprint, professor.getId());
            }
        }
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
     * Create students for a workspace.
     * For the first student (Alice Johnson) in the "udg" workspace, uses STUDENT1_EMAIL env var if set.
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
        
        // Check for custom email for first student (Alice Johnson) in udg workspace
        String student1Email = environment.getProperty("STUDENT1_EMAIL");
        
        List<User> students = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String firstName = firstNames.get(i % firstNames.size());
            String lastName = lastNames.get(i % lastNames.size());
            String fullName = firstName + " " + lastName;
            String username = (firstName + "-" + lastName).toLowerCase();
            
            // Use STUDENT1_EMAIL for Alice Johnson (first student in udg workspace)
            String email;
            if (i == 0 && "udg".equals(prefix) && student1Email != null && !student1Email.isEmpty()) {
                email = student1Email;
                logger.info("Using custom email for Alice Johnson from STUDENT1_EMAIL: {}", email);
            } else {
                email = prefix + ".student" + (i + 1) + "@trackdev.com";
            }
            
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
        List<Sprint> allSprints = sprintRepository.findByProjectIdOrderByPatternItemOrderIndex(project.getId());
        
        int storyIndex = random.nextInt(storyTemplates.size());
        
        // Find Alice Johnson (first student in list for pds25a project)
        User aliceJohnson = students.stream()
            .filter(s -> s.getFullName() != null && s.getFullName().equals("Alice Johnson"))
            .findFirst()
            .orElse(null);
        
        // Debug logging
        if (projectName.equals("pds25a")) {
            logger.info("pds25a: Looking for Alice Johnson in {} students", students.size());
            for (User s : students) {
                logger.info("  - Student: {} ({})", s.getFullName(), s.getEmail());
            }
            logger.info("pds25a: Alice Johnson found: {}", aliceJohnson != null ? aliceJohnson.getEmail() : "NOT FOUND");
        }

        for (int i = 0; i < allSprints.size(); i++) {
            Sprint sprint = allSprints.get(i);
            boolean isActiveSprint = (i == 2);  // Sprint 3 is active (index 2)
            boolean isClosedSprint = (i < 2);   // Sprints 1-2 are closed (index 0, 1)
            boolean isFutureSprint = (i == 3);  // Sprint 4 is in the future (index 3)
            boolean isSprint2ForPds25a = (i == 1) && projectName.equals("pds25a");  // Sprint 2 for pds25a

            // Activate the sprint first (except future sprints)
            if (!isFutureSprint) {
                activateSprint(sprint, professor.getId());
            }

            // Create 4-6 stories per sprint (except future sprint which has no tasks)
            if (isFutureSprint) {
                continue;  // Skip task creation for future sprint
            }
            
            int storiesCount = 4 + random.nextInt(3);
            for (int s = 0; s < storiesCount; s++) {
                User reporter = students.get(random.nextInt(students.size()));
                String storyName = storyTemplates.get(storyIndex % storyTemplates.size());
                storyIndex++;

                Task story = taskService.createTask(project.getId(), storyName, null, null, null, reporter.getId());
                assignOrderedTimestamp(story);

                // For pds25a sprint 2, assign some stories to Alice Johnson
                User assignee;
                if (isSprint2ForPds25a && aliceJohnson != null && s < 2) {
                    assignee = aliceJohnson;  // First 2 stories assigned to Alice
                    logger.info("pds25a Sprint 2: Assigning story '{}' (s={}) to Alice Johnson ({})", storyName, s, aliceJohnson.getEmail());
                } else {
                    assignee = students.get(random.nextInt(students.size()));
                }
                
                MergePatchTask storyEdit = new MergePatchTask();
                storyEdit.assignee = Optional.of(assignee.getEmail());
                // USER_STORY estimation points are calculated from subtasks, don't set manually
                // USER_STORY activeSprints are computed from subtasks, don't set manually
                storyEdit.rank = Optional.of(s + 1);

                if (isClosedSprint && !isSprint2ForPds25a) {
                    storyEdit.status = Optional.of(TaskStatus.DONE);
                } else if (isSprint2ForPds25a && assignee == aliceJohnson) {
                    // Alice's tasks in Sprint 2: mix of DONE, INPROGRESS, and TODO
                    // First story (s=0): DONE, Second story (s=1): INPROGRESS
                    if (s == 0) {
                        storyEdit.status = Optional.of(TaskStatus.DONE);
                    } else {
                        storyEdit.status = Optional.of(TaskStatus.INPROGRESS);
                    }
                } else if (isClosedSprint) {
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

                    // Create subtask with sprint assignment - randomly assign TASK or BUG type
                    TaskType subtaskType = random.nextBoolean() ? TaskType.TASK : TaskType.BUG;
                    Task subtask = taskService.createSubTask(story.getId(), subtaskName, null, assignee.getId(), sprint.getId(), subtaskType, assignee.getId());
                    assignOrderedTimestamp(subtask);

                    User subtaskAssignee;
                    if (isSprint2ForPds25a && assignee == aliceJohnson) {
                        subtaskAssignee = aliceJohnson;  // Alice's subtasks also assigned to her
                    } else {
                        subtaskAssignee = students.get(random.nextInt(students.size()));
                    }
                    
                    // Determine subtask status first
                    TaskStatus subtaskStatus = null;
                    if (isSprint2ForPds25a && assignee == aliceJohnson) {
                        // Alice's subtasks in Sprint 2: match parent story status with some TODO mixed in
                        if (s == 0) {
                            // First story is DONE, so all its subtasks should be DONE
                            subtaskStatus = TaskStatus.DONE;
                        } else {
                            // Second story is INPROGRESS: mix of TODO, INPROGRESS
                            subtaskStatus = (t == 0) ? TaskStatus.TODO : TaskStatus.INPROGRESS;
                        }
                    } else if (isClosedSprint) {
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

            // Close sprints 1-2
            if (isClosedSprint && !(isSprint2ForPds25a)) {
                closeSprint(sprint, professor.getId());
            }
        }

        // Create backlog tasks (USER_STORY type, no estimation points since they're in BACKLOG status)
        int backlogCount = 2 + random.nextInt(3);
        for (int i = 0; i < backlogCount; i++) {
            User reporter = students.get(random.nextInt(students.size()));
            String storyName = storyTemplates.get((storyIndex + i) % storyTemplates.size());

            // Create backlog task - no estimation points needed (calculated from subtasks, and subtasks can't have estimation points in BACKLOG)
            Task backlogTask = taskService.createTask(project.getId(), storyName, null, null, null, reporter.getId());
            assignOrderedTimestamp(backlogTask);
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

    // ==========================================================================
    // HARDCODED PROJECT: pds25a with deterministic data for udg.student1@trackdev.com
    // ==========================================================================

    /**
     * Create pds25a project with hardcoded, deterministic data for all 3 students.
     * Each sprint has 1 story per user, each story with 2 subtasks.
     * - Sprint 1 (closed): 3 stories (Alice, Bob, Carol), all subtasks DONE with estimation points
     * - Sprint 2 (closed): 3 stories (Alice, Bob, Carol), subtasks mix of DONE and VERIFY
     * - Sprint 3 (active): 3 stories (Alice, Bob, Carol), subtasks mix of TODO, INPROGRESS, DONE
     * - Sprint 4 (future): no tasks
     */
    private Project createPds25aProject(List<User> students, Course course, User professor, SprintPattern sprintPattern) {
        // Create project
        List<String> studentIds = students.stream().map(User::getId).toList();
        Project project = projectService.createProject("pds25a", studentIds, course.getId(), professor.getId());

        // Apply sprint pattern to create sprints
        project = projectService.applySprintPattern(project.getId(), sprintPattern.getId(), professor.getId());

        // Get sprints (sorted by order index)
        List<Sprint> allSprints = sprintRepository.findByProjectIdOrderByPatternItemOrderIndex(project.getId());
        Sprint sprint1 = allSprints.get(0);
        Sprint sprint2 = allSprints.get(1);
        Sprint sprint3 = allSprints.get(2);
        // Sprint 4 is future, no tasks needed

        // All 3 students in the project
        User alice = students.get(0); // Alice Johnson
        User bob = students.get(1);   // Bob Smith
        User carol = students.get(2); // Carol Williams
        List<User> allUsers = List.of(alice, bob, carol);

        logger.info("pds25a: Creating data for {} users across 3 sprints", allUsers.size());

        // Activate sprints 1, 2, 3 (not 4 - it's future)
        activateSprint(sprint1, professor.getId());
        activateSprint(sprint2, professor.getId());
        activateSprint(sprint3, professor.getId());

        // Story names: [sprint][user]
        String[][] storyNames = {
            {"User authentication flow", "Database schema design", "API endpoint setup"},
            {"Dashboard overview page", "User profile management", "Search functionality"},
            {"Task board drag and drop", "Sprint navigation feature", "Backlog task management"}
        };

        // Subtask names: [sprint][user][subtask]
        String[][][] subtaskNames = {
            {
                {"Implement login form", "Add password validation"},
                {"Design ER diagram", "Write migration scripts"},
                {"Setup REST controllers", "Add request validation"}
            },
            {
                {"Create dashboard layout", "Add statistics widgets"},
                {"Design profile edit form", "Implement avatar upload"},
                {"Build search index", "Add filter components"}
            },
            {
                {"Implement drag handlers", "Add drop zones"},
                {"Add prev/next arrows", "Fetch project sprints"},
                {"Create backlog panel", "Implement drag to sprint"}
            }
        };

        Sprint[] sprints = {sprint1, sprint2, sprint3};

        // Sprint 1 (closed): All subtasks DONE with estimation points
        for (int u = 0; u < allUsers.size(); u++) {
            createHardcodedStory(project, sprint1, allUsers.get(u), professor,
                storyNames[0][u],
                subtaskNames[0][u],
                new TaskStatus[]{TaskStatus.DONE, TaskStatus.DONE},
                new int[]{3, 5});
        }
        closeSprint(sprint1, professor.getId());

        // Sprint 2 (closed): Mix of DONE and VERIFY with estimation points
        for (int u = 0; u < allUsers.size(); u++) {
            createHardcodedStory(project, sprint2, allUsers.get(u), professor,
                storyNames[1][u],
                subtaskNames[1][u],
                new TaskStatus[]{TaskStatus.DONE, TaskStatus.VERIFY},
                new int[]{5, 8});
        }
        closeSprint(sprint2, professor.getId());

        // Sprint 3 (active): Mix including DONE subtasks for points review testing
        // Alice: 1 DONE + 1 INPROGRESS, Bob: 1 DONE + 1 TODO, Carol: 1 VERIFY + 1 DONE
        createHardcodedStory(project, sprint3, alice, professor,
            storyNames[2][0], subtaskNames[2][0],
            new TaskStatus[]{TaskStatus.DONE, TaskStatus.INPROGRESS},
            new int[]{3, 0});
        createHardcodedStory(project, sprint3, bob, professor,
            storyNames[2][1], subtaskNames[2][1],
            new TaskStatus[]{TaskStatus.DONE, TaskStatus.TODO},
            new int[]{5, 0});
        createHardcodedStory(project, sprint3, carol, professor,
            storyNames[2][2], subtaskNames[2][2],
            new TaskStatus[]{TaskStatus.VERIFY, TaskStatus.DONE},
            new int[]{2, 8});

        // ========== BACKLOG: Add a couple of stories ==========
        Task backlog1 = taskService.createTask(project.getId(), "As a user, I want to export reports to PDF", null, null, null, alice.getId());
        assignOrderedTimestamp(backlog1);
        Task backlog2 = taskService.createTask(project.getId(), "As a user, I want to receive email notifications", null, null, null, alice.getId());
        assignOrderedTimestamp(backlog2);

        logger.info("pds25a: Completed data creation");
        return project;
    }

    /**
     * Helper method to create a hardcoded story with subtasks
     */
    private void createHardcodedStory(Project project, Sprint sprint, User assignee, User professor,
                                       String storyName, String[] subtaskNames,
                                       TaskStatus[] subtaskStatuses, int[] estimationPoints) {
        // Create the USER_STORY
        Task story = taskService.createTask(project.getId(), "As a user, I want to " + storyName.toLowerCase(), null, null, null, assignee.getId());
        assignOrderedTimestamp(story);

        // Set story assignee
        MergePatchTask storyEdit = new MergePatchTask();
        storyEdit.assignee = Optional.of(assignee.getEmail());
        taskService.editTaskInternal(story.getId(), storyEdit, assignee.getId());

        // Create subtasks - use createSubTaskInternal with allowPastSprint=true for demo data
        for (int i = 0; i < subtaskNames.length; i++) {
            TaskType subtaskType = (i % 2 == 0) ? TaskType.TASK : TaskType.BUG; // Alternate TASK/BUG
            Task subtask = taskService.createSubTaskInternal(story.getId(), subtaskNames[i], null, assignee.getId(), sprint.getId(), subtaskType, assignee.getId(), true);
            assignOrderedTimestamp(subtask);

            // Set subtask status
            MergePatchTask subtaskEdit = new MergePatchTask();
            subtaskEdit.assignee = Optional.of(assignee.getEmail());
            subtaskEdit.status = Optional.of(subtaskStatuses[i]);
            taskService.editTaskInternal(subtask.getId(), subtaskEdit, assignee.getId());

            // Set estimation points if > 0 (only for VERIFY or DONE)
            if (estimationPoints[i] > 0) {
                MergePatchTask estimationEdit = new MergePatchTask();
                estimationEdit.estimationPoints = Optional.of(estimationPoints[i]);
                taskService.editTaskInternal(subtask.getId(), estimationEdit, assignee.getId());
            }
        }
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
     * Create a pull request record linked to a done task for testing.
     * Uses the task at the specified index from DONE tasks found for the given user in the project.
     * Fetches real PR data from GitHub API.
     * @param taskIndex The index of the DONE task to link the PR to (0-based)
     */
    private void createHardcodedPullRequest(Project project, User user, String repoUrl, int prNumber, int taskIndex) {
        // Find DONE tasks assigned to user in this project
        List<Task> doneTasks = taskService.findByProjectIdAndStatusAndAssignee(
            project.getId(), TaskStatus.DONE, user.getId());
        
        if (doneTasks.isEmpty()) {
            logger.warn("No DONE tasks found for {} in project {} - skipping PR {} creation", 
                user.getEmail(), project.getName(), prNumber);
            return;
        }
        
        if (taskIndex >= doneTasks.size()) {
            logger.warn("Not enough DONE tasks for {} in project {} (need index {}, have {}) - skipping PR {} creation", 
                user.getEmail(), project.getName(), taskIndex, doneTasks.size(), prNumber);
            return;
        }
        
        // Get the task ID for linking
        Long taskId = doneTasks.get(taskIndex).getId();
        
        // Extract owner/repo from URL (e.g., https://github.com/owner/repo)
        String repoFullName = repoUrl.replace("https://github.com/", "")
                                     .replace(".git", "");
        
        // Create the PR URL
        String prUrl = "https://github.com/" + repoFullName + "/pull/" + prNumber;
        
        // Create a unique node ID for the PR (max 32 chars for DB column)
        String nodeId = "PR_demo_" + prNumber;
        
        // Check if PR already exists
        if (pullRequestRepository.findByNodeId(nodeId).isPresent()) {
            logger.info("PR {} already exists - skipping", prUrl);
            return;
        }
        
        // Create a minimal PullRequest entity with just the essential info
        PullRequest pr = new PullRequest(prUrl, nodeId);
        pr.setAuthor(user);
        pr.setPrNumber(prNumber);
        pr.setRepoFullName(repoFullName);
        pullRequestRepository.save(pr);
        
        // Fetch real PR data from GitHub API
        pullRequestService.fetchAndUpdatePRStats(pr);
        
        // Link PR to task using the transactional service method
        taskService.linkPullRequestToTask(taskId, pr);
        
        logger.info("Created PR {} (merged: {}) linked to task ID: {}", prUrl, pr.getMerged(), taskId);
    }

    /**
     * Create a project with sprints but no tasks (empty sprints)
     */
    private Project createProjectWithSprintsOnly(String projectName, List<User> students,
                                                  Course course, User professor,
                                                  SprintPattern sprintPattern) {
        List<String> studentIds = students.stream().map(User::getId).toList();
        Project project = projectService.createProject(projectName, studentIds, course.getId(), professor.getId());
        project = projectService.applySprintPattern(project.getId(), sprintPattern.getId(), professor.getId());
        
        // Activate all sprints but don't create any tasks
        List<Sprint> allSprints = sprintRepository.findByProjectIdOrderByPatternItemOrderIndex(project.getId());
        for (int i = 0; i < allSprints.size(); i++) {
            Sprint sprint = allSprints.get(i);
            activateSprint(sprint, professor.getId());
            // Close past sprints
            if (i < 3) {
                closeSprint(sprint, professor.getId());
            }
        }
        return project;
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

    /**
     * Create a demo profile with enums and attributes for all types and targets
     */
    private Profile createDemoProfile(User professor) {
        Profile profile = new Profile(
            "Software Development Profile",
            "A comprehensive profile for tracking software development metrics and student performance",
            professor
        );
        profile = profileRepository.save(profile);

        // Create enums
        ProfileEnum skillLevelEnum = new ProfileEnum("Skill Level", profile);
        skillLevelEnum.setValues(Arrays.asList(
            new EnumValueEntry("Beginner", "Little or no prior experience with the technology"),
            new EnumValueEntry("Intermediate", "Can work independently on standard tasks"),
            new EnumValueEntry("Advanced", "Deep understanding, can solve complex problems"),
            new EnumValueEntry("Expert", "Mastery level, can mentor others and design architectures")
        ));
        profile.addEnum(skillLevelEnum);

        ProfileEnum priorityEnum = new ProfileEnum("Priority", profile);
        priorityEnum.setValues(Arrays.asList(
            new EnumValueEntry("Low", "Nice to have, can be deferred to a future sprint"),
            new EnumValueEntry("Medium", "Should be completed within the current iteration"),
            new EnumValueEntry("High", "Important for the sprint goal, prioritize accordingly"),
            new EnumValueEntry("Critical", "Blocking other work, must be resolved immediately")
        ));
        profile.addEnum(priorityEnum);

        ProfileEnum reviewStatusEnum = new ProfileEnum("Review Status", profile);
        reviewStatusEnum.setValues(Arrays.asList(
            new EnumValueEntry("Pending", "Awaiting initial review from a reviewer"),
            new EnumValueEntry("InReview", "Currently being reviewed by a team member"),
            new EnumValueEntry("Approved", "Review passed, ready to merge"),
            new EnumValueEntry("Rejected", "Does not meet requirements, needs significant rework"),
            new EnumValueEntry("NeedsChanges", "Minor issues found, requires small fixes before approval")
        ));
        profile.addEnum(reviewStatusEnum);

        ProfileEnum listGradeEnum = new ProfileEnum("Grade", profile);
        listGradeEnum.setValues(Arrays.asList(
            new EnumValueEntry("A", "Excellent performance"),
            new EnumValueEntry("B", "Good performance"),
            new EnumValueEntry("C", "Satisfactory performance"),
            new EnumValueEntry("D", "Below average performance"),
            new EnumValueEntry("E", "Needs significant improvement")
        ));
        profile.addEnum(listGradeEnum);

        // Save to persist the enums
        profile = profileRepository.save(profile);
        
        // Get the saved enum references from the profile
        ProfileEnum savedSkillLevelEnum = profile.getEnums().stream()
            .filter(e -> e.getName().equals("Skill Level")).findFirst().orElseThrow();
        ProfileEnum savedPriorityEnum = profile.getEnums().stream()
            .filter(e -> e.getName().equals("Priority")).findFirst().orElseThrow();
        ProfileEnum savedReviewStatusEnum = profile.getEnums().stream()
            .filter(e -> e.getName().equals("Review Status")).findFirst().orElseThrow();
        ProfileEnum savedListGradeEnum = profile.getEnums().stream()
            .filter(e -> e.getName().equals("Grade")).findFirst().orElseThrow();

        // Create attributes for STUDENT target (one per type)
        ProfileAttribute studentNotes = new ProfileAttribute("Notes", AttributeType.STRING, AttributeTarget.STUDENT, profile);
        studentNotes.setVisibility(AttributeVisibility.PROFESSOR_ONLY);
        profile.addAttribute(studentNotes);

        ProfileAttribute studentSkill = new ProfileAttribute("Technical Skill", AttributeType.ENUM, AttributeTarget.STUDENT, profile);
        studentSkill.setEnumRef(savedSkillLevelEnum);
        studentSkill.setVisibility(AttributeVisibility.ASSIGNED_STUDENT);
        profile.addAttribute(studentSkill);

        ProfileAttribute studentAttendance = new ProfileAttribute("Attendance Count", AttributeType.INTEGER, AttributeTarget.STUDENT, profile);
        studentAttendance.setVisibility(AttributeVisibility.PROFESSOR_ONLY);
        profile.addAttribute(studentAttendance);

        ProfileAttribute studentGrade = new ProfileAttribute("Participation Grade", AttributeType.FLOAT, AttributeTarget.STUDENT, profile);
        studentGrade.setVisibility(AttributeVisibility.PROJECT_STUDENTS);
        profile.addAttribute(studentGrade);

        ProfileAttribute studentEvaluations = new ProfileAttribute("Sprint Evaluations", AttributeType.LIST, AttributeTarget.STUDENT, profile);
        studentEvaluations.setEnumRef(savedListGradeEnum);
        studentEvaluations.setVisibility(AttributeVisibility.PROFESSOR_ONLY);
        studentEvaluations.setAppliedBy(AttributeAppliedBy.PROFESSOR);
        profile.addAttribute(studentEvaluations);

        // Create attributes for TASK target (one per type)
        ProfileAttribute taskDescription = new ProfileAttribute("Technical Notes", AttributeType.STRING, AttributeTarget.TASK, profile);
        taskDescription.setVisibility(AttributeVisibility.PROJECT_STUDENTS);
        taskDescription.setAppliedBy(AttributeAppliedBy.STUDENT);
        profile.addAttribute(taskDescription);

        ProfileAttribute taskPriority = new ProfileAttribute("Business Priority", AttributeType.ENUM, AttributeTarget.TASK, profile);
        taskPriority.setEnumRef(savedPriorityEnum);
        taskPriority.setVisibility(AttributeVisibility.PROJECT_STUDENTS);
        taskPriority.setAppliedBy(AttributeAppliedBy.STUDENT);
        profile.addAttribute(taskPriority);

        ProfileAttribute taskComplexity = new ProfileAttribute("Complexity Score", AttributeType.INTEGER, AttributeTarget.TASK, profile);
        taskComplexity.setVisibility(AttributeVisibility.PROFESSOR_ONLY);
        profile.addAttribute(taskComplexity);

        ProfileAttribute taskCodeCoverage = new ProfileAttribute("Code Coverage", AttributeType.FLOAT, AttributeTarget.TASK, profile);
        taskCodeCoverage.setVisibility(AttributeVisibility.ASSIGNED_STUDENT);
        taskCodeCoverage.setAppliedBy(AttributeAppliedBy.STUDENT);
        profile.addAttribute(taskCodeCoverage);

        // Create attributes for PULL_REQUEST target (one per type)
        ProfileAttribute prReviewNotes = new ProfileAttribute("Review Notes", AttributeType.STRING, AttributeTarget.PULL_REQUEST, profile);
        prReviewNotes.setVisibility(AttributeVisibility.PROFESSOR_ONLY);
        profile.addAttribute(prReviewNotes);

        ProfileAttribute prStatus = new ProfileAttribute("Review Outcome", AttributeType.ENUM, AttributeTarget.PULL_REQUEST, profile);
        prStatus.setEnumRef(savedReviewStatusEnum);
        prStatus.setVisibility(AttributeVisibility.PROJECT_STUDENTS);
        profile.addAttribute(prStatus);

        ProfileAttribute prChangesRequested = new ProfileAttribute("Changes Requested", AttributeType.INTEGER, AttributeTarget.PULL_REQUEST, profile);
        prChangesRequested.setVisibility(AttributeVisibility.ASSIGNED_STUDENT);
        profile.addAttribute(prChangesRequested);

        ProfileAttribute prQualityScore = new ProfileAttribute("Quality Score", AttributeType.FLOAT, AttributeTarget.PULL_REQUEST, profile);
        profile.addAttribute(prQualityScore);

        return profileRepository.save(profile);
    }

    // ==========================================================================
    // PERMISSION TEST DATA
    // ==========================================================================

    /**
     * Create specific test data for API permission enforcement tests.
     * These tasks have predictable IDs and states for Postman tests.
     */
    private void createPermissionTestData(Project project, Sprint pastSprint, Sprint activeSprint, 
                                           Sprint futureSprint, User alice, User bob, User professor) {
        logger.info("");
        logger.info("=== PERMISSION TEST DATA ===");
        logger.info("Use these IDs in Postman collection variables:");
        logger.info("");

        // 1. FROZEN TASK - A task that is frozen (only professor can edit)
        Task frozenTask = taskService.createTask(project.getId(), "PERMISSION TEST: Frozen task", null, null, null, alice.getId());
        assignOrderedTimestamp(frozenTask);
        MergePatchTask frozenEdit = new MergePatchTask();
        frozenEdit.assignee = Optional.of(alice.getEmail());
        taskService.editTaskInternal(frozenTask.getId(), frozenEdit, alice.getId());
        taskService.freezeTask(frozenTask.getId(), professor.getId());
        logger.info("frozenTaskId = {}", frozenTask.getId());

        // 2. PAST SPRINT TASK - A task in a closed/past sprint (student cannot edit status)
        // Create a USER_STORY in backlog, then add a subtask to the past sprint
        Task pastStory = taskService.createTask(project.getId(), "PERMISSION TEST: Past sprint story", null, null, null, alice.getId());
        assignOrderedTimestamp(pastStory);
        MergePatchTask pastStoryEdit = new MergePatchTask();
        pastStoryEdit.assignee = Optional.of(alice.getEmail());
        taskService.editTaskInternal(pastStory.getId(), pastStoryEdit, alice.getId());

        // Create subtask in past sprint using internal method that allows past sprint
        Task pastSprintTask = taskService.createSubTaskInternal(pastStory.getId(),
            "PERMISSION TEST: Task in past sprint", null, alice.getId(), pastSprint.getId(), TaskType.TASK, alice.getId(), true);
        assignOrderedTimestamp(pastSprintTask);
        MergePatchTask pastTaskEdit = new MergePatchTask();
        pastTaskEdit.assignee = Optional.of(alice.getEmail());
        pastTaskEdit.status = Optional.of(TaskStatus.DONE);
        taskService.editTaskInternal(pastSprintTask.getId(), pastTaskEdit, alice.getId());
        logger.info("pastSprintTaskId = {}", pastSprintTask.getId());

        // 3. TASK REPORTED BY ALICE BUT ASSIGNED TO BOB (for delete permission test)
        // Reporter is Alice, Assignee is Bob - Alice should NOT be able to delete
        Task reportedNotAssigned = taskService.createTask(project.getId(),
            "PERMISSION TEST: Reported by Alice, assigned to Bob", null, null, null, alice.getId());
        assignOrderedTimestamp(reportedNotAssigned);
        MergePatchTask reportedEdit = new MergePatchTask();
        reportedEdit.assignee = Optional.of(bob.getEmail());
        taskService.editTaskInternal(reportedNotAssigned.getId(), reportedEdit, professor.getId());
        logger.info("taskReportedNotAssignedId = {} (reporter: Alice, assignee: Bob)", reportedNotAssigned.getId());

        // 4. TASK ASSIGNED TO ALICE FOR DELETE TEST
        Task taskToDelete = taskService.createTask(project.getId(),
            "PERMISSION TEST: Task assigned to Alice for delete", null, null, null, alice.getId());
        assignOrderedTimestamp(taskToDelete);
        MergePatchTask deleteEdit = new MergePatchTask();
        deleteEdit.assignee = Optional.of(alice.getEmail());
        taskService.editTaskInternal(taskToDelete.getId(), deleteEdit, alice.getId());
        logger.info("taskAssignedToDeleteId = {}", taskToDelete.getId());

        // 5. USER_STORY (for testing that status cannot be changed directly)
        Task userStory = taskService.createTask(project.getId(),
            "PERMISSION TEST: User story (status computed from subtasks)", null, null, null, alice.getId());
        assignOrderedTimestamp(userStory);
        MergePatchTask storyEdit = new MergePatchTask();
        storyEdit.assignee = Optional.of(alice.getEmail());
        taskService.editTaskInternal(userStory.getId(), storyEdit, alice.getId());
        logger.info("userStoryTaskId = {}", userStory.getId());

        // 6. USER_STORY WITH SUBTASKS (for testing cannot delete if has subtasks)
        Task userStoryWithSubtasks = taskService.createTask(project.getId(),
            "PERMISSION TEST: User story with subtasks (cannot delete)", null, null, null, alice.getId());
        assignOrderedTimestamp(userStoryWithSubtasks);
        MergePatchTask storyWithSubtasksEdit = new MergePatchTask();
        storyWithSubtasksEdit.assignee = Optional.of(alice.getEmail());
        taskService.editTaskInternal(userStoryWithSubtasks.getId(), storyWithSubtasksEdit, alice.getId());

        // Add a subtask to make it non-deletable
        Task subtask1 = taskService.createSubTask(userStoryWithSubtasks.getId(),
            "PERMISSION TEST: Subtask 1", null, alice.getId(), activeSprint.getId(), TaskType.TASK, alice.getId());
        assignOrderedTimestamp(subtask1);
        MergePatchTask subtaskEdit = new MergePatchTask();
        subtaskEdit.assignee = Optional.of(alice.getEmail());
        taskService.editTaskInternal(subtask1.getId(), subtaskEdit, alice.getId());
        logger.info("userStoryWithSubtasksId = {}", userStoryWithSubtasks.getId());

        // 7. TASK IN FUTURE SPRINT (cannot change status from TODO)
        Task futureTask = taskService.createTask(project.getId(),
            "PERMISSION TEST: Task in future sprint", null, null, null, alice.getId());
        assignOrderedTimestamp(futureTask);
        MergePatchTask futureEdit = new MergePatchTask();
        futureEdit.assignee = Optional.of(alice.getEmail());
        futureEdit.activeSprints = Optional.of(List.of(futureSprint.getId()));
        taskService.editTaskInternal(futureTask.getId(), futureEdit, alice.getId());
        logger.info("futureSprintTaskId = {}", futureTask.getId());

        // 8. TASK FOR UNASSIGNMENT TEST
        // First create a task assigned to Alice, then we'll unassign it via test
        Task unassignmentTask = taskService.createTask(project.getId(),
            "PERMISSION TEST: Task for unassignment test", null, null, null, alice.getId());
        assignOrderedTimestamp(unassignmentTask);
        MergePatchTask unassignEdit = new MergePatchTask();
        unassignEdit.assignee = Optional.of(alice.getEmail());
        taskService.editTaskInternal(unassignmentTask.getId(), unassignEdit, alice.getId());
        logger.info("unassignedTaskId = {} (initially assigned to Alice)", unassignmentTask.getId());

        logger.info("");
        logger.info("=== USER TOKENS ===");
        String student1Email = environment.getProperty("STUDENT1_EMAIL");
        if (student1Email != null && !student1Email.isEmpty()) {
            logger.info("udgStudentEmail = {} (Alice Johnson - password: student1) [from STUDENT1_EMAIL]", student1Email);
        } else {
            logger.info("udgStudentEmail = udg.student1@trackdev.com (Alice - password: student1)");
        }
        logger.info("ubStudentEmail = ub.student1@trackdev.com (UB student - password: student1)");
        logger.info("professorUdGEmail = maria.garcia@trackdev.com (password: professor)");
        logger.info("");
        logger.info("=== END PERMISSION TEST DATA ===");
        logger.info("");
    }

    /**
     * Assign a distinct, ascending createdAt timestamp to a task and persist it.
     * Call this right after creating a task (via createTask or createSubTask).
     */
    private void assignOrderedTimestamp(Task task) {
        task.setCreatedAt(TASK_BASE_TIME.plusMinutes(taskCreationOrder));
        taskCreationOrder++;
        taskService.save(task);
    }
}
