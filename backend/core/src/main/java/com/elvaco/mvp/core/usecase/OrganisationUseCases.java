package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Asset;
import com.elvaco.mvp.core.domainmodels.AssetType;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.exception.InvalidFormat;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.OrganisationPermissions;
import com.elvaco.mvp.core.security.Permission;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.OrganisationAssets;
import com.elvaco.mvp.core.spi.repository.Organisations;

import lombok.RequiredArgsConstructor;

import static com.elvaco.mvp.core.security.Permission.CREATE;
import static com.elvaco.mvp.core.security.Permission.DELETE;
import static com.elvaco.mvp.core.security.Permission.READ;
import static com.elvaco.mvp.core.security.Permission.UPDATE;
import static com.elvaco.mvp.core.spi.data.EmptyPage.emptyPage;
import static java.util.Collections.emptyList;

@RequiredArgsConstructor
public class OrganisationUseCases {

  private static final Set<String> ALLOWED_CONTENT_TYPES_FOR_IMAGES = Set.of(
    "image/jpeg",
    "image/png",
    "image/gif"
  );

  private final AuthenticatedUser currentUser;
  private final Organisations organisations;
  private final OrganisationPermissions organisationPermissions;
  private final OrganisationAssets organisationAssets;

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

  public void createAsset(Organisation organisation, Asset asset) {
    ensureAllowedToModifyAsset(organisation);
    if (!ALLOWED_CONTENT_TYPES_FOR_IMAGES.contains(asset.contentType)) {
      throw InvalidFormat.image(ALLOWED_CONTENT_TYPES_FOR_IMAGES, asset.contentType);
    }
    organisationAssets.create(organisation.id, asset);
  }

  public void deleteAsset(Organisation organisation, AssetType assetType) {
    ensureAllowedToModifyAsset(organisation);
    organisationAssets.delete(assetType, organisation.id);
  }

  public Asset findAssetByOrganisationSlugOrFallback(
    String slug,
    AssetType assetType
  ) {
    return organisations
      .findBySlug(slug)
      .flatMap(organisation -> organisationAssets.findByOrganisationIdAndAssetType(
        organisation.id,
        assetType
      ))
      .orElse(organisationAssets.getDefault(assetType));
  }

  private void ensureAllowedToModifyAsset(Organisation organisation) {
    if (currentUser.isSuperAdmin()) {
      return;
    }

    throw new Unauthorized("User '" + currentUser.getUsername() + "' is not allowed to modify "
      + "this organisation");
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
