package org.trackdev.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Security Audit Logger for tracking security-sensitive operations.
 * 
 * This service logs security events such as:
 * - Login attempts (successful and failed)
 * - Privilege escalation attempts
 * - Administrative actions
 * - Unauthorized access attempts
 * 
 * In production, these logs should be forwarded to a SIEM system
 * (e.g., Splunk, ELK Stack) for monitoring and alerting.
 */
@Service
public class SecurityAuditLogger {
    
    private static final Logger auditLogger = LoggerFactory.getLogger("SECURITY_AUDIT");
    
    /**
     * Log a successful login.
     */
    public void logLoginSuccess(String email, String ipAddress) {
        auditLogger.info("LOGIN_SUCCESS | email={} | ip={} | timestamp={}", 
            email, ipAddress, Instant.now());
    }
    
    /**
     * Log a failed login attempt.
     */
    public void logLoginFailure(String email, String ipAddress, String reason) {
        auditLogger.warn("LOGIN_FAILURE | email={} | ip={} | reason={} | timestamp={}", 
            email, ipAddress, reason, Instant.now());
    }
    
    /**
     * Log when a user is rate limited.
     */
    public void logRateLimited(String email, String ipAddress) {
        auditLogger.warn("RATE_LIMITED | email={} | ip={} | timestamp={}", 
            email, ipAddress, Instant.now());
    }
    
    /**
     * Log unauthorized access attempt.
     */
    public void logUnauthorizedAccess(String userId, String resource, String action) {
        auditLogger.warn("UNAUTHORIZED_ACCESS | userId={} | resource={} | action={} | timestamp={}", 
            userId, resource, action, Instant.now());
    }
    
    /**
     * Log an administrative action.
     */
    public void logAdminAction(String adminId, String action, String targetUser, String details) {
        auditLogger.info("ADMIN_ACTION | adminId={} | action={} | target={} | details={} | timestamp={}", 
            adminId, action, targetUser, details, Instant.now());
    }
    
    /**
     * Log user registration.
     */
    public void logUserRegistration(String adminId, String newUserEmail) {
        auditLogger.info("USER_REGISTRATION | adminId={} | newUser={} | timestamp={}", 
            adminId, newUserEmail, Instant.now());
    }
    
    /**
     * Log password change.
     */
    public void logPasswordChange(String userId, String ipAddress, boolean viaRecovery) {
        auditLogger.info("PASSWORD_CHANGE | userId={} | ip={} | viaRecovery={} | timestamp={}", 
            userId, ipAddress, viaRecovery, Instant.now());
    }
    
    /**
     * Log role/privilege changes.
     */
    public void logPrivilegeChange(String adminId, String targetUserId, String oldRoles, String newRoles) {
        auditLogger.warn("PRIVILEGE_CHANGE | adminId={} | targetUser={} | oldRoles={} | newRoles={} | timestamp={}", 
            adminId, targetUserId, oldRoles, newRoles, Instant.now());
    }
    
    /**
     * Log suspicious activity.
     */
    public void logSuspiciousActivity(String userId, String activity, String details) {
        auditLogger.error("SUSPICIOUS_ACTIVITY | userId={} | activity={} | details={} | timestamp={}", 
            userId, activity, details, Instant.now());
    }
    
    /**
     * Log token validation failure.
     */
    public void logTokenValidationFailure(String ipAddress, String reason) {
        auditLogger.warn("TOKEN_VALIDATION_FAILURE | ip={} | reason={} | timestamp={}", 
            ipAddress, reason, Instant.now());
    }
}
