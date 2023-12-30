package org.udg.trackdev.spring.entity.taskchanges;

import org.udg.trackdev.spring.entity.changes.EntityLogChange;

import javax.persistence.*;

@Entity
@Table(name = "task_changes")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class TaskChange extends EntityLogChange {

    public TaskChange() { }

    public TaskChange(String author, Long entityId) {
        super(author, entityId);
    }
}
