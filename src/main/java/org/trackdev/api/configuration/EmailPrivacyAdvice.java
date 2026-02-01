package org.trackdev.api.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.trackdev.api.dto.*;
import org.trackdev.api.entity.User;
import org.trackdev.api.service.UserService;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * ResponseBodyAdvice that filters email fields from API responses for STUDENT users.
 * 
 * STUDENT users can only see their own email (for Settings page).
 * Other users' emails are hidden - students only need fullName for display.
 * ADMIN, WORKSPACE_ADMIN, and PROFESSOR can see all emails.
 */
@ControllerAdvice
public class EmailPrivacyAdvice implements ResponseBodyAdvice<Object> {

    private static final Logger logger = LoggerFactory.getLogger(EmailPrivacyAdvice.class);
    
    private final UserService userService;

    public EmailPrivacyAdvice(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Apply to all responses
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                   Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                   ServerHttpRequest request, ServerHttpResponse response) {
        
        if (body == null) {
            return null;
        }

        // Check if current user is a student
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            // Not authenticated, don't filter
            return body;
        }

        if (!isCurrentUserStudent(currentUserId)) {
            // Non-student users can see all emails
            return body;
        }

        // Student user - filter emails from user DTOs except their own
        try {
            logger.info("EMAIL_PRIVACY: Filtering for student userId={}, body type={}", currentUserId, body.getClass().getSimpleName());
            filterEmailsRecursively(body, currentUserId, new HashSet<>());
        } catch (Exception e) {
            logger.warn("Error filtering emails from response", e);
        }

        return body;
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        return auth.getName();
    }

    private boolean isCurrentUserStudent(String userId) {
        try {
            User user = userService.get(userId);
            return user.isUserType(UserType.STUDENT);
        } catch (Exception e) {
            return true; // Treat errors as restricted
        }
    }

    /**
     * Recursively filter emails from user DTOs in the response.
     * Preserves the current user's own email (for Settings page).
     * Uses a visited set to prevent infinite recursion on circular references.
     */
    private void filterEmailsRecursively(Object obj, String currentUserId, Set<Integer> visited) {
        if (obj == null) {
            return;
        }

        // Prevent infinite recursion
        int identityHash = System.identityHashCode(obj);
        if (visited.contains(identityHash)) {
            return;
        }
        visited.add(identityHash);

        Class<?> clazz = obj.getClass();

        // Check if this is a user DTO that needs email filtering
        // Only filter if it's NOT the current user's own data
        // Note: Check more specific types first (subclasses before superclasses)
        if (obj instanceof UserWithGithubTokenDTO dto) {
            logger.info("EMAIL_PRIVACY: UserWithGithubTokenDTO found - dtoId='{}', currentUserId='{}', equals={}", 
                dto.getId(), currentUserId, currentUserId.equals(dto.getId()));
            if (!currentUserId.equals(dto.getId())) {
                dto.setEmail(null);
            }
        } else if (obj instanceof UserWithProjectsDTO dto) {
            if (!currentUserId.equals(dto.getId())) {
                dto.setEmail(null);
            }
        } else if (obj instanceof UserSummaryDTO dto) {
            if (!currentUserId.equals(dto.getId())) {
                dto.setEmail(null);
            }
        } else if (obj instanceof UserBasicDTO dto) {
            if (!currentUserId.equals(dto.getId())) {
                dto.setEmail(null);
            }
        }

        // Handle collections
        if (obj instanceof Collection<?>) {
            for (Object item : (Collection<?>) obj) {
                filterEmailsRecursively(item, currentUserId, visited);
            }
            return;
        }

        // Handle arrays
        if (clazz.isArray()) {
            Object[] array = (Object[]) obj;
            for (Object item : array) {
                filterEmailsRecursively(item, currentUserId, visited);
            }
            return;
        }

        // Skip primitives, enums, strings, and standard Java types
        if (clazz.isPrimitive() || clazz.isEnum() || 
            clazz.getName().startsWith("java.") || 
            clazz.getName().startsWith("javax.")) {
            return;
        }

        // Recurse into fields of DTO classes
        if (clazz.getPackageName().startsWith("org.trackdev.api.dto") ||
            clazz.getPackageName().startsWith("org.trackdev.api.model")) {
            try {
                for (Field field : getAllFields(clazz)) {
                    field.setAccessible(true);
                    Object fieldValue = field.get(obj);
                    if (fieldValue != null) {
                        filterEmailsRecursively(fieldValue, currentUserId, visited);
                    }
                }
            } catch (Exception e) {
                logger.debug("Error accessing fields of {}", clazz.getName(), e);
            }
        }
    }

    private Field[] getAllFields(Class<?> clazz) {
        // Get fields from this class and all superclasses
        Set<Field> fields = new HashSet<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                fields.add(field);
            }
            current = current.getSuperclass();
        }
        return fields.toArray(new Field[0]);
    }
}
