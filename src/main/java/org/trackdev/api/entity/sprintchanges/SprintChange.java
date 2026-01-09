package org.trackdev.api.entity.sprintchanges;

import org.trackdev.api.entity.changes.EntityLogChange;

import jakarta.persistence.*;

@Entity
@Table(name = "sprint_changes")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class SprintChange extends EntityLogChange {

    public SprintChange() { }

    public SprintChange(String author, Long sprint) {
        super(author, sprint);
    }
}