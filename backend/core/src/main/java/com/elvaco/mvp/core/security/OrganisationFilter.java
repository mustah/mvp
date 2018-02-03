package com.elvaco.mvp.core.security;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OrganisationFilter {
  public static Map<String, List<String>> complementFilterWithOrganisationParameters(
    AuthenticatedUser currentUser,
    Map<String, List<String>> filterParams
  ) {
    if (!currentUser.isSuperAdmin()) {
      Long organisationId = currentUser.getOrganisation().id;
      filterParams.put("organisation", Collections.singletonList(organisationId.toString()));
    }
    return filterParams;
  }
}
