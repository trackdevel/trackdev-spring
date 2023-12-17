package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.udg.trackdev.spring.configuration.UserType;
import org.udg.trackdev.spring.entity.views.EntityLevelViews;
import org.udg.trackdev.spring.entity.views.PrivacyLevelViews;
import org.udg.trackdev.spring.serializer.JsonDateSerializer;
import org.udg.trackdev.spring.serializer.JsonRolesSerializer;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;

@Entity
@Table(name = "users")
public class User extends BaseEntityUUID {

  public static final int USERNAME_LENGTH = 50;
  public static final int EMAIL_LENGTH = 128;
  public static final int CAPITAL_LETTERS_LENGTH = 2;
  public static final int RECOVERY_CODE_LENGTH = 8;

  public User() {}

  public User(String username, String email, String password) {
    this.username = username;
    this.email = email;
    this.password = password;
    this.color = randomColorGenerator();
    this.githubInfo = new GithubInfo();
    this.capitalLetters = generateCapitalLetters(username);
  }

  @NotNull
  @Column(length=USERNAME_LENGTH)
  private String username;

  @NotNull
  @Column(unique=true, length=EMAIL_LENGTH)
  private String email;

  @NotNull
  private String password;

  @JsonView(EntityLevelViews.Basic.class)
  @JsonSerialize(using = JsonDateSerializer.class)
  private Date lastLogin;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
  private Collection<Subject> subjectsOwns = new ArrayList<>();

  @ManyToMany(mappedBy = "members")
  private Collection<Project> projects = new ArrayList<>();

  @ManyToMany()
  private Set<Role> roles = new HashSet<>();

  private String color;

  @Size(min = CAPITAL_LETTERS_LENGTH, max = CAPITAL_LETTERS_LENGTH)
  private String capitalLetters;

  private Long currentProject;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "author", fetch = FetchType.LAZY)
  private final Collection<Comment> comments = new ArrayList<>();

  @NotNull
  private Boolean changePassword;

  @NotNull
  private Boolean enabled;

  private String recoveryCode;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "githubInfoId", referencedColumnName = "id")
  private GithubInfo githubInfo;

 // -- GETTERS AND SETTERS

  @JsonView({PrivacyLevelViews.Private.class, EntityLevelViews.Basic.class, EntityLevelViews.TaskWithProjectMembers.class})
  public String getId() {
    return super.getId();
  }

  @JsonView({PrivacyLevelViews.Private.class, EntityLevelViews.Basic.class, EntityLevelViews.TaskWithProjectMembers.class})
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @JsonView({PrivacyLevelViews.Public.class, EntityLevelViews.Basic.class, EntityLevelViews.TaskWithProjectMembers.class})
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
    this.capitalLetters = generateCapitalLetters(username);
  }

  @JsonView({PrivacyLevelViews.Public.class, EntityLevelViews.Basic.class})
  public Long getCurrentProject() { return currentProject; }

  public void setCurrentProject(Long currentProject) { this.currentProject = currentProject; }

  @JsonIgnore
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @JsonIgnore
  public String getRecoveryCode() { return recoveryCode; }

  public void setRecoveryCode(String recoveryCode) { this.recoveryCode = recoveryCode; }

  @JsonView({PrivacyLevelViews.Private.class, EntityLevelViews.Basic.class})
  @JsonSerialize(using= JsonRolesSerializer.class)
  public Set<Role> getRoles() { return roles; }

  @JsonView({PrivacyLevelViews.Private.class, EntityLevelViews.Basic.class, EntityLevelViews.TaskWithProjectMembers.class})
  public String getColor() { return color; }

  public String setColor(String color) { return this.color = color; }

  @JsonView({PrivacyLevelViews.Private.class, EntityLevelViews.Basic.class, EntityLevelViews.TaskWithProjectMembers.class})
  public String getCapitalLetters() { return capitalLetters; }

  @JsonView({PrivacyLevelViews.Private.class, EntityLevelViews.UserWithoutProjectMembers.class})
  public Collection<Project> getProjects() {
    return projects;
  }

  @JsonView({PrivacyLevelViews.Public.class, EntityLevelViews.Basic.class, EntityLevelViews.TaskWithProjectMembers.class})
  public GithubInfo getGithubInfo() { return githubInfo; }

  public String setGithubToken(String githubToken) { return githubInfo.setGithubToken(githubToken); }

  public void setGithubName(String login) { githubInfo.setLogin(login); }

  public void setGithubAvatar(String githubAvatar) { githubInfo.setAvatar_url(githubAvatar); }

  public void setGithubHtmlUrl(String githubHtmlUrl) { githubInfo.setHtml_url(githubHtmlUrl); }

  public String setCapitalLetters(String capitalLetters) { return this.capitalLetters = capitalLetters; }

  @JsonView({PrivacyLevelViews.Public.class, EntityLevelViews.Basic.class})
  public Boolean getChangePassword() { return changePassword; }

  public void setChangePassword(Boolean changePassword) { this.changePassword = changePassword; }

  @JsonView({PrivacyLevelViews.Public.class, EntityLevelViews.Basic.class})
  public Boolean getEnabled() { return enabled; }

  public void setEnabled(Boolean enabled) { this.enabled = enabled; }

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

  @JsonView({PrivacyLevelViews.Public.class, EntityLevelViews.ProjectWithUser.class})
  public Date getLastLogin(){ return lastLogin; }

  public void setLastLogin(Date lastLogin) {
    this.lastLogin = lastLogin;
  }

  public void addOwnCourse(Subject subject) { subjectsOwns.add(subject); }

  public void addToGroup(Project project) {
    this.projects.add(project);
  }

  public void removeFromGroup(Project project) {
    if(this.projects.contains(project)) {
      this.projects.remove(project);
    }
  }

  /**
  public void enrollToCourse(Course course) { this.course.add(course); }

  public void removeFromCourse(Course course) { this.course.remove(course); }


  @JsonIgnore
  public Collection<Course> getEnrolledCourse() { return this.course; }
   **/

  private static String randomColorGenerator(){
    Random random = new Random();
    int red = random.nextInt(256);
    int green = random.nextInt(256);
    int blue = random.nextInt(256);
    return String.format("#%02x%02x%02x", red, green, blue);
  }

  private static String generateCapitalLetters(String username){
    String[] names = username.split(" ");
    String firstLetter;
    String secondLetter;
    if(names.length > 1){
      firstLetter = names[0].substring(0, 1);
      secondLetter = names[1].substring(0, 1);
    }
    else{
      firstLetter = names[0].substring(0, 1);
      secondLetter = names[0].substring(1, 2);
    }
    return (firstLetter + secondLetter).toUpperCase();
  }

}
