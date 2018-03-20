package com.elvaco.mvp.core.usecase;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.OrganisationPermissions;
import com.elvaco.mvp.core.security.Permission;
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

  public Optional<Organisation> findById(UUID id) {
    return organisations.findById(id)
      .filter(this::mayRead);
  }

  public Organisation create(Organisation organisation) {
    return persist(organisation, CREATE);
  }

  public Organisation update(Organisation organisation) {
    return persist(organisation, UPDATE);
  }

  public void delete(Organisation organisation) {
    if (organisationPermissions.isAllowed(currentUser, organisation, DELETE)) {
      organisations.deleteById(organisation.id);
    }
  }

  public Optional<Organisation> findByExternalId(String externalId) {
    return organisations.findByExternalId(externalId).filter(this::mayRead);
  }

  private boolean mayRead(Organisation organisation) {
    return organisationPermissions.isAllowed(currentUser, organisation, READ);
  }

  private Organisation persist(Organisation organisation, Permission permission) {
    if (organisationPermissions.isAllowed(currentUser, organisation, permission)) {
      return organisations.save(organisation);
    } else {
      throw new Unauthorized("User '" + currentUser.getUsername() + "' is not allowed to save "
                               + "this organisation");
    }
  }
}
