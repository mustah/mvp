package com.elvaco.mvp.core.spi.repository;

import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Asset;
import com.elvaco.mvp.core.domainmodels.AssetType;

public interface OrganisationAssets {

  Asset defaultAsset(AssetType assetType);

  Optional<Asset> findByOrganisationIdAndAssetType(UUID organisationId, AssetType assetType);

  void createAsset(UUID organisationId, Asset asset);

  void deleteAsset(AssetType assetType, UUID organisationId);

  void deleteAll();
}
