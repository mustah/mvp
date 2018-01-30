package com.elvaco.mvp.repository.access;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.entity.user.OrganisationEntity;

public class OrganisationMapper implements DomainEntityMapper<Organisation, OrganisationEntity> {

  @Override
  public Organisation toDomainModel(OrganisationEntity entity) {
    return new Organisation(entity.id, entity.name, entity.code);
  }

  @Override
  public OrganisationEntity toEntity(Organisation domainModel) {
    return new OrganisationEntity(domainModel.id, domainModel.name, domainModel.code);
  }
}
