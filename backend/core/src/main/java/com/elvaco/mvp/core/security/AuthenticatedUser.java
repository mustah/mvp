package com.elvaco.mvp.core.security;

import com.elvaco.mvp.core.domainmodels.Organisation;

public interface AuthenticatedUser {
  boolean isSuperAdmin();

  boolean isAdmin();

  boolean isWithinOrganisation(Organisation organisation);

  Organisation getOrganisation();

  String getUsername();
}