package org.udg.trackdev.spring.entity.sprintchanges;

import com.fasterxml.jackson.annotation.JsonView;
import org.udg.trackdev.spring.entity.Sprint;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = SprintNameChange.CHANGE_TYPE_NAME)
public class SprintNameChange extends SprintChange {
    public static final String CHANGE_TYPE_NAME = "name_change";

    public SprintNameChange() {}

    public SprintNameChange(String author, Long sprint, String name) {
        super(author, sprint);
        this.name = name;
    }

    private String name;

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }

    @JsonView(EntityLevelViews.Basic.class)
    public String getName() {
        return this.name;
    }
}
