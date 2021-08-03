package org.udg.trackdev.spring.entity.changes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import org.udg.trackdev.spring.entity.BaseEntityLong;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.service.Global;

import javax.persistence.*;
import java.time.LocalDateTime;

@MappedSuperclass
public abstract class EntityLogChange<T> extends BaseEntityLong {

    public EntityLogChange() { }

    public EntityLogChange(User author, T entity) {
        this.author = author;
        this.entity = entity;
        this.changedAt = LocalDateTime.now();
    }

    @ManyToOne
    private User author;

    @ManyToOne
    @JoinColumn(name = "entityId")
    private T entity;

    // Used for specifications
    @Column(name = "entityId", insertable = false, updatable = false)
    private Long entityId;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime changedAt;

    // Used for specifications
    @Column(name = "type", insertable = false, updatable = false)
    private String type;

    @JsonView(EntityLevelViews.Basic.class)
    public User getAuthor() { return this.author; }

    @JsonIgnore
    public T getEntity() { return this.entity; }

    @JsonView(EntityLevelViews.Basic.class)
    @JsonFormat(pattern = Global.SIMPLE_DATE_FORMAT)
    public LocalDateTime getChangedAt() { return this.changedAt; }

    @JsonView(EntityLevelViews.Basic.class)
    public abstract String getType();
}