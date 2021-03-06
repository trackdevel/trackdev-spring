package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.serializer.JsonRolesSerializer;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "invites")
public class Invite extends BaseEntityLong {

    public Invite() {}

    public Invite(String email) {
        this.email = email;
        this.state = InviteState.PENDING;
        this.createdAt = new Date();
        this.lastModified = new Date();
    }

    @NotNull
    @Column(length=User.EMAIL_LENGTH)
    private String email;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<>();

    private InviteState state;

    private Date createdAt;

    private Date lastModified;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ownerId")
    private User owner;

    @Column(name = "ownerId", insertable = false, updatable = false, length = BaseEntityUUID.UUID_LENGTH)
    private String ownerId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "courseYearId", foreignKey =
        @ForeignKey(name="course_year_foreign_key_cascade",
                    foreignKeyDefinition = "FOREIGN KEY (`courseYearId`) REFERENCES `course_years` (`id`) ON DELETE CASCADE"))
    private CourseYear courseYear;

    @Column(name = "courseYearId", insertable = false, updatable = false)
    private Long courseYearId;

    @JsonSerialize(using= JsonRolesSerializer.class)
    @JsonView(EntityLevelViews.Basic.class)
    public Set<Role> getRoles() { return roles; }

    @JsonView(EntityLevelViews.Basic.class)
    public String getEmail() {
        return email;
    }

    public void setOwner(User owner) { this.owner = owner; }

    @JsonView(EntityLevelViews.Basic.class)
    public String getOwnerId() { return ownerId; }

    public void addRole(Role role) { this.roles.add(role); }

    @JsonView(EntityLevelViews.Basic.class)
    public CourseYear getCourseYear() { return courseYear; }

    public void setCourseYear(CourseYear courseYear) { this.courseYear = courseYear; }

    @JsonView(EntityLevelViews.Basic.class)
    public InviteState getState() { return this.state; }

    public void use() {
        if(this.state == InviteState.PENDING) {
            this.state = InviteState.USED;
            this.lastModified = new Date();
        }
    }
}
