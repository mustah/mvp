package com.elvaco.mvp.core.security;

import com.elvaco.mvp.core.domainmodels.Organisation;

public interface MvpPrincipal {
  boolean isSuperAdmin();

  boolean isAdmin();

  boolean isWithinOrganisation(Organisation organisation);

  Long getOrganisationId();
}
