package org.udg.trackdev.spring.entity.sprintchanges;

import org.udg.trackdev.spring.entity.changes.EntityLogChange;

import javax.persistence.*;

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