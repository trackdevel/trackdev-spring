package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "course_years")
public class CourseYear extends BaseEntityLong {

    public CourseYear() {}

    public CourseYear(Integer startYear) {
        this.startYear = startYear;
    }

    private Integer startYear;

    @ManyToOne
    private Course course;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "courseYear", fetch = FetchType.LAZY)
    private Collection<Group> groups;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "courseYear", fetch = FetchType.LAZY)
    private Collection<Iteration> iterations;

    @ManyToMany(cascade = CascadeType.PERSIST, mappedBy = "courseYears", fetch = FetchType.LAZY)
    private Set<User> students = new HashSet<>();

    @JsonView(EntityLevelViews.Basic.class)
    public Integer getStartYear() { return startYear; }

    @JsonView(EntityLevelViews.CourseYearComplete.class)
    public Course getCourse() { return this.course; }

    public void setCourse(Course course) { this.course = course; }

    public void addGroup(Group group) { this.groups.add(group); }

    public void addIteration(Iteration iteration) {
        this.iterations.add(iteration);
    }

    @JsonIgnore
    public Set<User> getStudents() {  return this.students; }

    public void enrollStudent(User user) {
        this.students.add(user);
    }

    public void removeStudent(User user) {
        this.students.remove(user);
    }
}
