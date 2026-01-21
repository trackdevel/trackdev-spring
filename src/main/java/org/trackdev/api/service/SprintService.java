package org.trackdev.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.Project;
import org.trackdev.api.entity.Sprint;
import org.trackdev.api.entity.SprintStatus;
import org.trackdev.api.entity.User;
import org.trackdev.api.entity.sprintchanges.*;
import org.trackdev.api.model.MergePatchSprint;
import org.trackdev.api.query.CriteriaParser;
import org.trackdev.api.query.GenericSpecificationsBuilder;
import org.trackdev.api.query.SearchSpecification;
import org.trackdev.api.repository.SprintRepository;
import org.trackdev.api.utils.ErrorConstants;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class SprintService extends BaseServiceLong<Sprint, SprintRepository> {

    private static final int SPRINT_STATUS_CHANGE_RATE = 1000 * 2 * 30; //1 second

    private static final Logger logger = LoggerFactory.getLogger(SprintService.class);

    @Autowired
    AccessChecker accessChecker;

    @Autowired
    UserService userService;

    @Autowired
    SprintChangeService sprintChangeService;

    @Transactional
    public Sprint create(Project project, String name, ZonedDateTime startDate, ZonedDateTime endDate, String userId) {
        // Validate that end date is after start date
        if (startDate != null && endDate != null && !endDate.isAfter(startDate)) {
            throw new ServiceException(ErrorConstants.SPRINT_END_DATE_BEFORE_START);
        }
        
        User user = userService.get(userId);
        Sprint sprint = new Sprint(name);
        sprint.setStartDate(startDate);
        sprint.setEndDate(endDate);
        sprint.setProject(project);
        this.repo().save(sprint);
        return sprint;
    }

    /**
     * Edit a sprint with authorization check.
     * Only professors who own the course or admins can edit sprints.
     */
    @Transactional
    public Sprint editSprint(Long sprintId, MergePatchSprint editSprint, String userId) {
        Sprint sprint = get(sprintId);
        User user = userService.get(userId);
        // Only professors who own the course or admins can edit sprints
        accessChecker.checkCanManageProject(sprint.getProject(), userId);
        return applySprintChanges(sprint, editSprint, user);
    }

    /**
     * Internal method to edit sprint without authorization check.
     * Used for seeding and internal operations only.
     */
    @Transactional
    public Sprint editSprintInternal(Long sprintId, MergePatchSprint editSprint, String userId) {
        Sprint sprint = get(sprintId);
        User user = userService.get(userId);
        return applySprintChanges(sprint, editSprint, user);
    }

    /**
     * Apply changes to a sprint and save.
     */
    private Sprint applySprintChanges(Sprint sprint, MergePatchSprint editSprint, User user) {
        List<SprintChange> changes = new ArrayList<>();
        if(editSprint.name != null) {
            String name = editSprint.name.orElseThrow(
                    () -> new ServiceException(ErrorConstants.CAN_NOT_BE_NULL));
            if(!name.equals(sprint.getName())) {
                sprint.setName(name);
                changes.add(new SprintNameChange(user, sprint, name));
            }
        }
        if(editSprint.startDate != null) {
            ZonedDateTime startDate = editSprint.startDate.orElseThrow(
                    () -> new ServiceException(ErrorConstants.CAN_NOT_BE_NULL));
            if(!startDate.equals(sprint.getStartDate())) {
                sprint.setStartDate(startDate);
                changes.add(new SprintStartDateChange(user, sprint, startDate));
            }
        }
        if(editSprint.endDate != null) {
            ZonedDateTime endDate = editSprint.endDate.orElseThrow(
                    () -> new ServiceException(ErrorConstants.CAN_NOT_BE_NULL));
            if(!endDate.equals(sprint.getEndDate())) {
                sprint.setEndDate(endDate);
                changes.add(new SprintEndDateChange(user, sprint, endDate));
            }
        }
        if(editSprint.status != null) {
            SprintStatus status = editSprint.status.orElseThrow(
                    () -> new ServiceException(ErrorConstants.CAN_NOT_BE_NULL));
            if(status != sprint.getStatus()) {
                sprint.setStatus(status);
                changes.add(new SprintStatusChange(user, sprint, status));
            }
        }
        repo().save(sprint);
        for (SprintChange change : changes) {
            sprintChangeService.store(change);
        }
        return sprint;
    }

    @Transactional
    public void deleteSprint(Long sprintId, String userId) {
        Sprint sprint = get(sprintId);
        // Only professors who own the course or admins can delete sprints
        accessChecker.checkCanManageProject(sprint.getProject(), userId);
        repo().delete(sprint);
    }

    /**
     * Get a sprint with authorization check.
     */
    public Sprint getSprint(Long sprintId, String userId) {
        Sprint sprint = get(sprintId);
        accessChecker.checkCanViewProject(sprint.getProject(), userId);
        return sprint;
    }

    /**
     * Get sprint history with authorization check in a single transaction.
     * This ensures atomicity between the auth check and the data retrieval.
     */
    @Transactional(readOnly = true)
    public List<SprintChange> getSprintHistory(Long sprintId, String userId, String search) {
        Sprint sprint = get(sprintId);
        accessChecker.checkCanViewProject(sprint.getProject(), userId);
        
        String refinedSearch = "sprint.id:" + sprintId + (search != null ? " and ( " + search + " )" : "");
        Specification<SprintChange> specification = buildSpecification(refinedSearch);
        return sprintChangeService.search(specification);
    }

    private <K> Specification<K> buildSpecification(String search) {
        CriteriaParser parser = new CriteriaParser();
        GenericSpecificationsBuilder<K> specBuilder = new GenericSpecificationsBuilder<>();
        return specBuilder.build(parser.parse(search), SearchSpecification::new);
    }


    public Collection<Sprint> getSpritnsByIds(Collection<Long> sprintIds) {
        return repo.findAllById(sprintIds);
    }

    @Transactional
    @Scheduled(fixedRate = SPRINT_STATUS_CHANGE_RATE)
    public void triggerSprintStatusChange() {
        logger.info("--- Start triggering sprint status change");
        Collection<Sprint> sprintsToClose = repo().sprintsToClose();
        if (sprintsToClose.size() > 0) {
            logger.info("-- Sprints to CLOSE status: " + sprintsToClose.size());
            for (Sprint sprint : sprintsToClose) {
                sprint.setStatus(SprintStatus.CLOSED);
            }
            repo.saveAll(sprintsToClose);
            logger.info("-- Sprints to CLOSED status changed");
        }
        Collection<Sprint> sprintsToDraft = repo().sprintsToDraft();
        if(sprintsToDraft.size() > 0) {
            logger.info("-- Sprints to DRAFT status: " + sprintsToDraft.size());
            for(Sprint sprint : sprintsToDraft) {
                sprint.setStatus(SprintStatus.DRAFT);
            }
            repo.saveAll(sprintsToDraft);
            logger.info("-- Sprints to DRAFT status changed");
        }
        Collection<Sprint> sprintsToActive = repo().sprintsToActive();
        if(sprintsToActive.size() > 0) {
            logger.info("-- Sprints to ACTIVE status: " + sprintsToActive.size());
            for (Sprint sprint : sprintsToActive) {
                sprint.setStatus(SprintStatus.ACTIVE);
            }
            repo.saveAll(sprintsToActive);
            logger.info("-- Sprints to ACTIVE status changed");
        }
        logger.info("--- Done triggering sprint status change");
    }
}
