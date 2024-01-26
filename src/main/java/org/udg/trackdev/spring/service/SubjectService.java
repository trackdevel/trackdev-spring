package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.controller.exceptions.EntityNotFound;
import org.udg.trackdev.spring.entity.Subject;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.repository.SubjectRepository;
import org.udg.trackdev.spring.utils.ErrorConstants;

import java.util.List;
import java.util.Optional;

@Service
public class SubjectService extends BaseServiceLong<Subject, SubjectRepository> {

    @Autowired
    UserService userService;

    @Autowired
    AccessChecker accessChecker;

    public Subject getSubject(Long id) {
        Optional<Subject> oc = this.repo.findById(id);
        if (oc.isEmpty())
            throw new EntityNotFound(ErrorConstants.SUBJECT_NOT_EXIST);
        return oc.get();
    }


    @Transactional
    public Subject createSubject(String name, String acronym, String loggedInUserId) {
        User owner = userService.get(loggedInUserId);
        accessChecker.checkCanCreateSubject(owner);
        Subject  subject = new Subject(name,acronym, owner);
        owner.addOwnCourse(subject);
        repo.save(subject);
        return subject;
    }

    public Subject editSubjectDetails(Long id, String name, String acronym, String loggedInUserId) {
        Subject subject = getSubject(id);
        accessChecker.checkCanManageSubject(subject, loggedInUserId);
        subject.setName(name);
        subject.setAcronym(acronym);
        repo.save(subject);
        return subject;
    }

    public void deleteSubject(Long id, String loggedInUserId) {
        Subject subject = getSubject(id);
        accessChecker.checkCanManageSubject(subject, loggedInUserId);
        repo.delete(subject);
    }

    List<Subject> findCoursesOwned(String uuid) {
        return this.repo.findByOwner(uuid);
    }
}
