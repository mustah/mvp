package com.elvaco.mvp.core.usecase;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.OrganisationPermissions;
import com.elvaco.mvp.core.spi.repository.Organisations;

import static com.elvaco.mvp.core.security.OrganisationPermissions.Permission.CREATE;
import static com.elvaco.mvp.core.security.OrganisationPermissions.Permission.DELETE;
import static com.elvaco.mvp.core.security.OrganisationPermissions.Permission.READ;
import static com.elvaco.mvp.core.security.OrganisationPermissions.Permission.UPDATE;

public class OrganisationUseCases {

  private final AuthenticatedUser currentUser;
  private final Organisations organisations;
  private final OrganisationPermissions organisationPermissions;

  public OrganisationUseCases(
    AuthenticatedUser currentUser,
    Organisations organisations,
    OrganisationPermissions organisationPermissions
  ) {
    this.currentUser = currentUser;
    this.organisations = organisations;
    this.organisationPermissions = organisationPermissions;
  }

  public List<Organisation> findAll() {
    if (currentUser.isSuperAdmin()) {
      return organisations.findAll();
    }
    return Collections.emptyList();
  }

  public Optional<Organisation> findById(Long id) {
    return organisations.findById(id)
      .filter(organisation -> organisationPermissions.isAllowed(currentUser, organisation, READ));
  }

  public Optional<Organisation> create(Organisation organisation) {
    if (organisationPermissions.isAllowed(currentUser, organisation, CREATE)) {
      return Optional.of(organisations.create(organisation));
    }
    return Optional.empty();
  }

  public Optional<Organisation> update(Organisation organisation) {
    if (organisationPermissions.isAllowed(currentUser, organisation, UPDATE)) {
      return findById(organisation.id)
        .map(organisations::update);
    }
    return Optional.empty();
  }

  public void delete(Organisation organisation) {
    if (organisationPermissions.isAllowed(currentUser, organisation, DELETE)) {
      organisations.deleteById(organisation.id);
    }
  }
}
