package com.elvaco.mvp.core.security;

import java.io.Serializable;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.Usernamed;

import static com.elvaco.mvp.core.domainmodels.UserSelection.SelectionParametersDto;

public interface AuthenticatedUser extends Usernamed, Serializable {

  boolean isSuperAdmin();

  boolean isAdmin();

  boolean isWithinOrganisation(UUID organisationId);

  UUID getOrganisationId();

  @Nullable
  UUID getParentOrganisationId();

  @Nullable
  SelectionParametersDto selectionParameters();

  String getToken();

  UUID getUserId();
}
