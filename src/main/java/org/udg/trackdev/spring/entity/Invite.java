package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonView;
import org.udg.trackdev.spring.configuration.UserType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "invites")
public class Invite extends BaseEntityLong {

    public Invite() {}

    public Invite(UserType TType) {
        this.TType = TType;
    }

    @NotNull
    private String email;

    @NotNull
    private UserType TType;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ownerId")
    private User owner;

    @Column(name = "ownerId", insertable = false, updatable = false)
    private String ownerId;

    @JsonView(Views.Public.class)
    public UserType getUserType() {
      return TType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setOwner(User owner) { this.owner = owner; }

    public String getOwnerId() { return ownerId; }
}
