package org.udg.trackdev.spring.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name = "backlogs")
public class Backlog extends BaseEntityLong {

    @ManyToOne
    private Group group;

    @OneToMany(mappedBy = "backlog")
    private Collection<Task> tasks = new ArrayList<>();

    public void setGroup(Group group) {
        this.group = group;
    }
}
