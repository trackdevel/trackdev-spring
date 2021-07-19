package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.serializer.JsonHierarchyViewSerializer;

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

    @JsonView(EntityLevelViews.Hierarchy.class)
    @JsonSerialize(using = JsonHierarchyViewSerializer.class)
    public Group getGroup() {
        return this.group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
