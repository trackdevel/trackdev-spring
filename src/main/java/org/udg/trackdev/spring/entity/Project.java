package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.serializer.JsonHierarchyViewSerializer;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
public class Project extends BaseEntityLong {

   //-- CONSTANTS

    public static final int NAME_LENGTH = 50;

    //-- ATTRIBUTES

    @NotNull
    @Column(length = NAME_LENGTH)
    private String name;

    @ManyToOne
    @JoinColumn(name = "courseId")
    private Course course;

    @ManyToMany(cascade = CascadeType.PERSIST)
    private Set<User> members = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Collection<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Collection<Sprint> sprints = new ArrayList<>();

    @Max(10)
    private Double qualification;

    //--- CONSTRUCTOR

    public Project() {}

    public Project(String name) {
        this.name = name;
    }

    //--- GETTERS AND SETTERS

    @JsonView({EntityLevelViews.Basic.class})
    public String getName() { return this.name; }

    public void setName(String name) { this.name = name; }

    @JsonView({EntityLevelViews.ProjectWithUser.class, EntityLevelViews.TaskWithProjectMembers.class})
    public Set<User> getMembers() { return this.members; }

    @JsonView( { EntityLevelViews.Basic.class, EntityLevelViews.Hierarchy.class })
    @JsonSerialize(using = JsonHierarchyViewSerializer.class)
    public Course getCourse() {
        return this.course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    @JsonView({EntityLevelViews.ProjectComplete.class})
    public Collection<Task> getTasks() {
        Collection<Task> mainTasks = new ArrayList<>();
        this.tasks.stream().filter(task -> task.getParentTask() == null).forEach(mainTasks::add);
        return mainTasks;
    }

    @JsonView({EntityLevelViews.ProjectComplete.class})
    public Collection<Sprint> getSprints() {
        return this.sprints;
    }

    @JsonView({EntityLevelViews.Basic.class})
    public Double getQualification() { return this.qualification; }
    public void setQualification(Double qualification) { this.qualification = qualification; }

    //--- METHODS

    public void addMember(User member) { this.members.add(member); }

    public boolean isMember(User user) {
        return this.members.contains(user);
    }

    public boolean isMember(String userId) {
        for(User user: this.members) {
            if(user.getId().equals(userId)) {
                return true;
            }
        }
        return false;
    }

    public void removeMember(User user) {
        this.members.remove(user);
    }

    public void addTask(Task task) {
        this.tasks.add(task);
    }

    public void addSprint(Sprint sprint) {
        this.sprints.add(sprint);
    }

}
