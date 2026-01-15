package org.trackdev.api.entity.sprintchanges;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.trackdev.api.entity.Sprint;
import org.trackdev.api.entity.User;
import org.trackdev.api.entity.changes.EntityLogChange;

import jakarta.persistence.*;

/**
 * Base class for tracking changes to Sprints.
 * Uses single table inheritance with discriminator column for change types.
 */
@Entity
@Table(name = "sprint_changes")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class SprintChange extends EntityLogChange {

    public SprintChange() { }

    public SprintChange(User author, Sprint sprint) {
        super(author);
        this.sprint = sprint;
    }

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    @JsonIgnore
    public Sprint getSprint() { return this.sprint; }

    public Long getSprintId() { 
        try {
            return this.sprint != null ? this.sprint.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }
}