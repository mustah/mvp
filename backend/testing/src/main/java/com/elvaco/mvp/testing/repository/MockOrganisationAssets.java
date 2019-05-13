package com.elvaco.mvp.testing.repository;

import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Asset;
import com.elvaco.mvp.core.domainmodels.AssetType;
import com.elvaco.mvp.core.spi.repository.OrganisationAssets;
import com.elvaco.mvp.testing.exception.NotImplementedYet;

public class MockOrganisationAssets implements OrganisationAssets {

  @Override
  public Asset getDefault(AssetType assetType) {
    throw new NotImplementedYet();
  }

  @Override
  public boolean existsByOrganisationIdAndAssetTypeAndChecksum(
    UUID organisationId,
    AssetType assetType,
    String checksum
  ) {
    throw new NotImplementedYet();
  }

  @Override
  public Optional<Asset> findByOrganisationIdAndAssetType(
    UUID organisationId,
    AssetType assetType
  ) {
    throw new NotImplementedYet();
  }

  @Override
  public void create(UUID organisationId, Asset asset) {
    throw new NotImplementedYet();
  }

  @Override
  public void delete(AssetType assetType, UUID organisationId) {
    throw new NotImplementedYet();
  }

  @Override
  public void deleteAll() {
    throw new NotImplementedYet();
  }
}
