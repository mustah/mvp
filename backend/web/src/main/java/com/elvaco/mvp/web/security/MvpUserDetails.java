package com.elvaco.mvp.web.security;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import static java.util.stream.Collectors.toList;

public class MvpUserDetails implements UserDetails, AuthenticatedUser {

  private static final long serialVersionUID = -7344530747327091472L;
  private static final String SPRING_ROLE_PREFIX = "ROLE_";

  private final List<GrantedAuthority> authorities;
  private final String token;
  private final transient User user;

  public MvpUserDetails(User user, String token) {
    Objects.requireNonNull(user.password, "User must have a password.");
    this.authorities = user.roles.stream()
      .map(r -> r.role)
      .map(role -> new SimpleGrantedAuthority(SPRING_ROLE_PREFIX + role))
      .collect(toList());
    this.user = user;
    this.token = token;
  }

  public User getUser() {
    return user;
  }

  @Override
  public String getToken() {
    return token;
  }

  @Override
  public boolean isSuperAdmin() {
    return user.isSuperAdmin;
  }

  @Override
  public boolean isAdmin() {
    return user.isAdmin;
  }

  @Override
  public boolean isWithinOrganisation(Organisation organisation) {
    return user.organisation.id.equals(organisation.id);
  }

  @Override
  public Organisation getOrganisation() {
    return user.organisation;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return user.password;
  }

  @Override
  public String getUsername() {
    return user.email;
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
}
