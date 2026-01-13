package org.trackdev.api.entity;

import org.springframework.lang.NonNull;

import jakarta.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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

    @Column(length = 5)
    private String language = "en"; // Default language: English

    @ManyToOne
    private Subject subject;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;

    @Column(name = "owner_id", insertable = false, updatable = false, length = BaseEntityUUID.UUID_LENGTH)
    private String ownerId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "course", fetch = FetchType.LAZY)
    private Set<Project> projects = new HashSet<>();

    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinTable(
        name = "courses_students",
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private Set<User> students = new HashSet<>();

    public Integer getStartYear() { return startYear; }

    public void setStartYear(Integer startYear) { this.startYear = startYear; }

    public Subject getSubject() { return this.subject; }

    public void setSubject(Subject subject) { this.subject = subject; }

    public Collection<Project> getProjects() { return this.projects; }

    public void addProject(Project project) { this.projects.add(project); }

    public String getGithubOrganization() { return this.githubOrganization; }

    public void setGithubOrganization(String githubOrganization) { this.githubOrganization = githubOrganization; }

    public String getLanguage() { return this.language; }

    public void setLanguage(String language) { this.language = language; }

    public User getOwner() { return this.owner; }

    public void setOwner(@NonNull User owner) { this.owner = owner; }

    public String getOwnerId() { return this.ownerId; }

    public Set<User> getStudents() { return this.students; }

    public void addStudent(User student) { this.students.add(student); }

    public void removeStudent(User student) { this.students.remove(student); }

    public boolean isStudentEnrolled(User student) { return this.students.contains(student); }

    public boolean isStudentEnrolled(String userId) {
        return this.students.stream().anyMatch(s -> s.getId().equals(userId));
    }

}
