package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(name = "courses")
public class Course extends BaseEntityLong {

    public static final int MIN_START_YEAR = 1900;
    public static final int MAX_START_YEAR = 9999;


    public Course() {}

    public Course(Integer startYear) {
        this.startYear = startYear;
    }

    private Integer startYear;

    private String githubOrganization;

    @ManyToOne
    private Subject subject;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "course", fetch = FetchType.LAZY)
    private Collection<Project> projects;

    @JsonView({ EntityLevelViews.Basic.class, EntityLevelViews.Hierarchy.class })
    public Integer getStartYear() { return startYear; }

    public void setStartYear(Integer startYear) { this.startYear = startYear; }

    @JsonView({ EntityLevelViews.CourseComplete.class, EntityLevelViews.Hierarchy.class })
    public Subject getSubject() { return this.subject; }

    public void setSubject(Subject subject) { this.subject = subject; }

    @JsonIgnore
    public Collection<Project> getProjects() { return this.projects; }

    public void addProject(Project project) { this.projects.add(project); }

    @JsonView({ EntityLevelViews.Basic.class, EntityLevelViews.Hierarchy.class })
    public String getGithubOrganization() { return this.githubOrganization; }

    public void setGithubOrganization(String githubOrganization) { this.githubOrganization = githubOrganization; }

}
