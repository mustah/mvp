package com.elvaco.mvp.core.security;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import lombok.experimental.UtilityClass;

import static com.elvaco.mvp.core.spi.data.RequestParameter.ORGANISATION;

@UtilityClass
public class OrganisationFilter {

  public static RequestParameters parametersWithOrganisationId(
    AuthenticatedUser currentUser,
    RequestParameters parameters
  ) {
    if (!currentUser.isSuperAdmin()) {
      parameters.replace(ORGANISATION, currentUser.getOrganisationId().toString());
    }
    return parameters;
  }
}
