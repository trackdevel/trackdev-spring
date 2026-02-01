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
    private PullRequestRepository pullRequestRepository;

    @Autowired
    private PullRequestService pullRequestService;

    @Autowired
    private org.trackdev.api.repository.ProfileRepository profileRepository;

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

        // Add a hardcoded PR to a done task for Alice Johnson in pds25a
        if (environment.getProperty("GITHUB_REPO1_URL") != null) {
            createHardcodedPullRequest(
                projectPDS,
                studentsUdG.get(0), // Alice Johnson
                environment.getProperty("GITHUB_REPO1_URL"),
                8 // PR number 8
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
        logger.info("Created demo profile '{}' for professor {}", demoProfile.getName(), professorPDS.getEmail());

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

                Task story = taskService.createTask(project.getId(), storyName, reporter.getId());

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
                    Task subtask = taskService.createSubTask(story.getId(), subtaskName, assignee.getId(), sprint.getId(), subtaskType);

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

    // ==========================================================================
    // HARDCODED PROJECT: pds25a with deterministic data for udg.student1@trackdev.com
    // ==========================================================================

    /**
     * Create pds25a project with hardcoded, deterministic data for Alice Johnson (udg.student1@trackdev.com).
     * This shows all possible scenarios in a project:
     * - Sprint 1 (closed): 1 story with 2 subtasks in DONE
     * - Sprint 2 (closed): 1 story with 2 subtasks in DONE + 1 story with 2 subtasks in DONE and 1 in INPROGRESS
     * - Sprint 3 (active): 3 stories, each with 3 subtasks (1 INPROGRESS, 1 VERIFY, 1 DONE)
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

        // Get Alice Johnson (first student in list - may have custom email from STUDENT1_EMAIL)
        User alice = students.stream()
            .filter(s -> s.getFullName() != null && s.getFullName().equals("Alice Johnson"))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Alice Johnson not found in students list"));

        logger.info("pds25a: Creating hardcoded data for Alice Johnson ({})", alice.getEmail());

        // Activate sprints 1, 2, 3 (not 4 - it's future)
        activateSprint(sprint1, professor.getId());
        activateSprint(sprint2, professor.getId());
        activateSprint(sprint3, professor.getId());

        // ========== SPRINT 1: 1 story with 2 subtasks in DONE ==========
        createHardcodedStory(project, sprint1, alice, professor, "User authentication flow",
            new String[]{"Implement login form", "Add password validation"},
            new TaskStatus[]{TaskStatus.DONE, TaskStatus.DONE},
            new int[]{3, 2});

        // Close Sprint 1
        closeSprint(sprint1, professor.getId());

        // ========== SPRINT 2: 2 stories ==========
        // Story 1: 2 subtasks in DONE
        createHardcodedStory(project, sprint2, alice, professor, "Dashboard overview page",
            new String[]{"Create dashboard layout", "Add statistics widgets"},
            new TaskStatus[]{TaskStatus.DONE, TaskStatus.DONE},
            new int[]{5, 3});

        // Story 2: 2 subtasks in DONE + 1 in INPROGRESS
        createHardcodedStory(project, sprint2, alice, professor, "User profile management",
            new String[]{"Design profile edit form", "Implement avatar upload", "Add email change flow"},
            new TaskStatus[]{TaskStatus.DONE, TaskStatus.DONE, TaskStatus.INPROGRESS},
            new int[]{2, 3, 0}); // INPROGRESS has no estimation points

        // Note: Sprint 2 is NOT closed because it has an INPROGRESS task

        // ========== SPRINT 3 (active): 3 stories, each with 3 subtasks (1 INPROGRESS, 1 VERIFY, 1 DONE) ==========
        createHardcodedStory(project, sprint3, alice, professor, "Task board drag and drop",
            new String[]{"Implement drag handlers", "Add drop zones", "Update task status on drop"},
            new TaskStatus[]{TaskStatus.INPROGRESS, TaskStatus.VERIFY, TaskStatus.DONE},
            new int[]{0, 5, 3}); // INPROGRESS has no estimation points

        createHardcodedStory(project, sprint3, alice, professor, "Sprint navigation feature",
            new String[]{"Add prev/next arrows", "Fetch project sprints", "Compute navigation links"},
            new TaskStatus[]{TaskStatus.INPROGRESS, TaskStatus.VERIFY, TaskStatus.DONE},
            new int[]{0, 2, 5});

        createHardcodedStory(project, sprint3, alice, professor, "Backlog task management",
            new String[]{"Create backlog panel", "Implement drag to sprint", "Add validation rules"},
            new TaskStatus[]{TaskStatus.INPROGRESS, TaskStatus.VERIFY, TaskStatus.DONE},
            new int[]{0, 3, 2});

        // ========== BACKLOG: Add a couple of stories ==========
        taskService.createTask(project.getId(), "As a user, I want to export reports to PDF", alice.getId());
        taskService.createTask(project.getId(), "As a user, I want to receive email notifications", alice.getId());

        logger.info("pds25a: Completed hardcoded data creation");
        return project;
    }

    /**
     * Helper method to create a hardcoded story with subtasks
     */
    private void createHardcodedStory(Project project, Sprint sprint, User assignee, User professor,
                                       String storyName, String[] subtaskNames,
                                       TaskStatus[] subtaskStatuses, int[] estimationPoints) {
        // Create the USER_STORY
        Task story = taskService.createTask(project.getId(), "As a user, I want to " + storyName.toLowerCase(), assignee.getId());

        // Set story assignee
        MergePatchTask storyEdit = new MergePatchTask();
        storyEdit.assignee = Optional.of(assignee.getEmail());
        taskService.editTaskInternal(story.getId(), storyEdit, assignee.getId());

        // Create subtasks - use createSubTaskInternal with allowPastSprint=true for demo data
        for (int i = 0; i < subtaskNames.length; i++) {
            TaskType subtaskType = (i % 2 == 0) ? TaskType.TASK : TaskType.BUG; // Alternate TASK/BUG
            Task subtask = taskService.createSubTaskInternal(story.getId(), subtaskNames[i], assignee.getId(), sprint.getId(), subtaskType, true);

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
     * Uses the first DONE task found for the given user in the project.
     * Fetches real PR data from GitHub API.
     */
    private void createHardcodedPullRequest(Project project, User alice, String repoUrl, int prNumber) {
        // Find a DONE task assigned to Alice in this project
        List<Task> doneTasks = taskService.findByProjectIdAndStatusAndAssignee(
            project.getId(), TaskStatus.DONE, alice.getId());
        
        if (doneTasks.isEmpty()) {
            logger.warn("No DONE tasks found for {} in project {} - skipping PR creation", 
                alice.getEmail(), project.getName());
            return;
        }
        
        // Get the task ID for linking
        Long taskId = doneTasks.get(0).getId();
        
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
        pr.setAuthor(alice);
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
        skillLevelEnum.setValues(Arrays.asList("Beginner", "Intermediate", "Advanced", "Expert"));
        profile.addEnum(skillLevelEnum);

        ProfileEnum priorityEnum = new ProfileEnum("Priority", profile);
        priorityEnum.setValues(Arrays.asList("Low", "Medium", "High", "Critical"));
        profile.addEnum(priorityEnum);

        ProfileEnum reviewStatusEnum = new ProfileEnum("Review Status", profile);
        reviewStatusEnum.setValues(Arrays.asList("Pending", "In Review", "Approved", "Rejected", "Needs Changes"));
        profile.addEnum(reviewStatusEnum);

        // Save to persist the enums
        profile = profileRepository.save(profile);
        
        // Get the saved enum references from the profile
        ProfileEnum savedSkillLevelEnum = profile.getEnums().stream()
            .filter(e -> e.getName().equals("Skill Level")).findFirst().orElseThrow();
        ProfileEnum savedPriorityEnum = profile.getEnums().stream()
            .filter(e -> e.getName().equals("Priority")).findFirst().orElseThrow();
        ProfileEnum savedReviewStatusEnum = profile.getEnums().stream()
            .filter(e -> e.getName().equals("Review Status")).findFirst().orElseThrow();

        // Create attributes for STUDENT target (one per type)
        ProfileAttribute studentNotes = new ProfileAttribute("Notes", AttributeType.STRING, AttributeTarget.STUDENT, profile);
        profile.addAttribute(studentNotes);

        ProfileAttribute studentSkill = new ProfileAttribute("Technical Skill", AttributeType.ENUM, AttributeTarget.STUDENT, profile);
        studentSkill.setEnumRef(savedSkillLevelEnum);
        profile.addAttribute(studentSkill);

        ProfileAttribute studentAttendance = new ProfileAttribute("Attendance Count", AttributeType.INTEGER, AttributeTarget.STUDENT, profile);
        profile.addAttribute(studentAttendance);

        ProfileAttribute studentGrade = new ProfileAttribute("Participation Grade", AttributeType.FLOAT, AttributeTarget.STUDENT, profile);
        profile.addAttribute(studentGrade);

        // Create attributes for TASK target (one per type)
        ProfileAttribute taskDescription = new ProfileAttribute("Technical Notes", AttributeType.STRING, AttributeTarget.TASK, profile);
        profile.addAttribute(taskDescription);

        ProfileAttribute taskPriority = new ProfileAttribute("Business Priority", AttributeType.ENUM, AttributeTarget.TASK, profile);
        taskPriority.setEnumRef(savedPriorityEnum);
        profile.addAttribute(taskPriority);

        ProfileAttribute taskComplexity = new ProfileAttribute("Complexity Score", AttributeType.INTEGER, AttributeTarget.TASK, profile);
        profile.addAttribute(taskComplexity);

        ProfileAttribute taskCodeCoverage = new ProfileAttribute("Code Coverage", AttributeType.FLOAT, AttributeTarget.TASK, profile);
        profile.addAttribute(taskCodeCoverage);

        // Create attributes for PULL_REQUEST target (one per type)
        ProfileAttribute prReviewNotes = new ProfileAttribute("Review Notes", AttributeType.STRING, AttributeTarget.PULL_REQUEST, profile);
        profile.addAttribute(prReviewNotes);

        ProfileAttribute prStatus = new ProfileAttribute("Review Outcome", AttributeType.ENUM, AttributeTarget.PULL_REQUEST, profile);
        prStatus.setEnumRef(savedReviewStatusEnum);
        profile.addAttribute(prStatus);

        ProfileAttribute prChangesRequested = new ProfileAttribute("Changes Requested", AttributeType.INTEGER, AttributeTarget.PULL_REQUEST, profile);
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
        Task frozenTask = taskService.createTask(project.getId(), "PERMISSION TEST: Frozen task", alice.getId());
        MergePatchTask frozenEdit = new MergePatchTask();
        frozenEdit.assignee = Optional.of(alice.getEmail());
        taskService.editTaskInternal(frozenTask.getId(), frozenEdit, alice.getId());
        taskService.freezeTask(frozenTask.getId(), professor.getId());
        logger.info("frozenTaskId = {}", frozenTask.getId());

        // 2. PAST SPRINT TASK - A task in a closed/past sprint (student cannot edit status)
        // Create a USER_STORY in backlog, then add a subtask to the past sprint
        Task pastStory = taskService.createTask(project.getId(), "PERMISSION TEST: Past sprint story", alice.getId());
        MergePatchTask pastStoryEdit = new MergePatchTask();
        pastStoryEdit.assignee = Optional.of(alice.getEmail());
        taskService.editTaskInternal(pastStory.getId(), pastStoryEdit, alice.getId());
        
        // Create subtask in past sprint using internal method that allows past sprint
        Task pastSprintTask = taskService.createSubTaskInternal(pastStory.getId(), 
            "PERMISSION TEST: Task in past sprint", alice.getId(), pastSprint.getId(), TaskType.TASK, true);
        MergePatchTask pastTaskEdit = new MergePatchTask();
        pastTaskEdit.assignee = Optional.of(alice.getEmail());
        pastTaskEdit.status = Optional.of(TaskStatus.DONE);
        taskService.editTaskInternal(pastSprintTask.getId(), pastTaskEdit, alice.getId());
        logger.info("pastSprintTaskId = {}", pastSprintTask.getId());

        // 3. TASK REPORTED BY ALICE BUT ASSIGNED TO BOB (for delete permission test)
        // Reporter is Alice, Assignee is Bob - Alice should NOT be able to delete
        Task reportedNotAssigned = taskService.createTask(project.getId(), 
            "PERMISSION TEST: Reported by Alice, assigned to Bob", alice.getId());
        MergePatchTask reportedEdit = new MergePatchTask();
        reportedEdit.assignee = Optional.of(bob.getEmail());
        taskService.editTaskInternal(reportedNotAssigned.getId(), reportedEdit, professor.getId());
        logger.info("taskReportedNotAssignedId = {} (reporter: Alice, assignee: Bob)", reportedNotAssigned.getId());

        // 4. TASK ASSIGNED TO ALICE FOR DELETE TEST
        Task taskToDelete = taskService.createTask(project.getId(), 
            "PERMISSION TEST: Task assigned to Alice for delete", alice.getId());
        MergePatchTask deleteEdit = new MergePatchTask();
        deleteEdit.assignee = Optional.of(alice.getEmail());
        taskService.editTaskInternal(taskToDelete.getId(), deleteEdit, alice.getId());
        logger.info("taskAssignedToDeleteId = {}", taskToDelete.getId());

        // 5. USER_STORY (for testing that status cannot be changed directly)
        Task userStory = taskService.createTask(project.getId(), 
            "PERMISSION TEST: User story (status computed from subtasks)", alice.getId());
        MergePatchTask storyEdit = new MergePatchTask();
        storyEdit.assignee = Optional.of(alice.getEmail());
        taskService.editTaskInternal(userStory.getId(), storyEdit, alice.getId());
        logger.info("userStoryTaskId = {}", userStory.getId());

        // 6. USER_STORY WITH SUBTASKS (for testing cannot delete if has subtasks)
        Task userStoryWithSubtasks = taskService.createTask(project.getId(), 
            "PERMISSION TEST: User story with subtasks (cannot delete)", alice.getId());
        MergePatchTask storyWithSubtasksEdit = new MergePatchTask();
        storyWithSubtasksEdit.assignee = Optional.of(alice.getEmail());
        taskService.editTaskInternal(userStoryWithSubtasks.getId(), storyWithSubtasksEdit, alice.getId());
        
        // Add a subtask to make it non-deletable
        Task subtask1 = taskService.createSubTask(userStoryWithSubtasks.getId(), 
            "PERMISSION TEST: Subtask 1", alice.getId(), activeSprint.getId(), TaskType.TASK);
        MergePatchTask subtaskEdit = new MergePatchTask();
        subtaskEdit.assignee = Optional.of(alice.getEmail());
        taskService.editTaskInternal(subtask1.getId(), subtaskEdit, alice.getId());
        logger.info("userStoryWithSubtasksId = {}", userStoryWithSubtasks.getId());

        // 7. TASK IN FUTURE SPRINT (cannot change status from TODO)
        Task futureTask = taskService.createTask(project.getId(), 
            "PERMISSION TEST: Task in future sprint", alice.getId());
        MergePatchTask futureEdit = new MergePatchTask();
        futureEdit.assignee = Optional.of(alice.getEmail());
        futureEdit.activeSprints = Optional.of(List.of(futureSprint.getId()));
        taskService.editTaskInternal(futureTask.getId(), futureEdit, alice.getId());
        logger.info("futureSprintTaskId = {}", futureTask.getId());

        // 8. TASK FOR UNASSIGNMENT TEST
        // First create a task assigned to Alice, then we'll unassign it via test
        Task unassignmentTask = taskService.createTask(project.getId(), 
            "PERMISSION TEST: Task for unassignment test", alice.getId());
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
}
