package org.trackdev.api.entity;

import org.springframework.lang.NonNull;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A sprint pattern is a template containing a set of sprint blueprints
 * that can be applied to projects within a course.
 */
@Entity
@Table(name = "sprint_patterns")
public class SprintPattern extends BaseEntityLong {

    public static final int MIN_NAME_LENGTH = 1;
    public static final int NAME_LENGTH = 100;

    @NonNull
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courseId")
    private Course course;

    @Column(name = "courseId", insertable = false, updatable = false)
    private Long courseId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "sprintPattern", orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    private List<SprintPatternItem> items = new ArrayList<>();

    public SprintPattern() {}

    public SprintPattern(String name, Course course) {
        this.name = name;
        this.course = course;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Long getCourseId() {
        return courseId;
    }

    public List<SprintPatternItem> getItems() {
        return items;
    }

    public void setItems(List<SprintPatternItem> items) {
        this.items = items;
    }

    public void addItem(SprintPatternItem item) {
        items.add(item);
        item.setSprintPattern(this);
    }

    public void removeItem(SprintPatternItem item) {
        items.remove(item);
        item.setSprintPattern(null);
    }

    public void clearItems() {
        for (SprintPatternItem item : items) {
            item.setSprintPattern(null);
        }
        items.clear();
    }
}
