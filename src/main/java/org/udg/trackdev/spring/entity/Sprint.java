package org.udg.trackdev.spring.entity;

import org.springframework.lang.NonNull;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "sprints")
public class Sprint extends BaseEntityLong {

    public Sprint() {}

    public Sprint(String name) {
        this.name = name;
    }

    @NonNull
    private String name;

    @ManyToOne
    private Iteration iteration;

    @ManyToOne
    private Group group;

    @OneToMany(mappedBy = "activeSprint")
    private Collection<Task> activeTasks;

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public Iteration getIteration() {
        return iteration;
    }

    public void setIteration(Iteration iteration) {
        this.iteration = iteration;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Collection<Task> getActiveTasks() {
        return activeTasks;
    }

}
