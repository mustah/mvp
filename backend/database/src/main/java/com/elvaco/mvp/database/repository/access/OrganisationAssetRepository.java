package com.elvaco.mvp.database.repository.access;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Asset;
import com.elvaco.mvp.core.domainmodels.AssetType;
import com.elvaco.mvp.core.spi.repository.OrganisationAssets;
import com.elvaco.mvp.database.entity.organisationasset.OrganisationAssetPk;
import com.elvaco.mvp.database.repository.jpa.OrganisationAssetJpaRepository;
import com.elvaco.mvp.database.repository.mappers.OrganisationAssetEntityMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrganisationAssetRepository implements OrganisationAssets {

  private final Map<AssetType, Asset> defaultAssets;

  private final OrganisationAssetJpaRepository organisationAssetJpaRepository;

  @Override
  public Asset getDefault(AssetType assetType) {
    return defaultAssets.get(assetType);
  }

  @Override
  public Optional<Asset> findByOrganisationIdAndAssetType(
    UUID organisationId,
    AssetType assetType
  ) {
    return organisationAssetJpaRepository
      .findById(
        OrganisationAssetPk.builder()
          .organisationId(organisationId)
          .assetType(assetType)
          .build()
      )
      .map(OrganisationAssetEntityMapper::toDomainModel);
  }

  @Override
  public void create(
    UUID organisationId,
    Asset asset
  ) {
    organisationAssetJpaRepository.save(OrganisationAssetEntityMapper.toEntity(
      asset,
      organisationId
    ));
  }

  @Override
  public void delete(AssetType assetType, UUID organisationId) {
    organisationAssetJpaRepository.deleteById(
      OrganisationAssetPk.builder()
        .assetType(assetType)
        .organisationId(organisationId)
        .build()
    );
  }

  @Override
  public void deleteAll() {
    organisationAssetJpaRepository.deleteAll();
  }
}
