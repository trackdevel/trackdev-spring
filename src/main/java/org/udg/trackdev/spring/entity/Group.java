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
@Table(name = "`groups`")
public class Group extends BaseEntityLong {

    public static final int NAME_LENGTH = 50;

    public Group() {}

    public Group(String name) {
        this.name = name;
    }

    @NotNull
    @Column(length = NAME_LENGTH)
    private String name;

    @ManyToMany(cascade = CascadeType.PERSIST)
    private Set<User> members = new HashSet<>();

    @ManyToOne
    private Courses courses;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Collection<Backlog> backlogs = new ArrayList<>();

    @JsonView(EntityLevelViews.Basic.class)
    public String getName() { return this.name; }

    public void setName(String name) { this.name = name; }

    @JsonView(EntityLevelViews.Basic.class)
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
        if(this.members.contains(user)) {
            this.members.remove(user);
        }
    }

    @JsonView( { EntityLevelViews.Basic.class, EntityLevelViews.Hierarchy.class })
    @JsonSerialize(using = JsonHierarchyViewSerializer.class)
    public Courses getCourseYear() {
        return this.courses;
    }

    public void setCourseYear(Courses courses) {
        this.courses = courses;
    }

    @JsonView(EntityLevelViews.Basic.class)
    public Collection<Backlog> getBacklogs() { return this.backlogs; }

    public void addBacklog(Backlog backlog) {
        this.backlogs.add(backlog);
    }
}
