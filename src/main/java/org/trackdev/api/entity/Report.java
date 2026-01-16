package org.trackdev.api.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.springframework.lang.NonNull;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "reports")
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id"
)
public class Report extends BaseEntityLong {

    public static final int MIN_NAME_LENGTH = 1;
    public static final int NAME_LENGTH = 200;

    @NonNull
    @Column(length = NAME_LENGTH)
    private String name;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    private Date createdAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ReportAxisType rowType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ReportAxisType columnType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ReportElement element;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ReportMagnitude magnitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    // Constructors
    public Report() {
        this.element = ReportElement.TASK; // Default to TASK
    }

    public Report(@NonNull String name, User owner) {
        this.name = name;
        this.owner = owner;
        this.createdAt = new Date();
    }

    // Getters and Setters
    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public ReportAxisType getRowType() {
        return rowType;
    }

    public void setRowType(ReportAxisType rowType) {
        this.rowType = rowType;
    }

    public ReportAxisType getColumnType() {
        return columnType;
    }

    public void setColumnType(ReportAxisType columnType) {
        this.columnType = columnType;
    }

    public ReportElement getElement() {
        return element;
    }

    public void setElement(ReportElement element) {
        this.element = element;
    }

    public ReportMagnitude getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(ReportMagnitude magnitude) {
        this.magnitude = magnitude;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }
}
