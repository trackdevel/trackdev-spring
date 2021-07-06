package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.udg.trackdev.spring.configuration.UserType;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.entity.views.PrivacyLevelViews;
import org.udg.trackdev.spring.serializer.JsonRolesSerializer;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
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

  private Date lastLogin;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
  private Collection<Course> coursesOwns = new ArrayList<>();

  @ManyToMany(mappedBy = "members")
  private Collection<Group> groups = new ArrayList<>();

  @ManyToMany(mappedBy = "students")
  private Collection<CourseYear> courseYears = new ArrayList<>();

  @ManyToMany()
  private Set<Role> roles = new HashSet<>();

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "owner")
  private Collection<Invite> invites = new ArrayList<>();

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

  @JsonView(PrivacyLevelViews.Private.class)
  @JsonSerialize(using= JsonRolesSerializer.class)
  public Set<Role> getRoles() { return roles; }

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

  public void removeFromGroup(Group group) {
    if(this.groups.contains(group)) {
      this.groups.remove(group);
    }
  }

  public void enrollToCourseYear(CourseYear courseYear) { this.courseYears.add(courseYear); }

  public void removeFromCourseYear(CourseYear courseYear) { this.courseYears.remove(courseYear); }

  public void addInvite(Invite invite) { this.invites.add(invite); }

  @JsonIgnore
  public Collection<CourseYear> getEnrolledCourseYears() { return this.courseYears; }
}
