package org.trackdev.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.trackdev.api.dto.ActivitiesResponseDTO;
import org.trackdev.api.dto.ActivityDTO;
import org.trackdev.api.dto.ActivityUnreadCountDTO;
import org.trackdev.api.entity.Activity;
import org.trackdev.api.mapper.ActivityMapper;
import org.trackdev.api.service.ActivityService;

import java.security.Principal;
import java.util.List;

/**
 * Controller for activity/notification feed endpoints.
 * Provides access to the activity timeline for students.
 */
@Tag(name = "Activity", description = "Activity feed and notifications")
@RestController
@RequestMapping(path = "/activities")
@SecurityRequirement(name = "bearerAuth")
public class ActivityController extends BaseController {

    @Autowired
    ActivityService activityService;

    @Autowired
    ActivityMapper activityMapper;

    @Operation(summary = "Get activity feed", 
               description = "Get paginated list of activities for the logged-in user's projects with optional filters")
    @GetMapping
    public ActivitiesResponseDTO getActivities(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "projectId", required = false) Long projectId,
            @RequestParam(name = "sprintId", required = false) Long sprintId,
            @RequestParam(name = "actorId", required = false) String actorId,
            Principal principal) {
        String userId = getUserId(principal);
        Pageable pageable = PageRequest.of(page, Math.min(size, 100)); // Cap at 100
        
        Page<Activity> activityPage = activityService.getActivitiesForUserWithFilters(userId, projectId, sprintId, actorId, pageable);
        List<ActivityDTO> activityDTOs = activityMapper.toDTOList(activityPage.getContent());
        
        return new ActivitiesResponseDTO(
            activityDTOs,
            activityPage.getNumber(),
            activityPage.getSize(),
            activityPage.getTotalElements(),
            activityPage.getTotalPages(),
            activityPage.hasNext(),
            activityPage.hasPrevious()
        );
    }

    @Operation(summary = "Get unread count", 
               description = "Get the count of unread activities since last access")
    @GetMapping("/unread-count")
    public ActivityUnreadCountDTO getUnreadCount(Principal principal) {
        String userId = getUserId(principal);
        long unreadCount = activityService.getUnreadCount(userId);
        return new ActivityUnreadCountDTO(unreadCount);
    }

    @Operation(summary = "Mark activities as read", 
               description = "Mark all activities as read by updating last access timestamp")
    @PostMapping("/mark-read")
    public ResponseEntity<Void> markAsRead(Principal principal) {
        String userId = getUserId(principal);
        activityService.markAsRead(userId);
        return okNoContent();
    }
}
