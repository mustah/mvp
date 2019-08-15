package com.elvaco.mvp.core.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.SubOrganisationParameters;
import com.elvaco.mvp.core.domainmodels.Usernamed;

public interface AuthenticatedUser extends Usernamed, Serializable {

  boolean isSuperAdmin();

  boolean isMvpAdmin();

  boolean isOtcAdmin();

  boolean isWithinOrganisation(UUID organisationId);

  Collection<Role> getRoles();

  UUID getOrganisationId();

  SubOrganisationParameters subOrganisationParameters();

  String getToken();

  UUID getUserId();
}
