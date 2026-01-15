package org.trackdev.api.entity;

import org.springframework.lang.NonNull;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name = "workspaces")
public class Workspace extends BaseEntityLong {

    public static final int MIN_NAME_LENGTH = 1;
    public static final int NAME_LENGTH = 100;

    @NonNull
    @Column(length = NAME_LENGTH)
    private String name;

    @OneToMany(mappedBy = "workspace")
    private Collection<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "workspace")
    private Collection<Subject> subjects = new ArrayList<>();

    public Workspace() {}

    public Workspace(String name) {
        this.name = name;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public Collection<User> getUsers() {
        return users;
    }

    public void addUser(User user) {
        this.users.add(user);
        user.setWorkspace(this);
    }

    public void removeUser(User user) {
        this.users.remove(user);
        user.setWorkspace(null);
    }

    public Collection<Subject> getSubjects() {
        return subjects;
    }

    public void addSubject(Subject subject) {
        this.subjects.add(subject);
        subject.setWorkspace(this);
    }

    public void removeSubject(Subject subject) {
        this.subjects.remove(subject);
        subject.setWorkspace(null);
    }
}
