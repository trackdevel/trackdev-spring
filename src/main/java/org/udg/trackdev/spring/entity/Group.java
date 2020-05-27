package org.udg.trackdev.spring.entity;

import lombok.NonNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "groups")
public class Group extends BaseEntityLong {

    public Group() {}

    public Group(String name) {
        this.name = name;
    }

    @NonNull
    private String name;

    @ManyToMany(cascade = CascadeType.PERSIST, mappedBy = "groups")
    private Set<User> members = new HashSet<>();

    @ManyToOne
    private Course course;

    @OneToMany(mappedBy = "group")
    private Collection<Sprint> sprints = new ArrayList<>();

    public void addMember(User member) { this.members.add(member); }

    public void setCourse(Course course) {
        this.course = course;
    }
}
