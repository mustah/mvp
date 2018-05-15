package com.elvaco.mvp.testing.security;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.security.AuthenticatedUser;

public class MockAuthenticatedUser implements AuthenticatedUser {

  private static final long serialVersionUID = -4347874617014900239L;

  private final transient User user;
  private final String token;

  public MockAuthenticatedUser(List<Role> roles) {
    this(
      new User(
        UUID.randomUUID(),
        "test-user",
        "test@test.test",
        "password",
        Language.en,
        new Organisation(
          UUID.randomUUID(),
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
    return user.organisation.id;
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

  public Organisation getOrganisation() {
    return user.organisation;
  }

}
