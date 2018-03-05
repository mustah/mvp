package com.elvaco.mvp.core.security;

import java.io.Serializable;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Usernamed;

public interface AuthenticatedUser extends Usernamed, Serializable {

  boolean isSuperAdmin();

  boolean isAdmin();

  boolean isWithinOrganisation(UUID organisationId);

  UUID getOrganisationId();

  String getToken();
}
