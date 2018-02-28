package com.elvaco.mvp.core.security;

import java.io.Serializable;

public interface AuthenticatedUser extends Serializable {

  boolean isSuperAdmin();

  boolean isAdmin();

  boolean isWithinOrganisation(Long organisationId);

  Long getOrganisationId();

  String getUsername();

  String getToken();
}
