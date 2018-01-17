package com.elvaco.mvp.core.domainmodels;

import java.util.List;
import javax.annotation.Nullable;

import static java.util.Collections.unmodifiableList;

public class User {

  @Nullable
  public final Long id;
  public final String name;
  public final String email;
  @Nullable
  public final String password;
  public final Organisation organisation;
  public final List<Role> roles;

  public User(
    @Nullable Long id,
    String name,
    String email,
    @Nullable String password,
    Organisation organisation,
    List<Role> roles
  ) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.password = password;
    this.organisation = organisation;
    this.roles = unmodifiableList(roles);
  }

  public User(Long id, String name, String email, Organisation organisation, List<Role> roles) {
    this(id, name, email, null, organisation, roles);
  }

  public User(
    String name,
    String email,
    String password,
    Organisation organisation,
    List<Role> roles
  ) {
    this(null, name, email, password, organisation, roles);
  }

  public User withPassword(Password password) {
    return new User(id, name, email, password.getPassword(), organisation, roles);
  }
}
