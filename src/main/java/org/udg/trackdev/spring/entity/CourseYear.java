package org.udg.trackdev.spring.entity;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(name = "course_years")
public class CourseYear extends BaseEntityLong {

    public CourseYear() {}

    public CourseYear(Integer startYear, Integer endYear) {
        this.startYear = startYear;
        this.endYear = endYear;
    }

    private Integer startYear;

    private Integer endYear;

    public Integer getStartYear() { return startYear; }

    public Integer getEndYear() { return endYear; }

    @ManyToOne
    private Course course;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "courseYear")
    private Collection<Group> groups;

    @OneToMany(mappedBy = "courseYear")
    private Collection<Iteration> iterations;

    public void setCourse(Course course) { this.course = course; }

    public void addGroup(Group group) { this.groups.add(group); }

    public void addIteration(Iteration iteration) {
        this.iterations.add(iteration);
    }
}
