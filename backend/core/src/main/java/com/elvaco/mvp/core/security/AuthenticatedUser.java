package com.elvaco.mvp.core.security;

import java.io.Serializable;

import com.elvaco.mvp.core.domainmodels.Organisation;

public interface AuthenticatedUser extends Serializable {

  boolean isSuperAdmin();

  boolean isAdmin();

  boolean isWithinOrganisation(Organisation organisation);

  Organisation getOrganisation();

  String getUsername();

  String getToken();
}
