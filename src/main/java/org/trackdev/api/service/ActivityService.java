package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.entity.*;
import org.trackdev.api.repository.ActivityRepository;
import org.trackdev.api.repository.UserActivityAccessRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
public class ActivityService extends BaseServiceLong<Activity, ActivityRepository> {

    @Autowired
    UserActivityAccessRepository userActivityAccessRepository;

    @Autowired
    UserService userService;

    /**
     * Record a new activity event.
     */
    @Transactional
    public Activity recordActivity(ActivityType type, User actor, Project project, Task task, 
                                    String message, String oldValue, String newValue) {
        Activity activity = new Activity();
        activity.setType(type);
        activity.setActor(actor);
        activity.setProject(project);
        activity.setTask(task);
        activity.setMessage(message);
        activity.setOldValue(oldValue);
        activity.setNewValue(newValue);
        activity.setCreatedAt(LocalDateTime.now());
        return repo().save(activity);
    }

    /**
     * Record a simple activity without old/new values.
     */
    @Transactional
    public Activity recordActivity(ActivityType type, User actor, Project project, Task task, String message) {
        return recordActivity(type, actor, project, task, message, null, null);
    }

    /**
     * Record an activity for a task without a custom message.
     */
    @Transactional
    public Activity recordActivity(ActivityType type, User actor, Task task) {
        return recordActivity(type, actor, task.getProject(), task, null, null, null);
    }

    /**
     * Get activities for a user's projects, paginated.
     */
    public Page<Activity> getActivitiesForUser(String userId, Pageable pageable) {
        User user = userService.get(userId);
        Collection<Project> projects = user.getProjects();
        if (projects == null || projects.isEmpty()) {
            return Page.empty(pageable);
        }
        return repo().findByProjectInOrderByCreatedAtDesc(projects, pageable);
    }

    /**
     * Get all activities for a user's projects.
     */
    public List<Activity> getActivitiesForUser(String userId) {
        User user = userService.get(userId);
        Collection<Project> projects = user.getProjects();
        if (projects == null || projects.isEmpty()) {
            return List.of();
        }
        return repo().findByProjectInOrderByCreatedAtDesc(projects);
    }

    /**
     * Count unread activities for a user since their last access.
     */
    public long getUnreadCount(String userId) {
        User user = userService.get(userId);
        Collection<Project> projects = user.getProjects();
        if (projects == null || projects.isEmpty()) {
            return 0;
        }
        
        LocalDateTime lastAccess = getLastAccessTime(user);
        if (lastAccess == null) {
            // User has never accessed activities, count all activities
            // Use a date far in the past but within MySQL's valid range
            return repo().countByProjectInAndCreatedAtAfter(projects, LocalDateTime.of(1970, 1, 1, 0, 0));
        }
        
        return repo().countByProjectInAndCreatedAtAfter(projects, lastAccess);
    }

    /**
     * Check if user has any unread activities.
     */
    public boolean hasUnreadActivities(String userId) {
        return getUnreadCount(userId) > 0;
    }

    /**
     * Mark all activities as read by updating the user's last access timestamp.
     * Uses pessimistic locking to prevent race conditions.
     */
    @Transactional
    public void markAsRead(String userId) {
        User user = userService.get(userId);
        
        // First check if record exists (without lock for better performance)
        boolean exists = userActivityAccessRepository.existsByUser(user);
        
        if (exists) {
            // Use locking query to update existing record
            UserActivityAccess access = userActivityAccessRepository.findByUserWithLock(user).orElse(null);
            if (access != null) {
                access.markAsAccessed();
                userActivityAccessRepository.save(access);
            }
        } else {
            // Create new record - if duplicate key occurs, it means another request just created it
            // which is fine - the timestamp will be close enough
            UserActivityAccess access = new UserActivityAccess(user);
            userActivityAccessRepository.save(access);
        }
    }

    /**
     * Get the last time a user accessed the activity feed.
     */
    public LocalDateTime getLastAccessTime(User user) {
        return userActivityAccessRepository.findByUser(user)
            .map(UserActivityAccess::getLastAccessedAt)
            .orElse(null);
    }

    /**
     * Get activities for a specific project, paginated.
     */
    public Page<Activity> getActivitiesForProject(Project project, Pageable pageable) {
        return repo().findByProjectOrderByCreatedAtDesc(project, pageable);
    }

    /**
     * Delete all activities for a project (used when project is deleted).
     */
    @Transactional
    public void deleteActivitiesForProject(Project project) {
        repo().deleteByProject(project);
    }
}
