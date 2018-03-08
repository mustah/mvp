package com.elvaco.mvp.core.security;

import com.elvaco.mvp.core.spi.data.RequestParameters;

public final class OrganisationFilter {

  private OrganisationFilter() {}

  public static RequestParameters setCurrentUsersOrganisationId(
    AuthenticatedUser currentUser,
    RequestParameters parameters
  ) {
    if (!currentUser.isSuperAdmin()) {
      parameters.replace("organisation", currentUser.getOrganisationId().toString());
    }
    return parameters;
  }
}
