package com.elvaco.mvp.core.security;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.User;

class MockAuthenticatedUser implements AuthenticatedUser {
  private final User user;

  MockAuthenticatedUser(User user) {
    this.user = user;
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
  public String getUsername() {
    return user.email;
  }
}
