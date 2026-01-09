package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.Course;
import org.trackdev.api.entity.SprintPattern;
import org.trackdev.api.entity.SprintPatternItem;
import org.trackdev.api.model.SprintPatternRequest;
import org.trackdev.api.repository.SprintPatternRepository;
import org.trackdev.api.utils.ErrorConstants;

import java.util.List;

@Service
public class SprintPatternService extends BaseServiceLong<SprintPattern, SprintPatternRepository> {

    @Autowired
    CourseService courseService;

    @Autowired
    AccessChecker accessChecker;

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
     * Update an existing sprint pattern (professors only)
     */
    @Transactional
    public SprintPattern updatePattern(Long patternId, SprintPatternRequest request, String userId) {
        SprintPattern pattern = get(patternId);
        accessChecker.checkCanManageCourse(pattern.getCourse(), userId);

        pattern.setName(request.name);

        // Clear existing items and add new ones
        pattern.clearItems();
        
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
     * Delete a sprint pattern (professors only)
     */
    @Transactional
    public void deletePattern(Long patternId, String userId) {
        SprintPattern pattern = get(patternId);
        accessChecker.checkCanManageCourse(pattern.getCourse(), userId);
        repo.delete(pattern);
    }
}
