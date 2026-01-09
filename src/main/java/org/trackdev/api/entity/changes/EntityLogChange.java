package org.trackdev.api.entity.changes;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;

import org.trackdev.api.configuration.DateFormattingConfiguration;
import org.trackdev.api.entity.BaseEntityLong;

@MappedSuperclass
public abstract class EntityLogChange extends BaseEntityLong {

    public EntityLogChange() { }

    public EntityLogChange(String author, Long entityid) {
        this.author = author;
        this.entityid = entityid;
        this.changedAt = LocalDateTime.now();
    }

    private String author;

    private Long entityid;

    // Used for specifications
    @Column(name = "entityId", insertable = false, updatable = false)
    private Long entityId;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime changedAt;

    // Used for specifications
    @Column(name = "type", insertable = false, updatable = false)
    private String type;

    public String getAuthor() { return this.author; }

    public Long getEntity() { return this.entityid; }

    @JsonFormat(pattern = DateFormattingConfiguration.SIMPLE_DATE_FORMAT)
    public LocalDateTime getChangedAt() { return this.changedAt; }

    public abstract String getType();
}