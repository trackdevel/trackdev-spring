package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonView;
import org.hibernate.annotations.GenericGenerator;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;

import javax.persistence.*;

@MappedSuperclass
public abstract class BaseEntityUUID {
    public static final int UUID_LENGTH = 36;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(length = UUID_LENGTH)
    private String id;

    @JsonView({EntityLevelViews.Basic.class, EntityLevelViews.Hierarchy.class})
    public String getId() {
        return id;
    }
}