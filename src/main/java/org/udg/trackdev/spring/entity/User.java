package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.udg.trackdev.spring.serializer.JsonRolesSerializer;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;

@Entity
@Table(name = "users")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = User.class)
public class User extends BaseEntityUUID {

  public User() {
  }

  public User(String username, String email, String password) {
    this.username = username;
    this.email = email;
    this.password = password;
  }

  @NotNull
  @Column(unique=true, length=12)
  private String username;

  @NotNull
  @Column(unique=true, length=128)
  private String email;

  @NotNull
  private String password;

  private Date lastLogin;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
  private Collection<Course> coursesOwns = new ArrayList<>();

  @ManyToMany
  private Collection<Group> groups = new ArrayList<>();

  @ManyToMany()
  private Set<Role> roles = new HashSet<>();

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "owner")
  private Collection<Invite> invites = new ArrayList<>();

  @JsonView(Views.Private.class)
  public String getId() {
    return super.getId();
  }

  @JsonView(Views.Private.class)
  public String getEmail() {
    return email;
  }

  @JsonView(Views.Public.class)
  public String getUsername() {
    return username;
  }

  @JsonIgnore
  public String getPassword() {
    return password;
  }

  @JsonView(Views.Private.class)
  @JsonSerialize(using= JsonRolesSerializer.class)
  public Set<Role> getRoles() { return roles; }

  public void addRole(Role role) {
    roles.add(role);
  }

  public Date getLastLogin() {
    return lastLogin;
  }

  public void setLastLogin(Date lastLogin) {
    this.lastLogin = lastLogin;
  }

  public void addOwnCourse(Course course) { coursesOwns.add(course); }

  public void addToGroup(Group group) {
    this.groups.add(group);
  }

  public void addInvite(Invite invite) { this.invites.add(invite); }
}
