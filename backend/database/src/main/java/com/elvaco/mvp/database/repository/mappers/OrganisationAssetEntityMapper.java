package com.elvaco.mvp.database.repository.mappers;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Asset;
import com.elvaco.mvp.database.entity.organisationasset.OrganisationAssetEntity;
import com.elvaco.mvp.database.entity.organisationasset.OrganisationAssetPk;

import lombok.experimental.UtilityClass;

@UtilityClass
public class OrganisationAssetEntityMapper {

  public static Asset toDomainModel(OrganisationAssetEntity entity) {
    return Asset.builder()
      .assetType(entity.organisationAssetPk.assetType)
      .contentType(entity.contentType)
      .content(entity.content)
      .build();
  }

  public static OrganisationAssetEntity toEntity(Asset model, UUID organisationId) {
    return OrganisationAssetEntity.builder()
      .content(model.content)
      .contentType(model.contentType)
      .organisationAssetPk(
        OrganisationAssetPk.builder()
          .assetType(model.assetType)
          .organisationId(organisationId)
          .build()
      )
      .build();
  }
}
