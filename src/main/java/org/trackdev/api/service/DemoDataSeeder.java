package org.trackdev.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
 * Creates a minimal but complete dataset covering all entity types and states.
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
    private SprintPatternService sprintPatternService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private CommentService commentService;

    public void seedDemoData() {
        logger.info("Starting database seeding...");

        // ============================================
        // 1. CREATE USERS
        // ============================================
        
        // Admin/Professor user
        User admin = userService.addUserInternal(
            "TrackDev Admin", 
            "admin@trackdev.com", 
            global.getPasswordEncoder().encode("admin"), 
            List.of(UserType.ADMIN, UserType.PROFESSOR)
        );
        
        // Professor user
        User professor = userService.addUserInternal(
            "Professor Demo", 
            "professor@trackdev.com", 
            global.getPasswordEncoder().encode("professor"), 
            List.of(UserType.PROFESSOR)
        );

        // 6 Test students
        List<User> students = createTestStudents();

        // ============================================
        // 2. CREATE SUBJECTS (admin creates, professor owns)
        // ============================================
        
        Subject subjectPDS = subjectService.createSubject(
            "Projecte de Desenvolupament de Software", 
            "PDS", 
            admin.getId()
        );
        
        Subject subjectTFG = subjectService.createSubject(
            "Treball Final de Grau", 
            "TFG", 
            admin.getId()
        );

        // ============================================
        // 3. CREATE COURSES (Year 2025)
        // ============================================
        
        Course coursePDS = courseService.createCourse(
            subjectPDS.getId(), 
            2025, 
            null, 
            professor.getId()
        );
        
        Course courseTFG = courseService.createCourse(
            subjectTFG.getId(), 
            2025, 
            null, 
            professor.getId()
        );

        // ============================================
        // 4. CREATE SPRINT PATTERN "Dilluns"
        // ============================================
        
        // Calculate sprint dates so that the 4th sprint is active today (Jan 5, 2026)
        // 4 sprints, 2 weeks each, consecutive
        // Sprint 4 should contain today, so it starts before today and ends after
        LocalDate today = LocalDate.now();
        
        // Sprint 4 (active): starts ~1 week ago, ends ~1 week from now
        LocalDate sprint4Start = today.minusDays(7);
        LocalDate sprint4End = sprint4Start.plusDays(14);
        
        // Sprint 3: 2 weeks before sprint 4
        LocalDate sprint3Start = sprint4Start.minusDays(14);
        LocalDate sprint3End = sprint4Start;
        
        // Sprint 2: 2 weeks before sprint 3
        LocalDate sprint2Start = sprint3Start.minusDays(14);
        LocalDate sprint2End = sprint3Start;
        
        // Sprint 1: 2 weeks before sprint 2
        LocalDate sprint1Start = sprint2Start.minusDays(14);
        LocalDate sprint1End = sprint2Start;

        SprintPatternRequest patternRequest = new SprintPatternRequest();
        patternRequest.name = "Dilluns";
        patternRequest.items = new ArrayList<>();
        
        patternRequest.items.add(createPatternItem("Sprint 1", sprint1Start, sprint1End, 0));
        patternRequest.items.add(createPatternItem("Sprint 2", sprint2Start, sprint2End, 1));
        patternRequest.items.add(createPatternItem("Sprint 3", sprint3Start, sprint3End, 2));
        patternRequest.items.add(createPatternItem("Sprint 4", sprint4Start, sprint4End, 3));

        SprintPattern patternDilluns = sprintPatternService.createPattern(
            coursePDS.getId(), 
            patternRequest, 
            professor.getId()
        );

        // ============================================
        // 5. CREATE PROJECT pds25a FOR PDS COURSE
        // ============================================
        
        // Enroll students to course and create project
        List<String> studentEmails = students.stream()
            .map(User::getEmail)
            .toList();
        
        Project projectPDS = projectService.createProject(
            "pds25a", 
            studentEmails, 
            coursePDS.getId(), 
            professor.getId()
        );

        // ============================================
        // 6. CREATE ACTUAL SPRINTS MIRRORING THE PATTERN
        // ============================================
        
        // Create sprints for the project
        Sprint sprint1 = sprintService.create(
            projectPDS, 
            "Sprint 1", 
            toDate(sprint1Start), 
            toDate(sprint1End), 
            professor.getId()
        );
        
        Sprint sprint2 = sprintService.create(
            projectPDS, 
            "Sprint 2", 
            toDate(sprint2Start), 
            toDate(sprint2End), 
            professor.getId()
        );
        
        Sprint sprint3 = sprintService.create(
            projectPDS, 
            "Sprint 3", 
            toDate(sprint3Start), 
            toDate(sprint3End), 
            professor.getId()
        );
        
        Sprint sprint4 = sprintService.create(
            projectPDS, 
            "Sprint 4", 
            toDate(sprint4Start), 
            toDate(sprint4End), 
            professor.getId()
        );

        // ============================================
        // 7. POPULATE SPRINTS WITH TASKS
        // ============================================
        
        List<Sprint> allSprints = List.of(sprint1, sprint2, sprint3, sprint4);
        int storyIndex = 0;
        
        for (int i = 0; i < allSprints.size(); i++) {
            Sprint sprint = allSprints.get(i);
            boolean isActiveSprint = (i == 3); // Sprint 4 is active
            boolean isClosedSprint = (i < 3);  // Sprints 1-3 are closed
            
            // Activate the sprint first
            activateSprint(sprint, professor.getId());
            
            // Create 6 stories per sprint
            for (int s = 0; s < 6; s++) {
                User reporter = students.get(random.nextInt(students.size()));
                String storyName = storyTemplates.get(storyIndex % storyTemplates.size());
                storyIndex++;
                
                // Create the story (user story task)
                Task story = taskService.createTask(projectPDS.getId(), storyName, reporter.getId());
                
                // Assign to sprint and configure
                User assignee = students.get(random.nextInt(students.size()));
                MergePatchTask storyEdit = new MergePatchTask();
                storyEdit.assignee = Optional.of(assignee.getEmail());
                storyEdit.estimationPoints = Optional.of(possibleEstimationPoints.get(random.nextInt(possibleEstimationPoints.size())));
                storyEdit.activeSprints = Optional.of(List.of(sprint.getId()));
                storyEdit.rank = Optional.of(s + 1);
                
                // Set status based on sprint state
                if (isClosedSprint) {
                    storyEdit.status = Optional.of(TaskStatus.DONE);
                } else if (isActiveSprint) {
                    storyEdit.status = Optional.of(getRandomActiveStatus());
                }
                
                taskService.editTaskInternal(story.getId(), storyEdit, assignee.getId());
                
                // Create 3 subtasks for each story (admin can create subtasks)
                for (int t = 0; t < 3; t++) {
                    String subtaskName = subtaskPrefixes.get(t % subtaskPrefixes.size()) + " " + 
                        storyName.replace("As a user, I want to ", "").toLowerCase();
                    
                    Task subtask = taskService.createSubTask(story.getId(), subtaskName, admin.getId());
                    
                    User subtaskAssignee = students.get(random.nextInt(students.size()));
                    MergePatchTask subtaskEdit = new MergePatchTask();
                    subtaskEdit.assignee = Optional.of(subtaskAssignee.getEmail());
                    subtaskEdit.estimationPoints = Optional.of(possibleEstimationPoints.get(random.nextInt(3))); // Smaller points for subtasks
                    subtaskEdit.activeSprints = Optional.of(List.of(sprint.getId()));
                    
                    // Set status based on sprint state
                    if (isClosedSprint) {
                        subtaskEdit.status = Optional.of(TaskStatus.DONE);
                    } else if (isActiveSprint) {
                        subtaskEdit.status = Optional.of(getRandomActiveStatus());
                    }
                    
                    taskService.editTaskInternal(subtask.getId(), subtaskEdit, subtaskAssignee.getId());
                }
                
                // Add some comments to stories in active sprint
                if (isActiveSprint && random.nextBoolean()) {
                    addRandomComments(story, students);
                }
            }
            
            // Close sprints 1-3
            if (isClosedSprint) {
                closeSprint(sprint, professor.getId());
            }
        }

        // ============================================
        // 8. CREATE SOME BACKLOG TASKS (not in any sprint)
        // ============================================
        
        for (int i = 0; i < 4; i++) {
            User reporter = students.get(random.nextInt(students.size()));
            String storyName = storyTemplates.get((storyIndex + i) % storyTemplates.size());
            
            Task backlogTask = taskService.createTask(projectPDS.getId(), storyName, reporter.getId());
            
            MergePatchTask backlogEdit = new MergePatchTask();
            backlogEdit.estimationPoints = Optional.of(possibleEstimationPoints.get(random.nextInt(possibleEstimationPoints.size())));
            // Leave in BACKLOG status, no sprint assigned
            taskService.editTaskInternal(backlogTask.getId(), backlogEdit, reporter.getId());
        }

        // Set professor's current project
        userService.setCurrentProject(professor, projectPDS);

        logger.info("Database seeding completed successfully!");
        logger.info("Created: 2 subjects, 2 courses, 1 sprint pattern, 1 project, 4 sprints, {} tasks", 
            (6 * 4) + (6 * 4 * 3) + 4); // 6 stories * 4 sprints + 3 subtasks each + 4 backlog
    }

    /**
     * Create 6 test students
     */
    private List<User> createTestStudents() {
        List<String> names = Arrays.asList(
            "Alice Johnson",
            "Bob Smith", 
            "Carol Williams",
            "David Brown",
            "Eva Martinez",
            "Frank Garcia"
        );
        
        List<User> students = new ArrayList<>();
        int index = 1;
        for (String name : names) {
            String email = "student" + index + "@trackdev.com";
            String password = global.getPasswordEncoder().encode("student" + index);
            User student = userService.addUserInternal(name, email, password, List.of(UserType.STUDENT));
            students.add(student);
            index++;
        }
        return students;
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
        
        int numComments = random.nextInt(3) + 1; // 1-3 comments
        for (int i = 0; i < numComments; i++) {
            User commenter = students.get(random.nextInt(students.size()));
            String comment = comments.get(random.nextInt(comments.size()));
            commentService.addComment(comment, commenter, task);
        }
    }
}
