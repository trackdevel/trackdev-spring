package org.trackdev.api.entity.taskchanges;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.trackdev.api.entity.Task;
import org.trackdev.api.entity.User;
import org.trackdev.api.entity.changes.EntityLogChange;

import jakarta.persistence.*;

/**
 * Base class for tracking changes to Tasks.
 * Uses single table inheritance with discriminator column for change types.
 */
@Entity
@Table(name = "task_changes")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class TaskChange extends EntityLogChange {

    public TaskChange() { }

    public TaskChange(User author, Task task) {
        super(author);
        this.task = task;
    }

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @JsonIgnore
    public Task getTask() { return this.task; }

    public Long getTaskId() { 
        try {
            return this.task != null ? this.task.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get the old value for this change (for serialization)
     */
    public abstract String getOldValue();

    /**
     * Get the new value for this change (for serialization)
     */
    public abstract String getNewValue();
}
