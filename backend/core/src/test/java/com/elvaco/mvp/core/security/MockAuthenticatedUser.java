package com.elvaco.mvp.core.security;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.security.TokenFactory;

class MockAuthenticatedUser implements AuthenticatedUser {

  private final User user;
  private final TokenFactory tokenFactory;

  MockAuthenticatedUser(User user, TokenFactory tokenFactory) {
    this.user = user;
    this.tokenFactory = tokenFactory;
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
    return tokenFactory.newToken();
  }
}
