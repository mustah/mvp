package com.elvaco.mvp.testing.security;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.core.security.AuthenticatedUser;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;

public class MockAuthenticatedUser implements AuthenticatedUser {

  private static final long serialVersionUID = -4347874617014900239L;

  private final transient User user;
  private final String token;

  public MockAuthenticatedUser(List<Role> roles) {
    this(
      new User(
        randomUUID(),
        "test-user",
        "test@test.test",
        "password",
        Language.en,
        new Organisation(
          randomUUID(),
          "Test Organisation",
          "test-organisation",
          "Test Organisation"
        ),
        roles
      ),
      "testing-token"
    );
  }

  public MockAuthenticatedUser(User user, String token) {
    this.user = user;
    this.token = token;
  }

  public static MockAuthenticatedUser admin() {
    return new MockAuthenticatedUser(singletonList(Role.ADMIN));
  }

  public static MockAuthenticatedUser superAdmin() {
    return new MockAuthenticatedUser(singletonList(Role.SUPER_ADMIN));
  }

  public static MockAuthenticatedUser user() {
    return new MockAuthenticatedUser(singletonList(Role.USER));
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
  public boolean isWithinOrganisation(UUID organisationId) {
    return getOrganisationId().equals(organisationId);
  }

  @Override
  public UUID getOrganisationId() {
    return user.getOrganisationId();
  }

  @Nullable
  @Override
  public UUID getParentOrganisationId() {
    return user.getParentOrganisationId().orElse(null);
  }

  @Nullable
  @Override
  public UserSelection.SelectionParametersDto selectionParameters() {
    return user.getSelectionParameters().orElse(null);
  }

  @Override
  public String getToken() {
    return token;
  }

  @Override
  public UUID getUserId() {
    return user.id;
  }

  @Override
  public String getUsername() {
    return user.getUsername();
  }

  public User getUser() {
    return user;
  }

  public Organisation getOrganisation() {
    return user.organisation;
  }
}
