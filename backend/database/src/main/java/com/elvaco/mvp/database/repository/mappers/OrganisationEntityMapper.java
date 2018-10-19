package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OrganisationEntityMapper {

  public static Organisation toDomainModel(OrganisationEntity entity) {
    return new Organisation(
      entity.id,
      entity.name,
      entity.slug,
      entity.externalId,
      entity.parent != null ? toDomainModel(entity.parent) : null,
      entity.selection != null
        ? UserSelectionEntityMapper.toDomainModel(entity.selection)
        : null
    );
  }

  public static OrganisationEntity toEntity(Organisation domainModel) {
    return new OrganisationEntity(
      domainModel.id,
      domainModel.name,
      domainModel.slug,
      domainModel.externalId,
      domainModel.parent != null ? toEntity(domainModel.parent) : null,
      domainModel.selection != null
        ? UserSelectionEntityMapper.toEntity(domainModel.selection)
        : null
    );
  }
}
