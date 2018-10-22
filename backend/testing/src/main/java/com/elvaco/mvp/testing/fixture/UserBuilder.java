package com.elvaco.mvp.testing.fixture;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;

import static com.elvaco.mvp.core.domainmodels.Role.ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.SUPER_ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.USER;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.ELVACO;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;

public final class UserBuilder {

  private UUID id;
  private String name;
  private String email;
  @Nullable
  private String password;
  private Language language;
  private Organisation organisation;
  private List<Role> roles = emptyList();

  public UserBuilder() {}

  public static UserBuilder from(User user) {
    return new UserBuilder()
      .id(user.id)
      .name(user.name)
      .email(user.email)
      .password(user.password)
      .language(user.language)
      .organisation(user.organisation)
      .roles(user.roles);
  }

  public UserBuilder id(UUID id) {
    this.id = id;
    return this;
  }

  public UserBuilder name(String name) {
    this.name = name;
    return this;
  }

  public UserBuilder email(String email) {
    this.email = email;
    return this;
  }

  public UserBuilder password(@Nullable String password) {
    this.password = password;
    return this;
  }

  public UserBuilder language(Language language) {
    this.language = language;
    return this;
  }

  public UserBuilder organisation(Organisation organisation) {
    this.organisation = organisation;
    return this;
  }

  public UserBuilder organisationElvaco() {
    return organisation(ELVACO);
  }

  public UserBuilder asSuperAdmin() {
    return roles(SUPER_ADMIN);
  }

  public UserBuilder asAdmin() {
    return roles(ADMIN);
  }

  public UserBuilder asUser() {
    return roles(USER);
  }

  public UserBuilder roles(List<Role> roles) {
    this.roles = roles;
    return this;
  }

  public UserBuilder roles(Role... roles) {
    return roles(asList(roles));
  }

  public User build() {
    return new User(
      id != null ? id : randomUUID(),
      name,
      email,
      password,
      language,
      organisation,
      roles
    );
  }
}
