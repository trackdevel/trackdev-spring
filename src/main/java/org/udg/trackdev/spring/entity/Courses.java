package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "courses")
public class Courses extends BaseEntityLong {

    public Courses() {}

    public Courses(Integer startYear) {
        this.startYear = startYear;
    }

    private Integer startYear;

    @ManyToOne
    private Subject subject;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "courses", fetch = FetchType.LAZY)
    private Collection<Group> groups;

    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private Set<User> students = new HashSet<>();

    @JsonView({ EntityLevelViews.Basic.class, EntityLevelViews.Hierarchy.class })
    public Integer getStartYear() { return startYear; }

    @JsonView({ EntityLevelViews.CourseYearComplete.class, EntityLevelViews.Hierarchy.class })
    public Subject getCourse() { return this.subject; }

    public void setCourse(Subject subject) { this.subject = subject; }

    @JsonIgnore
    public Collection<Group> getGroups() { return this.groups; }

    public void addGroup(Group group) { this.groups.add(group); }

    @JsonIgnore
    public Set<User> getStudents() {  return this.students; }

    public void enrollStudent(User user) {
        this.students.add(user);
    }

    public void removeStudent(User user) {
        this.students.remove(user);
    }

    public boolean isEnrolled(User user) {
        return this.students.contains(user);
    }
}
