package com.elvaco.mvp.core.usecase;

import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.Asset;
import com.elvaco.mvp.core.domainmodels.AssetType;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Theme;
import com.elvaco.mvp.core.exception.InvalidFormat;
import com.elvaco.mvp.core.spi.repository.OrganisationAssets;
import com.elvaco.mvp.core.spi.repository.OrganisationThemes;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrganisationThemeUseCases {

  private static final Set<String> ALLOWED_CONTENT_TYPES_FOR_IMAGES = Set.of(
    "image/jpeg",
    "image/png",
    "image/gif"
  );

  private final OrganisationAssets organisationAssets;
  private final OrganisationThemes organisationTheme;

  public Optional<Asset> findAssetOrFallback(
    @Nullable Organisation organisation,
    AssetType assetType,
    @Nullable String checksum
  ) {
    if (organisation == null) {
      var defaultAsset = organisationAssets.getDefault(assetType);
      return defaultAsset.checksum.equals(checksum) ? Optional.empty() : Optional.of(defaultAsset);
    }

    if (checksum != null) {
      boolean cachedForOrganisation =
        organisationAssets.existsByOrganisationIdAndAssetTypeAndChecksum(
          organisation.id,
          assetType,
          checksum
        );

      if (cachedForOrganisation) {
        return Optional.empty();
      }

      boolean cachedForParentOrganisation = organisation.getParentId()
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

    return organisationAssets.findByOrganisationIdAndAssetType(organisation.id, assetType)
      .or(() -> organisation.getParentId()
        .flatMap(parentId ->
          organisationAssets.findByOrganisationIdAndAssetType(parentId, assetType)
        ))
      .or(() -> Optional.of(organisationAssets.getDefault(assetType)));
  }

  public void createAsset(Organisation organisation, Asset asset) {
    if (!ALLOWED_CONTENT_TYPES_FOR_IMAGES.contains(asset.contentType)) {
      throw InvalidFormat.image(ALLOWED_CONTENT_TYPES_FOR_IMAGES, asset.contentType);
    }
    organisationAssets.create(organisation.id, asset);
  }

  public void deleteAsset(Organisation organisation, AssetType assetType) {
    organisationAssets.delete(assetType, organisation.id);
  }

  public Theme findTheme(Organisation organisation) {
    return organisationTheme.findBy(organisation);
  }

  public void saveTheme(Theme theme) {
    sanityCheck(theme);
    organisationTheme.save(theme);
  }

  public void deleteTheme(Organisation organisation) {
    organisationTheme.deleteTheme(organisation);
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
}
