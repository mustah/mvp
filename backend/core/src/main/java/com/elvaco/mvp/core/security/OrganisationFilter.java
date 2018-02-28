package com.elvaco.mvp.core.security;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

public final class OrganisationFilter {

  private OrganisationFilter() {}

  public static Map<String, List<String>> addOrganisationIdToFilterParams(
    AuthenticatedUser currentUser,
    Map<String, List<String>> filterParams
  ) {
    if (!currentUser.isSuperAdmin()) {
      Long organisationId = currentUser.getOrganisationId();
      filterParams.put("organisation", singletonList(organisationId.toString()));
    }
    return filterParams;
  }
}
