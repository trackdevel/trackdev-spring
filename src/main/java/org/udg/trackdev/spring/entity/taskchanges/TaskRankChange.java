package org.udg.trackdev.spring.entity.taskchanges;

import com.fasterxml.jackson.annotation.JsonView;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = TaskRankChange.CHANGE_TYPE_NAME)
public class TaskRankChange extends TaskChange {

    public static final String CHANGE_TYPE_NAME = "rank_change";

    public TaskRankChange() { }

    public TaskRankChange(User user, Task task, Integer rank) {
        super(user, task);
        this.rank = rank;
    }

    @Column(name = "`rank`")
    private Integer rank;

    @JsonView(EntityLevelViews.Basic.class)
    public Integer getRank() { return rank; }

    @Override
    public String getType() {
        return CHANGE_TYPE_NAME;
    }
}
