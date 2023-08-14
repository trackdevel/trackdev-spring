package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.controller.exceptions.EntityNotFound;
import org.udg.trackdev.spring.entity.Subject;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.repository.SubjectRepository;

import java.util.List;
import java.util.Optional;

@Service
public class SubjectService extends BaseServiceLong<Subject, SubjectRepository> {

    @Autowired
    UserService userService;

    @Autowired
    AccessChecker accessChecker;

    public Subject getCourse(Long id) {
        Optional<Subject> oc = this.repo.findById(id);
        if (oc.isEmpty())
            throw new EntityNotFound("Subject does not exists");
        return oc.get();
    }


    @Transactional
    public Subject createCourse(String name, String acronym, String loggedInUserId) {
        User owner = userService.get(loggedInUserId);
        accessChecker.checkCanCreateCourse(owner);
        Subject subject = new Subject(name,acronym,loggedInUserId);
        //owner.addOwnCourse(subject);
        repo.save(subject);
        return subject;
    }

    public Subject editCourseDetails(Long id, String name, String loggedInUserId) {
        Subject subject = getCourse(id);
        accessChecker.checkCanManageCourse(subject, loggedInUserId);
        subject.setName(name);
        repo.save(subject);
        return subject;
    }

    public void deleteCourse(Long id, String loggedInUserId) {
        Subject subject = getCourse(id);
        accessChecker.checkCanManageCourse(subject, loggedInUserId);
        repo.delete(subject);
    }

    //List<Subject> findCoursesOwned(String uuid)  {
    //    return this.repo.findByOwner(uuid);
    //}
}
