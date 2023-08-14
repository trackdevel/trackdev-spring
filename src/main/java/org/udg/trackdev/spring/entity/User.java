package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.udg.trackdev.spring.configuration.UserType;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.entity.views.PrivacyLevelViews;
import org.udg.trackdev.spring.serializer.JsonRolesSerializer;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "users")
public class User extends BaseEntityUUID {

  public static final int USERNAME_LENGTH = 12;
  public static final int EMAIL_LENGTH = 128;

  public User() {
  }

  public User(String username, String email, String password) {
    this.username = username;
    this.email = email;
    this.password = password;
  }

  @NotNull
  @Column(unique=true, length=USERNAME_LENGTH)
  private String username;

  @NotNull
  @Column(unique=true, length=EMAIL_LENGTH)
  private String email;

  @NotNull
  private String password;

  private LocalDateTime lastLogin;

  //@OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
  //private Collection<Subject> coursesOwns = new ArrayList<>();

  @ManyToMany(mappedBy = "members")
  private Collection<Group> groups = new ArrayList<>();

  @ManyToMany(mappedBy = "students")
  private Collection<Courses> courses = new ArrayList<>();

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
  private Collection<Subject> coursesOwns = new ArrayList<>();

  @ManyToMany()
  private Set<Role> roles = new HashSet<>();

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "owner")
  private Collection<Invite> invites = new ArrayList<>();

  private String githubName;

  /**APARTIR D'AQUI SON MODIFICACIONS PER EL TFG**/

  private String nicename;

  private Boolean changePassword;

  private Boolean enabled;

  /**********************************/

  @JsonView(PrivacyLevelViews.Private.class)
  public String getId() {
    return super.getId();
  }

  @JsonView(PrivacyLevelViews.Private.class)
  public String getEmail() {
    return email;
  }

  @JsonView({PrivacyLevelViews.Public.class, EntityLevelViews.Basic.class})
  public String getUsername() {
    return username;
  }

  @JsonIgnore
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @JsonView(PrivacyLevelViews.Private.class)
  @JsonSerialize(using= JsonRolesSerializer.class)
  public Set<Role> getRoles() { return roles; }

  /** COSES NOVES **/

  @JsonView(PrivacyLevelViews.Public.class)
  public String nicename() { return nicename; }

  public void setNicename(String nicename) { this.nicename = nicename; }

  @JsonView(PrivacyLevelViews.Public.class)
  public Boolean getChangePassword() { return changePassword; }

  public void setChangePassword(Boolean changePassword) { this.changePassword = changePassword; }

  @JsonView(PrivacyLevelViews.Public.class)
  public Boolean getEnabled() { return enabled; }

  public void setEnabled(Boolean enabled) { this.enabled = enabled; }

  /***********/

  public void addRole(Role role) {
    roles.add(role);
  }

  public boolean isUserType(UserType userType) {
    boolean inRole = false;
    for(Role role: roles) {
      if(role.getUserType() == userType) {
        inRole = true;
        break;
      }
    }
    return inRole;
  }

  @JsonIgnore
  public LocalDateTime getLastLogin(){ return lastLogin; }

  public void setLastLogin(LocalDateTime lastLogin) {
    this.lastLogin = lastLogin;
  }

  public void addOwnCourse(Subject subject) { coursesOwns.add(subject); }

  public void addToGroup(Group group) {
    this.groups.add(group);
  }

  public void removeFromGroup(Group group) {
    if(this.groups.contains(group)) {
      this.groups.remove(group);
    }
  }

  public void enrollToCourseYear(Courses courses) { this.courses.add(courses); }

  public void removeFromCourseYear(Courses courses) { this.courses.remove(courses); }

  public void addInvite(Invite invite) { this.invites.add(invite); }

  @JsonIgnore
  public Collection<Courses> getEnrolledCourseYears() { return this.courses; }
}
