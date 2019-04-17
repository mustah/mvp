package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.OrganisationPermissions;
import com.elvaco.mvp.core.security.Permission;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.Organisations;

import lombok.AllArgsConstructor;

import static com.elvaco.mvp.core.security.Permission.CREATE;
import static com.elvaco.mvp.core.security.Permission.DELETE;
import static com.elvaco.mvp.core.security.Permission.READ;
import static com.elvaco.mvp.core.security.Permission.UPDATE;
import static com.elvaco.mvp.core.spi.data.EmptyPage.emptyPage;
import static java.util.Collections.emptyList;

@AllArgsConstructor
public class OrganisationUseCases {

  private final AuthenticatedUser currentUser;
  private final Organisations organisations;
  private final OrganisationPermissions organisationPermissions;

  public List<Organisation> findAll() {
    if (currentUser.isSuperAdmin()) {
      return organisations.findAll();
    } else if (currentUser.isAdmin()) {
      return organisations.findOrganisationAndSubOrganisations(currentUser.getOrganisationId());
    }
    return emptyList();
  }

  public List<Organisation> findAllSubOrganisations(UUID organisationId) {
    if (currentUser.isSuperAdmin()) {
      return organisations.findAllSubOrganisations(organisationId);
    } else if (currentUser.isAdmin()) {
      return organisations.findAllSubOrganisations(currentUser.getOrganisationId());
    }
    return emptyList();
  }

  public Page<Organisation> findAllMainOrganisations(
    RequestParameters parameters,
    Pageable pageable
  ) {
    if (currentUser.isSuperAdmin()) {
      return organisations.findAllMainOrganisations(
        parameters.ensureOrganisationFilters(currentUser),
        pageable
      );
    } else {
      return emptyPage();
    }
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

  public Organisation findOrCreate(String externalId) {
    return organisations.findByExternalId(externalId)
      .filter(this::mayRead)
      .orElseGet(() -> create(Organisation.of(externalId)));
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
