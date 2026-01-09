package org.trackdev.api.entity.sprintchanges;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

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

    public String getName() {
        return this.name;
    }
}
