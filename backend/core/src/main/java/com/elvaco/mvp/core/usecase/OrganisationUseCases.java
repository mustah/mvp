package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.Asset;
import com.elvaco.mvp.core.domainmodels.AssetType;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Theme;
import com.elvaco.mvp.core.exception.InvalidFormat;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.OrganisationPermissions;
import com.elvaco.mvp.core.security.Permission;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.OrganisationAssets;
import com.elvaco.mvp.core.spi.repository.OrganisationThemes;
import com.elvaco.mvp.core.spi.repository.Organisations;

import lombok.RequiredArgsConstructor;

import static com.elvaco.mvp.core.security.Permission.CREATE;
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
  private final OrganisationThemes organisationTheme;

  public List<Organisation> findAll() {
    if (currentUser.isSuperAdmin()) {
      return organisations.findAll();
    } else if (currentUser.isMvpAdmin()) {
      return organisations.findOrganisationAndSubOrganisations(currentUser.getOrganisationId());
    }
    return emptyList();
  }

  public List<Organisation> findAllSubOrganisations(UUID organisationId) {
    if (currentUser.isSuperAdmin()) {
      return organisations.findAllSubOrganisations(organisationId);
    } else if (currentUser.isMvpAdmin()) {
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
    if (organisationPermissions.isAllowedToDelete(currentUser, organisation)) {
      organisations.deleteById(organisation.id);
    }
  }

  public Organisation findOrCreate(String externalId) {
    return organisations.findByExternalId(externalId)
      .filter(this::mayRead)
      .orElseGet(() -> create(Organisation.of(externalId)));
  }

  public void createAsset(Organisation organisation, Asset asset) {
    ensureAllowedToModifyAsset();
    if (!ALLOWED_CONTENT_TYPES_FOR_IMAGES.contains(asset.contentType)) {
      throw InvalidFormat.image(ALLOWED_CONTENT_TYPES_FOR_IMAGES, asset.contentType);
    }
    organisationAssets.create(organisation.id, asset);
  }

  public void deleteAsset(Organisation organisation, AssetType assetType) {
    ensureAllowedToModifyAsset();
    organisationAssets.delete(assetType, organisation.id);
  }

  public Optional<Asset> findAssetOrFallback(
    String slug,
    AssetType assetType,
    @Nullable String checksum
  ) {
    var organisation = organisations.findBySlug(slug);

    if (organisation.isEmpty()) {
      var defaultAsset = organisationAssets.getDefault(assetType);
      return defaultAsset.checksum.equals(checksum) ? Optional.empty() : Optional.of(defaultAsset);
    }

    if (checksum != null) {
      boolean cachedForOrganisation =
        organisationAssets.existsByOrganisationIdAndAssetTypeAndChecksum(
          organisation.get().id,
          assetType,
          checksum
        );

      if (cachedForOrganisation) {
        return Optional.empty();
      }

      boolean cachedForParentOrganisation = organisation.get()
        .getParentId()
        .map(parentId -> organisationAssets.existsByOrganisationIdAndAssetTypeAndChecksum(
          parentId,
          assetType,
          checksum
        ))
        .orElse(false);

      if (cachedForParentOrganisation) {
        return Optional.empty();
      }
    }

    return organisation
      .flatMap(org -> organisationAssets.findByOrganisationIdAndAssetType(org.id, assetType))
      .or(() -> organisation
        .flatMap(Organisation::getParentId)
        .flatMap(parentId ->
          organisationAssets.findByOrganisationIdAndAssetType(parentId, assetType)
        )
      )
      .or(() -> Optional.of(organisationAssets.getDefault(assetType)));
  }

  public Theme findTheme(Organisation organisation) {
    return organisationTheme.findByOrganisation(organisation);
  }

  public Optional<Theme> findTheme(String slug) {
    return organisations.findBySlug(slug)
      .map(organisationTheme::findByOrganisation);
  }

  public void saveTheme(Theme theme) {
    ensureAllowedToModifyAsset();
    sanityCheck(theme);
    organisationTheme.save(theme);
  }

  public void deleteTheme(Organisation organisation) {
    ensureAllowedToModifyAsset();
    organisationTheme.deleteThemeForOrganisation(organisation);
  }

  public Optional<Organisation> findBySlug(String slug) {
    return organisations.findBySlug(slug);
  }

  private void ensureAllowedToModifyAsset() {
    if (currentUser.isSuperAdmin()) {
      return;
    }

    throw new Unauthorized("User '" + currentUser.getUsername() + "' is not allowed to modify "
      + "this organisation");
  }

  private boolean mayRead(Organisation organisation) {
    return organisationPermissions.isAllowedToRead(currentUser, organisation);
  }

  private void sanityCheck(Theme theme) {
    var nr = 100;
    if (theme.properties.size() > nr) {
      throw new IllegalArgumentException("Too many theme properties, max allowed is " + nr);
    }

    var len = 100;
    if (theme.properties.entrySet().stream()
      .anyMatch(p ->
        (p.getKey() != null && p.getKey().length() > len)
          || (p.getValue() != null && p.getValue().length() > len))) {
      throw new IllegalArgumentException("Theme property size exceeds " + len);
    }
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
