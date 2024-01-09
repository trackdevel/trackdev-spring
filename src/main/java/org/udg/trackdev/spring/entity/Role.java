package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.security.core.GrantedAuthority;
import org.udg.trackdev.spring.configuration.UserType;
import org.udg.trackdev.spring.entity.views.PrivacyLevelViews;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Entity
public class Role extends BaseEntityLong implements GrantedAuthority {

  public Role() {
  }

  public Role(UserType UserType) {
    this.userType = UserType;
  }

  @NotNull
  @Column(unique = true)
  private UserType userType;

  @JsonView(PrivacyLevelViews.Private.class)
  public Long getId() {
    return super.getId();
  }

  @JsonView(PrivacyLevelViews.Public.class)
  public UserType getUserType() {
    return userType;
  }

  @Override
  public String getAuthority() {
    return userType.toString();
  }
}
