package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonView;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;

import javax.persistence.Entity;

@Entity
public class TaskNameChange extends TaskChange {

    public TaskNameChange() {}

    public TaskNameChange(User author, Task task, String name) {
        super(author, task);
        this.name = name;
    }

    private String name;

    @JsonView(EntityLevelViews.Basic.class)
    public String getName() { return this.name; }
}