package org.udg.trackdev.spring.entity;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.security.core.GrantedAuthority;
import org.udg.trackdev.spring.configuration.UserType;

import javax.persistence.*;
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

  @JsonView(Views.Private.class)
  public Long getId() {
    return super.getId();
  }

  @JsonView(Views.Public.class)
  public UserType getUserType() {
    return userType;
  }

  @Override
  public String getAuthority() {
    return userType.toString();
  }
}
