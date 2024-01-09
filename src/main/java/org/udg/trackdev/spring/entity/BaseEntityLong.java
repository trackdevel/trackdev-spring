package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonView;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseEntityLong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonView({ EntityLevelViews.Basic.class, EntityLevelViews.Hierarchy.class})
    public Long getId() { return id; }
}