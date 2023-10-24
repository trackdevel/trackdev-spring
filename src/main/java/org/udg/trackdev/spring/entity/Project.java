package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.serializer.JsonHierarchyViewSerializer;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.NotNull;

@Entity
@Table(name = "projects")
public class Project extends BaseEntityLong {

    public static final int NAME_LENGTH = 50;

    public static final boolean DEFAULT_CURRENT = false;

    @NotNull
    @Column(length = NAME_LENGTH)
    private String name;

    @NotNull
    private boolean current;

    @ManyToMany(cascade = CascadeType.PERSIST)
    private Set<User> members = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "courseId")
    private Course course;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Collection<Task> tasks = new ArrayList<>();

    public Project() {}

    public Project(String name) {
        this.name = name;
        this.current = DEFAULT_CURRENT;
    }

    @JsonView({EntityLevelViews.Basic.class})
    public String getName() { return this.name; }

    public void setName(String name) { this.name = name; }

    @JsonView({EntityLevelViews.Basic.class})
    public boolean isCurrent() { return this.current; }

    public void setCurrent(boolean current) {
        this.current = current;
    }

    @JsonView(EntityLevelViews.ProjectComplete.class)
    public Set<User> getMembers() { return this.members; }

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

    @JsonView( { EntityLevelViews.ProjectComplete.class, EntityLevelViews.Hierarchy.class })
    @JsonSerialize(using = JsonHierarchyViewSerializer.class)
    public Course getCourse() {
        return this.course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    @JsonView({EntityLevelViews.ProjectComplete.class})
    public Collection<Task> getTasks() { return this.tasks; }

    public void addTask(Task task) {
        this.tasks.add(task);
    }
}
