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
    private Project project;

    @OneToMany(mappedBy = "backlog", cascade = CascadeType.ALL)
    private Collection<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "backlog", cascade = CascadeType.ALL)
    private Collection<Sprint> sprints = new ArrayList<>();

    @JsonView(EntityLevelViews.Hierarchy.class)
    @JsonSerialize(using = JsonHierarchyViewSerializer.class)
    public Project getGroup() {
        return this.project;
    }

    public void setGroup(Project project) {
        this.project = project;
    }

    public Collection<Sprint> getSprints() {
        return sprints;
    }

    public void addSprint(Sprint sprint) {
        sprints.add(sprint);
    }
}
