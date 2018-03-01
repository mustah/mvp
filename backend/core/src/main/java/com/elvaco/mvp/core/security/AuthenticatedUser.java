package com.elvaco.mvp.core.security;

import java.io.Serializable;
import java.util.UUID;

public interface AuthenticatedUser extends Serializable {

  boolean isSuperAdmin();

  boolean isAdmin();

  boolean isWithinOrganisation(UUID organisationId);

  UUID getOrganisationId();

  String getUsername();

  String getToken();
}
