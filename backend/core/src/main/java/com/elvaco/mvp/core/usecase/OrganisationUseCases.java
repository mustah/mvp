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

import lombok.RequiredArgsConstructor;

import static com.elvaco.mvp.core.security.Permission.CREATE;
import static com.elvaco.mvp.core.security.Permission.UPDATE;
import static com.elvaco.mvp.core.spi.data.EmptyPage.emptyPage;

@RequiredArgsConstructor
public class OrganisationUseCases {

  private final AuthenticatedUser currentUser;
  private final Organisations organisations;
  private final OrganisationPermissions organisationPermissions;

  public List<Organisation> findAll() {
    if (currentUser.isSuperAdmin()) {
      return organisations.findAll();
    } else if (currentUser.isMvpAdmin()) {
      return organisations.findOrganisationAndSubOrganisations(currentUser.getOrganisationId());
    } else {
      return List.of();
    }
  }

  public List<Organisation> findAllSubOrganisations(UUID organisationId) {
    if (currentUser.isSuperAdmin()) {
      return organisations.findAllSubOrganisations(organisationId);
    } else if (currentUser.isMvpAdmin()) {
      return organisations.findAllSubOrganisations(currentUser.getOrganisationId());
    } else {
      return List.of();
    }
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
      .filter(this::isAllowedToRead);
  }

  public Organisation create(Organisation organisation) {
    return persist(organisation, CREATE);
  }

  public Organisation update(Organisation organisation) {
    return persist(organisation, UPDATE);
  }

  public void delete(Organisation organisation) {
    if (organisationPermissions.isAllowedToDelete(currentUser, organisation)) {
      organisations.deleteById(organisation.id);
    }
  }

  public Organisation findOrCreate(String externalId) {
    return organisations.findByExternalId(externalId)
      .filter(this::isAllowedToRead)
      .orElseGet(() -> create(Organisation.of(externalId)));
  }

  public Optional<Organisation> findBySlug(String slug) {
    return organisations.findBySlug(slug);
  }

  private boolean isAllowedToRead(Organisation organisation) {
    return organisationPermissions.isAllowedToRead(currentUser, organisation);
  }

  private Organisation persist(Organisation organisation, Permission permission) {
    if (organisationPermissions.isAllowed(currentUser, organisation, permission)) {
      return organisations.saveAndFlush(organisation);
    } else {
      throw new Unauthorized("User '" + currentUser.getUsername() + "' is not allowed to save "
        + "this organisation");
    }
  }
}
