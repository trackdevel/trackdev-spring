package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.lang.NonNull;
import org.udg.trackdev.spring.serializer.JsonDateSerializer;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

@Entity
@Table(name = "iterations")
public class Iteration extends BaseEntityLong {

    public Iteration() {}

    public Iteration(String name) {
        this.name= name;
    }

    @NonNull
    private String name;

    @ManyToOne
    private Course course;

    @JsonSerialize(using = JsonDateSerializer.class)
    private Date startDate;

    @JsonSerialize(using = JsonDateSerializer.class)
    private Date endDate;

    @OneToMany(mappedBy = "iteration")
    private Collection<Sprint> sprints = new ArrayList<>();

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Collection<Sprint> getSprints() {
        return sprints;
    }

}
