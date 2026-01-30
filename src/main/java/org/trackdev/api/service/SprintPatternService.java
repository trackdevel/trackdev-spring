package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.Course;
import org.trackdev.api.entity.Sprint;
import org.trackdev.api.entity.SprintPattern;
import org.trackdev.api.entity.SprintPatternItem;
import org.trackdev.api.model.SprintPatternRequest;
import org.trackdev.api.repository.SprintPatternRepository;
import org.trackdev.api.repository.SprintRepository;
import org.trackdev.api.utils.ErrorConstants;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SprintPatternService extends BaseServiceLong<SprintPattern, SprintPatternRepository> {

    @Autowired
    CourseService courseService;

    @Autowired
    AccessChecker accessChecker;

    @Autowired
    SprintRepository sprintRepository;

    /**
     * Get all sprint patterns for a course
     */
    public List<SprintPattern> getPatternsByCourse(Long courseId, String userId) {
        Course course = courseService.get(courseId);
        accessChecker.checkCanViewCourse(course, userId);
        return repo.findByCourse_IdOrderByName(courseId);
    }

    /**
     * Get a specific sprint pattern
     */
    public SprintPattern getPattern(Long patternId, String userId) {
        SprintPattern pattern = get(patternId);
        accessChecker.checkCanViewCourse(pattern.getCourse(), userId);
        return pattern;
    }

    /**
     * Create a new sprint pattern for a course (professors only)
     */
    @Transactional
    public SprintPattern createPattern(Long courseId, SprintPatternRequest request, String userId) {
        Course course = courseService.get(courseId);
        accessChecker.checkCanManageCourse(course, userId);

        SprintPattern pattern = new SprintPattern(request.name, course);
        
        if (request.items != null) {
            int index = 0;
            for (SprintPatternRequest.SprintPatternItemRequest itemReq : request.items) {
                SprintPatternItem item = new SprintPatternItem(
                    itemReq.name,
                    itemReq.startDate,
                    itemReq.endDate,
                    itemReq.orderIndex != null ? itemReq.orderIndex : index
                );
                pattern.addItem(item);
                index++;
            }
        }

        return repo.save(pattern);
    }

    /**
     * Update an existing sprint pattern (professors only).
     * Changes to items are propagated to all associated Sprints.
     */
    @Transactional
    public SprintPattern updatePattern(Long patternId, SprintPatternRequest request, String userId) {
        SprintPattern pattern = get(patternId);
        accessChecker.checkCanManageCourse(pattern.getCourse(), userId);

        pattern.setName(request.name);

        if (request.items != null) {
            // Create a map of existing items by ID for quick lookup
            Map<Long, SprintPatternItem> existingItemsMap = pattern.getItems().stream()
                    .collect(Collectors.toMap(SprintPatternItem::getId, Function.identity()));
            
            // Track which existing items are still in the request
            Set<Long> updatedItemIds = new HashSet<>();
            
            int index = 0;
            for (SprintPatternRequest.SprintPatternItemRequest itemReq : request.items) {
                int orderIndex = itemReq.orderIndex != null ? itemReq.orderIndex : index;
                
                if (itemReq.id != null && existingItemsMap.containsKey(itemReq.id)) {
                    // Update existing item
                    SprintPatternItem existingItem = existingItemsMap.get(itemReq.id);
                    existingItem.setName(itemReq.name);
                    existingItem.setStartDate(itemReq.startDate);
                    existingItem.setEndDate(itemReq.endDate);
                    existingItem.setOrderIndex(orderIndex);
                    updatedItemIds.add(itemReq.id);
                    
                    // Propagate changes to associated sprints
                    propagateChangesToSprints(existingItem);
                } else {
                    // Create new item
                    SprintPatternItem newItem = new SprintPatternItem(
                        itemReq.name,
                        itemReq.startDate,
                        itemReq.endDate,
                        orderIndex
                    );
                    pattern.addItem(newItem);
                }
                index++;
            }
            
            // Remove items that are no longer in the request
            List<SprintPatternItem> itemsToRemove = pattern.getItems().stream()
                    .filter(item -> item.getId() != null && !updatedItemIds.contains(item.getId()))
                    .collect(Collectors.toList());
            for (SprintPatternItem item : itemsToRemove) {
                pattern.removeItem(item);
            }
        } else {
            // No items in request - clear all items
            pattern.clearItems();
        }

        return repo.save(pattern);
    }

    /**
     * Propagate changes from a SprintPatternItem to all associated Sprints.
     */
    private void propagateChangesToSprints(SprintPatternItem item) {
        List<Sprint> associatedSprints = sprintRepository.findByPatternItemId(item.getId());
        for (Sprint sprint : associatedSprints) {
            sprint.setName(item.getName());
            sprint.setStartDate(item.getStartDate());
            sprint.setEndDate(item.getEndDate());
            sprintRepository.save(sprint);
        }
    }

    /**
     * Delete a sprint pattern (professors only)
     */
    @Transactional
    public void deletePattern(Long patternId, String userId) {
        SprintPattern pattern = get(patternId);
        accessChecker.checkCanManageCourse(pattern.getCourse(), userId);
        repo.delete(pattern);
    }
}
