package com.elvaco.mvp.core.security;

import com.elvaco.mvp.core.spi.data.RequestParameters;

import static com.elvaco.mvp.core.spi.data.RequestParameter.ORGANISATION;

public final class OrganisationFilter {

  private OrganisationFilter() {}

  public static RequestParameters setCurrentUsersOrganisationId(
    AuthenticatedUser currentUser,
    RequestParameters parameters
  ) {
    if (!currentUser.isSuperAdmin()) {
      parameters.replace(ORGANISATION, currentUser.getOrganisationId().toString());
    }
    return parameters;
  }
}
