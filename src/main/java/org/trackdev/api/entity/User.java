package org.trackdev.api.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.ZonedDateTime;
import java.util.*;

import org.trackdev.api.configuration.UserType;
import org.trackdev.api.converter.EncryptedStringConverter;
import org.trackdev.api.serializer.JsonRolesSerializer;

@Entity
@Table(name = "users")
public class User extends BaseEntityUUID {

  public static final int MIN_USERNAME_LENGTH = 1;
  public static final int USERNAME_LENGTH = 50;
  public static final int MIN_EMAIL_LENGHT = 4;
  public static final int EMAIL_LENGTH = 512; // Increased for encrypted data
  public static final int CAPITAL_LETTERS_LENGTH = 2;
  public static final String USERNAME_PATTERN = "^[a-zA-Z0-9_#-]+$";
  public static final int MIN_FULL_NAME_LENGTH = 1;
  public static final int FULL_NAME_LENGTH = 100;

  public User() {}

  public User(String username, String fullName, String email, String password) {
    this.username = username;
    this.fullName = fullName;
    this.email = email;
    this.password = password;
    this.color = randomColorGenerator();
    this.githubInfo = new GithubInfo();
    this.discordInfo = new DiscordInfo();
    this.capitalLetters = generateCapitalLetters(fullName);
  }

  @NotNull
  @Column(length=USERNAME_LENGTH)
  private String username;

  @NotNull
  @Column(length=FULL_NAME_LENGTH)
  private String fullName;

  @NotNull
  @Column(unique=true, length=EMAIL_LENGTH)
  @Convert(converter = EncryptedStringConverter.class)
  private String email;

  @NotNull
  private String password;

  @Column(columnDefinition = "TIMESTAMP")
  private ZonedDateTime lastLogin;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
  private Collection<Subject> subjectsOwns = new ArrayList<>();

  @ManyToMany(mappedBy = "members")
  private Collection<Project> projects = new ArrayList<>();

  @ManyToMany()
  @JoinTable(
      name = "users_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id")
  )
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

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "discordInfoId", referencedColumnName = "id")
  private DiscordInfo discordInfo;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PointsReview> pointsReviewList = new ArrayList<>();

  @ManyToOne(fetch = FetchType.LAZY)
  private Workspace workspace;

  @Column(name = "workspace_id", insertable = false, updatable = false)
  private Long workspaceId;

  public static final int TIMEZONE_LENGTH = 50;
  public static final String DEFAULT_TIMEZONE = "UTC";

  @Column(length = TIMEZONE_LENGTH)
  private String timezone = DEFAULT_TIMEZONE;

  @Transient
  private Random random = new Random();

 // -- GETTERS AND SETTERS

  public String getId() {
    return super.getId();
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
    this.capitalLetters = generateCapitalLetters(fullName);
  }

  public Long getCurrentProject() { return currentProject; }

  public void setCurrentProject(Long currentProject) { this.currentProject = currentProject; }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getRecoveryCode() { return recoveryCode; }

  public void setRecoveryCode(String recoveryCode) { this.recoveryCode = recoveryCode; }

  @JsonSerialize(using= JsonRolesSerializer.class)
  public Set<Role> getRoles() { return roles; }

  public String getColor() { return color; }

  public String setColor(String color) { return this.color = color; }

  public String getCapitalLetters() { return capitalLetters; }

  public Collection<Project> getProjects() {
    return projects;
  }

  public GithubInfo getGithubInfo() { return githubInfo; }

  public DiscordInfo getDiscordInfo() { return discordInfo; }

  public void setDiscordInfo(DiscordInfo discordInfo) { this.discordInfo = discordInfo; }

  public List<PointsReview> getPointsReviewList() { return pointsReviewList; }

  public void addPointsReview(PointsReview pointsReview) { this.pointsReviewList.add(pointsReview); }

  public Workspace getWorkspace() { return workspace; }

  public void setWorkspace(Workspace workspace) { this.workspace = workspace; }

  public Long getWorkspaceId() { return workspaceId; }

  public String getTimezone() { return timezone != null ? timezone : DEFAULT_TIMEZONE; }

  public void setTimezone(String timezone) { this.timezone = timezone != null ? timezone : DEFAULT_TIMEZONE; }

  public String setGithubToken(String githubToken) { return githubInfo.setGithubToken(githubToken); }

  public void setGithubName(String login) { githubInfo.setLogin(login); }

  public void setGithubAvatar(String githubAvatar) { githubInfo.setAvatar_url(githubAvatar); }

  public void setGithubHtmlUrl(String githubHtmlUrl) { githubInfo.setHtml_url(githubHtmlUrl); }

  public String setCapitalLetters(String capitalLetters) { return this.capitalLetters = capitalLetters; }

  public Boolean getChangePassword() { return changePassword; }

  public void setChangePassword(Boolean changePassword) { this.changePassword = changePassword; }

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

  public ZonedDateTime getLastLogin(){ return lastLogin; }

  public void setLastLogin(ZonedDateTime lastLogin) {
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

  private String randomColorGenerator(){
    int red = this.random.nextInt(256);
    int green = this.random.nextInt(256);
    int blue = this.random.nextInt(256);
    return "#%02x%02x%02x".formatted(red, green, blue);
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
