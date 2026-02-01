package org.trackdev.api.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.trackdev.api.configuration.UserType;
import org.trackdev.api.entity.User;

/**
 * Service for handling data privacy.
 * Controls what information users can see based on their role.
 */
@Service
public class PrivacyService {

    private final UserService userService;

    public PrivacyService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Check if the current user can see the email of the target user.
     * 
     * Rules:
     * - Users can always see their own email
     * - ADMIN, WORKSPACE_ADMIN, PROFESSOR can see all emails
     * - STUDENT can only see their own email
     * 
     * @param targetUserId The user whose email is being accessed
     * @return true if the current user can see the target user's email
     */
    public boolean canSeeEmail(String targetUserId) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }

        // Users can always see their own email
        if (currentUserId.equals(targetUserId)) {
            return true;
        }

        // Check if current user is not a student
        User currentUser = userService.get(currentUserId);
        return !currentUser.isUserType(UserType.STUDENT);
    }

    /**
     * Check if the current user is a student.
     * 
     * @return true if the current user has only STUDENT role
     */
    public boolean isCurrentUserStudent() {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return true; // Treat unauthenticated as restricted
        }
        
        User currentUser = userService.get(currentUserId);
        return currentUser.isUserType(UserType.STUDENT);
    }

    /**
     * Get the current authenticated user's ID from the security context.
     */
    private String getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }

    /**
     * Filter email based on privacy rules.
     * Returns null if the current user cannot see the target user's email.
     * 
     * @param email The email to potentially filter
     * @param targetUserId The user whose email this is
     * @return The email if visible, null otherwise
     */
    public String filterEmail(String email, String targetUserId) {
        if (canSeeEmail(targetUserId)) {
            return email;
        }
        return null;
    }
}
