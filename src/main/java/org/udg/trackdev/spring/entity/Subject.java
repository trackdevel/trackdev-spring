package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.lang.NonNull;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(name = "subjects")
public class Subject extends BaseEntityLong {

    public static final int NAME_LENGTH = 50;

    @NonNull
    @Column(length = NAME_LENGTH)
    private String name;

    @Column(name = "ownerId", insertable = false, updatable = false, length = BaseEntityUUID.UUID_LENGTH)
    private String ownerId;

    @NonNull
    private String acronym;

    public Subject() {}

    public Subject(String name, String acronym, String ownerId  ) {
        this.name = name;
        this.acronym = acronym;
        this.ownerId = ownerId;
    }

    @JsonView({ EntityLevelViews.Basic.class, EntityLevelViews.Hierarchy.class })
    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    @JsonView(EntityLevelViews.Basic.class)
    public String getOwnerId() {
        return ownerId;
    }

    @JsonView(EntityLevelViews.Basic.class)
    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }
}
