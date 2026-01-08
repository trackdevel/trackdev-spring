package org.trackdev.api.entity;

import org.springframework.security.core.GrantedAuthority;
import org.trackdev.api.configuration.UserType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;

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

  public Long getId() {
    return super.getId();
  }

  public UserType getUserType() {
    return userType;
  }

  @Override
  public String getAuthority() {
    return userType.toString();
  }
}
