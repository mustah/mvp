package com.elvaco.mvp.core.spi.repository;

import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Asset;
import com.elvaco.mvp.core.domainmodels.AssetType;

public interface OrganisationAssets {

  Asset getDefault(AssetType assetType);

  boolean existsByOrganisationIdAndAssetTypeAndChecksum(
    UUID organisationId,
    AssetType assetType,
    String checksum
  );

  Optional<Asset> findByOrganisationIdAndAssetType(UUID organisationId, AssetType assetType);

  void create(UUID organisationId, Asset asset);

  void delete(AssetType assetType, UUID organisationId);

  void deleteAll();
}
