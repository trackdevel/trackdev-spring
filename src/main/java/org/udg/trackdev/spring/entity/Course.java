package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.lang.NonNull;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(name = "courses")
public class Course extends BaseEntityLong {

    public Course() {}

    public Course(String name) {
        this.name = name;
    }

    @NonNull
    private String name;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ownerId")
    private User owner;

    @Column(name = "ownerId", insertable = false, updatable = false, length = BaseEntityUUID.UUID_LENGTH)
    private String ownerId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "course")
    private Collection<CourseYear> courseYears;

    @JsonView(EntityLevelViews.Basic.class)
    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    @JsonView(EntityLevelViews.Basic.class)
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwner(@NonNull User owner) {
        this.owner = owner;
    }

    @JsonView(EntityLevelViews.CourseComplete.class)
    public Collection<CourseYear> getCourseYears() {
        return this.courseYears;
    }

    public void addCourseYear(CourseYear courseYear) { this.courseYears.add(courseYear); }
}
