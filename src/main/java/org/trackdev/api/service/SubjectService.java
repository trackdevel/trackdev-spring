package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.controller.exceptions.EntityNotFound;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.Subject;
import org.trackdev.api.entity.User;
import org.trackdev.api.repository.SubjectRepository;
import org.trackdev.api.utils.ErrorConstants;

import java.util.List;
import java.util.Optional;

@Service
public class SubjectService extends BaseServiceLong<Subject, SubjectRepository> {

    @Autowired
    @Lazy
    UserService userService;

    @Autowired
    AccessChecker accessChecker;

    public Subject getSubject(Long id) {
        Optional<Subject> oc = this.repo.findById(id);
        if (oc.isEmpty())
            throw new EntityNotFound(ErrorConstants.SUBJECT_NOT_EXIST);
        return oc.get();
    }

    /**
     * Get a subject with authorization check.
     */
    public Subject getSubject(Long id, String userId) {
        Subject subject = getSubject(id);
        accessChecker.checkCanViewSubject(subject, userId);
        return subject;
    }


    @Transactional
    public Subject createSubject(String name, String acronym, String loggedInUserId) {
        User owner = userService.get(loggedInUserId);
        accessChecker.checkCanCreateSubject(owner);
        Subject subject = new Subject(name, acronym, owner);
        // Set workspace from owner's workspace (required for workspace admins)
        if (owner.getWorkspace() != null) {
            subject.setWorkspace(owner.getWorkspace());
        }
        owner.addOwnCourse(subject);
        repo.save(subject);
        return subject;
    }

    public Subject editSubjectDetails(Long id, String name, String acronym, String loggedInUserId) {
        Subject subject = getSubject(id);
        accessChecker.checkCanManageSubject(subject, loggedInUserId);
        if (name != null) {
            subject.setName(name);
        }
        if (acronym != null) {
            subject.setAcronym(acronym);
        }
        repo.save(subject);
        return subject;
    }

    public void deleteSubject(Long id, String loggedInUserId) {
        Subject subject = getSubject(id);
        accessChecker.checkCanManageSubject(subject, loggedInUserId);
        
        // Check if subject has any courses
        if (subject.getCourses() != null && !subject.getCourses().isEmpty()) {
            throw new ServiceException(ErrorConstants.SUBJECT_HAS_COURSES);
        }
        
        repo.delete(subject);
    }

    List<Subject> findCoursesOwned(String uuid) {
        return this.repo.findByOwnerId(uuid);
    }
}
