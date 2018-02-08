package com.elvaco.mvp.core.usecase;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.OrganisationPermissions;
import com.elvaco.mvp.core.security.OrganisationPermissions.Permission;
import com.elvaco.mvp.core.spi.repository.Organisations;

import static com.elvaco.mvp.core.security.Permission.CREATE;
import static com.elvaco.mvp.core.security.Permission.DELETE;
import static com.elvaco.mvp.core.security.Permission.READ;
import static com.elvaco.mvp.core.security.Permission.UPDATE;

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
    return persist(organisation, CREATE);
  }

  public Optional<Organisation> update(Organisation organisation) {
    return persist(organisation, UPDATE);
  }

  public void delete(Organisation organisation) {
    if (organisationPermissions.isAllowed(currentUser, organisation, DELETE)) {
      organisations.deleteById(organisation.id);
    }
  }

  private Optional<Organisation> persist(Organisation organisation, Permission permission) {
    if (organisationPermissions.isAllowed(currentUser, organisation, permission)) {
      return Optional.of(organisations.save(organisation));
    } else {
      return Optional.empty();
    }
  }
}
