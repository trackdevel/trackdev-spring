package org.udg.trackdev.spring.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.udg.trackdev.spring.entity.Role;
import org.udg.trackdev.spring.entity.User;

import java.util.Collection;
import java.util.Set;

public class AppUserPrincipal implements UserDetails {

    private final User user;

    //

    public AppUserPrincipal(User user) {
        this.user = user;
    }

    //

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    //

    public User getAppUser() {
        return user;
    }

}
