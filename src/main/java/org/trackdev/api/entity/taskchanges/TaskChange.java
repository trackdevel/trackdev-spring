package org.trackdev.api.entity.taskchanges;

import org.trackdev.api.entity.changes.EntityLogChange;

import jakarta.persistence.*;

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
