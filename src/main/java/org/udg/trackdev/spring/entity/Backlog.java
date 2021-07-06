package org.udg.trackdev.spring.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name = "backlogs")
public class Backlog extends BaseEntityLong {

    @ManyToOne
    private Group group;

    @OneToMany(mappedBy = "backlog", cascade = CascadeType.ALL)
    private Collection<Task> tasks = new ArrayList<>();

    public Group getGroup() {
        return this.group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
