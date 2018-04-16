package com.elvaco.mvp.core.domainmodels;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import lombok.ToString;

import static java.util.Collections.unmodifiableList;
import static java.util.UUID.randomUUID;

@ToString
public class User implements Identifiable<UUID>, Usernamed {

  public final UUID id;
  public final String name;
  public final String email;
  @Nullable
  public final String password;
  public final Language language;
  public final Organisation organisation;
  public final List<Role> roles;
  public final boolean isAdmin;
  public final boolean isSuperAdmin;

  public User(
    UUID id,
    String name,
    String email,
    @Nullable String password,
    Language language,
    Organisation organisation,
    List<Role> roles
  ) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.password = password;
    this.language = language;
    this.organisation = organisation;
    this.roles = unmodifiableList(roles);
    this.isSuperAdmin = roles.contains(Role.SUPER_ADMIN);
    this.isAdmin = roles.contains(Role.ADMIN);
  }

  public User(
    String name,
    String email,
    String password,
    Language language,
    Organisation organisation,
    List<Role> roles
  ) {
    this(randomUUID(), name, email, password, language, organisation, roles);
  }

  public User withPassword(String password) {
    return new User(id, name, email, password, language, organisation, roles);
  }

  public User withName(String name) {
    return new User(id, name, email, password, language, organisation, roles);
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public String getUsername() {
    return email;
  }
}
