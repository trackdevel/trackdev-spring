package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
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
    AccessChecker accessChecker;

    @Transactional
    public Project createProject(String name, Collection<String> emails, Long courseId,
                                 String loggedInUserId) {
        Course course = courseService.get(courseId);
        accessChecker.checkCanManageCourse(course, loggedInUserId);
        Project project = new Project(name);
        
        // Generate unique slug based on project name
        project.setSlug(generateUniqueSlug(name));
        
        course.addProject(project);
        project.setCourse(course);

        if(emails != null && !emails.isEmpty()) {
            addMembers(course, project, emails);
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
    public Task createProjectTask(Long projectId, String name, String userId){
        Project project = get(projectId);
        User reporter = userService.get(userId);
        accessChecker.checkCanViewProject(project, userId);
        Task task = new Task(name, reporter);
        project.addTask(task);
        task.setProject(project);
        repo.save(project);
        return task;
    }

    @Transactional
    public void deleteProject(Long groupId, String userId) {
        Project project = get(groupId);
        accessChecker.checkCanManageProject(project, userId);
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
    public Collection<Sprint> getProjectSprints(Long projectId, String userId) {
        Project project = get(projectId);
        accessChecker.checkCanViewProject(project, userId);
        return project.getSprints();
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

    private void addMembers(Course course, Project project, Collection<String> usernames) {
        for(String username: usernames) {
            User user = userService.getByEmail(username);
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

}
