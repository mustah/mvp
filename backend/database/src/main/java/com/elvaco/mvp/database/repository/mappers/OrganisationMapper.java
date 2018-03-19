package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;

public class OrganisationMapper implements DomainEntityMapper<Organisation, OrganisationEntity> {

  @Override
  public Organisation toDomainModel(OrganisationEntity entity) {
    return new Organisation(entity.id, entity.name, entity.slug);
  }

  @Override
  public OrganisationEntity toEntity(Organisation domainModel) {
    return new OrganisationEntity(
      domainModel.id,
      domainModel.name,
      domainModel.slug,
      domainModel.externalId
    );
  }
}
