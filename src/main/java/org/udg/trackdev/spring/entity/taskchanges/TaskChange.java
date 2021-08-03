package org.udg.trackdev.spring.entity.taskchanges;

import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.entity.changes.EntityLogChange;

import javax.persistence.*;

@Entity
@Table(name = "task_changes")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class TaskChange extends EntityLogChange<Task> {

    public TaskChange() { }

    public TaskChange(User author, Task task) {
        super(author, task);
    }
}
