package com.elvaco.mvp.core.security;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.User;

class MockAuthenticatedUser implements AuthenticatedUser {

  private static final long serialVersionUID = -4347874617014900239L;

  private final transient User user;
  private final String token;

  MockAuthenticatedUser(User user, String token) {
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

  @Override
  public String getToken() {
    return token;
  }
}
