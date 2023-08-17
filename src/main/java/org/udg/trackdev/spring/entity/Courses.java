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

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "course", fetch = FetchType.LAZY)
    private Collection<Project> projects;

    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private Set<User> students = new HashSet<>();

    @JsonView({ EntityLevelViews.Basic.class, EntityLevelViews.Hierarchy.class })
    public Integer getStartYear() { return startYear; }

    @JsonView({ EntityLevelViews.CourseYearComplete.class, EntityLevelViews.Hierarchy.class })
    public Subject getSubject() { return this.subject; }

    public void setSubject(Subject subject) { this.subject = subject; }

    @JsonIgnore
    public Collection<Project> getProjects() { return this.projects; }

    public void addProject(Project project) { this.projects.add(project); }

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
