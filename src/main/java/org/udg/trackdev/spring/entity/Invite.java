package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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

    @ManyToMany()
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

    @JsonView(Views.Public.class)
    @JsonSerialize(using= JsonRolesSerializer.class)
    public Set<Role> getRoles() { return roles; }

    public String getEmail() {
        return email;
    }

    public void setOwner(User owner) { this.owner = owner; }

    public String getOwnerId() { return ownerId; }

    public void addRole(Role role) { this.roles.add(role); }

    public InviteState getState() { return this.state; }

    public void use() {
        if(this.state == InviteState.PENDING) {
            this.state = InviteState.USED;
            this.lastModified = new Date();
        }
    }
}
