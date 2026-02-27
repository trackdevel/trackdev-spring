package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.*;
import org.trackdev.api.repository.GroupRepository;
import org.trackdev.api.utils.ErrorConstants;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectService extends BaseServiceLong<Project, GroupRepository> {

    @Autowired
    UserService userService;

    @Autowired
    CourseService courseService;

    @Autowired
    SprintService sprintService;

    @Autowired
    SprintPatternService sprintPatternService;

    @Autowired
    AccessChecker accessChecker;

    @Autowired
    @Lazy
    TaskService taskService;

    @Transactional
    public Project createProject(String name, Collection<String> memberIds, Long courseId,
                                 String loggedInUserId) {
        Course course = courseService.get(courseId);
        accessChecker.checkCanManageCourse(course, loggedInUserId);
        Project project = new Project(name);
        
        // Generate unique slug based on project name
        project.setSlug(generateUniqueSlug(name));
        
        course.addProject(project);
        project.setCourse(course);

        if(memberIds != null && !memberIds.isEmpty()) {
            addMembers(course, project, memberIds);
        }
        repo.save(project);
        return project;
    }

    /**
     * Generates a unique slug based on the project name.
     * Tries name-based candidates first, then falls back to random if needed.
     * @param name The project name to derive the slug from
     * @return A unique slug
     */
    private String generateUniqueSlug(String name) {
        // Try name-based slugs first, starting with 3 characters
        for (int length = Project.MIN_SLUG_LENGTH; length <= Project.MAX_SLUG_LENGTH; length++) {
            List<String> candidates = Project.generateSlugCandidatesFromName(name, length);
            for (String slug : candidates) {
                if (!repo.existsBySlug(slug)) {
                    return slug;
                }
            }
        }
        
        // Fall back to random slugs if name-based ones are all taken
        // Try with 3 characters first
        for (int attempt = 0; attempt < 10; attempt++) {
            String slug = Project.generateRandomSlug(Project.MIN_SLUG_LENGTH);
            if (!repo.existsBySlug(slug)) {
                return slug;
            }
        }
        
        // Try with 4 characters
        for (int attempt = 0; attempt < 10; attempt++) {
            String slug = Project.generateRandomSlug(4);
            if (!repo.existsBySlug(slug)) {
                return slug;
            }
        }
        
        // Try with 5 characters
        for (int attempt = 0; attempt < 10; attempt++) {
            String slug = Project.generateRandomSlug(Project.MAX_SLUG_LENGTH);
            if (!repo.existsBySlug(slug)) {
                return slug;
            }
        }
        
        // Extremely unlikely to reach here
        throw new ServiceException(ErrorConstants.CANNOT_GENERATE_UNIQUE_SLUG);
    }

    @Transactional
    public Project editProject(Long projectId, String name, Collection<String> memberIds, Long courseId, Double qualification
                               , String loggedInUserId) {
        Project project = get(projectId);
        accessChecker.checkCanManageProject(project, loggedInUserId);
        if(name != null) {
            project.setName(name);
        }
        if(memberIds != null) {
            if(memberIds.isEmpty() && !project.getMembers().isEmpty()) {
                throw new ServiceException(ErrorConstants.PRJ_WITHOUT_MEMBERS);
            }
            editMembers(memberIds, project);
        }
        if(courseId != null) {
            Course course = courseService.get(courseId);
            project.setCourse(course);
        }
        project.setQualification(qualification);
        repo.save(project);
        
        return project;
    }

    /**
     * Create a task in a project with authorization check.
     * All operations in a single transaction.
     */
    @Transactional
    public Task createProjectTask(Long projectId, String name, String description, TaskType type, String assigneeId, String userId){
        Project project = get(projectId);
        User reporter = userService.get(userId);
        accessChecker.checkCanViewProject(project, userId);
        Task task = new Task(name, reporter);
        // Set type - default to USER_STORY if not provided
        task.setType(type != null ? type : TaskType.USER_STORY);
        // Set description if provided
        if (description != null && !description.isBlank()) {
            task.setDescription(description);
        }
        // Set assignee if provided
        if (assigneeId != null && !assigneeId.isBlank()) {
            User assignee = userService.get(assigneeId);
            task.setAssignee(assignee);
        }
        project.addTask(task);
        task.setProject(project);
        repo.save(project);
        return task;
    }

    /**
     * Delete a project. Only allowed if the project has no tasks.
     * Sprints associated with the project are deleted automatically via cascade.
     * 
     * @param projectId The project ID to delete
     * @param userId The user performing the deletion
     */
    @Transactional
    public void deleteProject(Long projectId, String userId) {
        Project project = get(projectId);
        
        // Check authorization - only course owner/admin can delete
        accessChecker.checkCanManageCourse(project.getCourse(), userId);
        
        // Check if project has any tasks
        if (taskService.existsByProjectId(projectId)) {
            throw new ServiceException(ErrorConstants.PROJECT_HAS_TASKS);
        }
        
        // Remove project from course
        Course course = project.getCourse();
        if (course != null) {
            course.getProjects().remove(project);
        }
        
        // Delete the project (sprints will be deleted via cascade)
        repo.delete(project);
    }

    /**
     * Get all projects visible to a user.
     * Includes: projects where user is a member + projects from courses they own + projects from subjects they own.
     */
    public Collection<Project> getProjectsForUser(String userId) {
        User user = userService.get(userId);
        Set<Project> allProjects = new HashSet<>(user.getProjects());
        
        // Add projects from courses the user owns or where they own the subject
        Collection<Course> courses = courseService.getCoursesForUser(userId);
        for (Course course : courses) {
            allProjects.addAll(course.getProjects());
        }
        
        return allProjects;
    }

    /**
     * Get projects paginated and sorted by name.
     * Admin/workspace admin sees all projects; other users see only their projects.
     * Optionally filters by courseId.
     */
    @Transactional(readOnly = true)
    public Page<Project> getProjectsPaginated(String userId, Long courseId, Pageable pageable) {
        if (accessChecker.checkCanViewAllProjects(userId)) {
            if (courseId != null) {
                return repo.findByCourseIdOrderByNameAsc(courseId, pageable);
            }
            return repo.findAllByOrderByNameAsc(pageable);
        } else {
            if (courseId != null) {
                return repo.findProjectsForUserByCourse(userId, courseId, pageable);
            }
            return repo.findProjectsForUser(userId, pageable);
        }
    }

    /**
     * Get a project with authorization check and set as current project.
     * All operations in a single transaction.
     */
    @Transactional
    public Project getProjectAndSetCurrent(Long projectId, String userId) {
        Project project = get(projectId);
        accessChecker.checkCanViewProject(project, userId);
        User user = userService.get(userId);
        userService.setCurrentProject(user, project);
        return project;
    }

    /**
     * Get a project with authorization check.
     */
    public Project getProject(Long projectId, String userId) {
        Project project = get(projectId);
        accessChecker.checkCanViewProject(project, userId);
        return project;
    }

    /**
     * Get project sprints with authorization check.
     */
    @Transactional(readOnly = true)
    public Collection<Sprint> getProjectSprints(Long projectId, String userId) {
        Project project = get(projectId);
        accessChecker.checkCanViewProject(project, userId);
        Collection<Sprint> sprints = project.getSprints();
        // Force initialization of lazy collection
        if (sprints != null) {
            sprints.size();
        }
        return sprints;
    }

    /**
     * Get project tasks with authorization check.
     * Returns only top-level tasks (without parent).
     */
    public Collection<Task> getProjectTasks(Long projectId, String userId) {
        Project project = get(projectId);
        accessChecker.checkCanViewProject(project, userId);
        return project.getTasks();
    }

    /**
     * Get all project tasks including subtasks with authorization check.
     * Uses transactional context to ensure childTasks are accessible for
     * USER_STORY estimation points calculation.
     */
    @Transactional(readOnly = true)
    public Collection<Task> getAllProjectTasks(Long projectId, String userId) {
        Project project = get(projectId);
        accessChecker.checkCanViewProject(project, userId);
        Collection<Task> tasks = project.getAllTasks();
        // Initialize childTasks for USER_STORY tasks to enable estimation calculation
        for (Task task : tasks) {
            if (task.getTaskType() == TaskType.USER_STORY && task.getChildTasks() != null) {
                task.getChildTasks().size(); // Force initialization
            }
        }
        return tasks;
    }

    public Collection<Sprint> getProjectSprints(Project project) {
        return project.getSprints();
    }

    public Collection<Task> getProjectTasks(Project project) {
        return project.getTasks();
    }

    /**
     * Create a sprint in a project with authorization check.
     * All operations in a single transaction.
     */
    @Transactional
    public Sprint createSprint(Long projectId, String name, ZonedDateTime startDate, ZonedDateTime endDate, String userId) {
        Project project = get(projectId);
        accessChecker.checkCanViewProject(project, userId);
        Sprint sprint = sprintService.create(project, name, startDate, endDate, userId);
        project.addSprint(sprint);
        repo.save(project);
        return sprint;
    }

    public Sprint createSprint(Project project, String name, ZonedDateTime startDate, ZonedDateTime endDate, String userId) {
        accessChecker.checkCanViewProject(project, userId);
        Sprint sprint = sprintService.create(project, name, startDate, endDate, userId);
        project.addSprint(sprint);
        repo.save(project);
        return sprint;
    }

    /**
     * Get project ranks for admin user with authorization check.
     * All operations in a single transaction.
     */
    @Transactional(readOnly = true)
    public Map<String, Map<String, String>> getProjectRanksForAdmin(Long projectId, String userId) {
        User user = userService.get(userId);
        accessChecker.checkIsUserAdmin(user);
        Project project = get(projectId);
        return getProjectRanks(project);
    }

    public Map<String, Map<String,String>> getProjectRanks(Project project) {
        if(project.getQualification() != null){
            Map<String, Map<String,String>> ranks = new HashMap<>();
            Map<User, Integer> points = project.getTasks().stream()
                    .filter(task -> task.getAssignee() != null)
                    .collect(Collectors.groupingBy(Task::getAssignee, Collectors.summingInt(Task::getEstimationPoints)));
            Integer maxPoints = points.values().stream().max(Integer::compareTo).orElse(0);
            for(User user: points.keySet()) {
                Map<String,String> info = new HashMap<>();
                info.put("name",user.getUsername());
                info.put("acronym",user.getCapitalLetters());
                info.put("color",user.getColor());
                info.put("qualification",String.valueOf(BigDecimal.valueOf(
                                points.get(user).doubleValue() * project.getQualification() / maxPoints.doubleValue())
                        .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
                ranks.put(user.getEmail(),info);
            }
            return ranks;
        }
        else{
            throw new ServiceException(ErrorConstants.PRJ_WITHOUT_QUALIFICATION);
        }
    }

    private void addMembers(Course course, Project project, Collection<String> memberIds) {
        for(String memberId: memberIds) {
            User user = userService.get(memberId);
            addMember(course, project, user);
        }
    }


    private void addMember(Course course, Project project, User user) {
        // Verify that the student is enrolled in the course
        if (!course.isStudentEnrolled(user)) {
            throw new ServiceException(ErrorConstants.STUDENT_NOT_ENROLLED);
        }
        project.addMember(user);
        user.addToGroup(project);
    }

    private void editMembers(Collection<String> memberIds, Project project) {
        // Add new members (if not already members)
        for(String memberId: memberIds) {
            User user = userService.get(memberId);
            if(!project.isMember(user)) {
                addMember(project.getCourse(), project, user);
            }
        }
        // Remove members that are not in the new list
        List<User> toRemove = new ArrayList<>();
        for(User user: project.getMembers()) {
            if(!memberIds.contains(user.getId())) {
                toRemove.add(user);
            }
        }
        for(User user: toRemove) {
            // Check if user has any assigned tasks in this project
            boolean hasAssignedTasks = project.getTasks().stream()
                    .anyMatch(task -> task.getAssignee() != null && task.getAssignee().getId().equals(user.getId()));
            
            if(hasAssignedTasks) {
                throw new ServiceException(ErrorConstants.CANNOT_REMOVE_MEMBER_HAS_ASSIGNED_TASKS);
            }
            
            project.removeMember(user);
            user.removeFromGroup(project);
        }
    }

    /**
     * Apply a sprint pattern to a project, creating sprints from the pattern items.
     * Only professors who manage the course can apply patterns.
     * 
     * @param projectId The ID of the project to apply the pattern to
     * @param patternId The ID of the sprint pattern to apply
     * @param userId The ID of the user performing the action
     * @return The updated project with the new sprints
     */
    @Transactional
    public Project applySprintPattern(Long projectId, Long patternId, String userId) {
        Project project = get(projectId);
        accessChecker.checkCanManageProject(project, userId);
        
        // Check if a pattern has already been applied
        if (project.getSprintPattern() != null) {
            throw new ServiceException(ErrorConstants.SPRINT_PATTERN_ALREADY_APPLIED);
        }
        
        SprintPattern pattern = sprintPatternService.get(patternId);
        
        // Verify pattern belongs to the same course as the project
        if (!pattern.getCourse().getId().equals(project.getCourse().getId())) {
            throw new ServiceException(ErrorConstants.SPRINT_PATTERN_NOT_IN_COURSE);
        }
        
        // Set the pattern reference on the project
        project.setSprintPattern(pattern);
        
        // Create sprints from pattern items
        for (SprintPatternItem item : pattern.getItems()) {
            Sprint sprint = new Sprint(item.getName());
            sprint.setStartDate(item.getStartDate());
            sprint.setEndDate(item.getEndDate());
            sprint.setProject(project);
            sprint.setSprintPatternItem(item);
            project.addSprint(sprint);
        }
        
        repo.save(project);
        return project;
    }

    /**
     * Get all DONE tasks in a project that have pull requests.
     * Optionally filter by sprint and/or assignee.
     * 
     * @param projectId The project ID
     * @param sprintId Optional sprint ID to filter by (null means all sprints)
     * @param assigneeId Optional assignee ID to filter by (null means all team members)
     * @param userId The user requesting the data
     * @return Collection of DONE tasks with PRs
     */
    @Transactional(readOnly = true)
    public Collection<Task> getDoneTasksWithPRs(Long projectId, Long sprintId, String assigneeId, String userId) {
        Project project = get(projectId);
        accessChecker.checkCanViewProject(project, userId);
        
        return taskService.findByProjectIdAndStatus(projectId, TaskStatus.DONE).stream()
                .filter(task -> task.getPullRequests() != null && !task.getPullRequests().isEmpty())
                .filter(task -> {
                    // If no sprint filter, include all tasks
                    if (sprintId == null) {
                        return true;
                    }
                    // Check if task belongs to the specified sprint
                    return task.getActiveSprints() != null && 
                           task.getActiveSprints().stream().anyMatch(s -> s.getId().equals(sprintId));
                })
                .filter(task -> {
                    // If no assignee filter, include all tasks
                    if (assigneeId == null || assigneeId.isEmpty()) {
                        return true;
                    }
                    // Check if task is assigned to the specified user
                    return task.getAssignee() != null && assigneeId.equals(task.getAssignee().getId());
                })
                .toList();
    }

}