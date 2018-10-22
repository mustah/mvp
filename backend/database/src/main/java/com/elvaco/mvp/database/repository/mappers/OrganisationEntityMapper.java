package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.database.entity.selection.UserSelectionEntity;
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
    OrganisationEntity parent = domainModel.parent != null ? toEntity(domainModel.parent) : null;
    UserSelectionEntity selection = domainModel.selection != null
      ? UserSelectionEntityMapper.toEntity(domainModel.selection)
      : null;
    return OrganisationEntity.builder()
      .id(domainModel.id)
      .name(domainModel.name)
      .slug(domainModel.slug)
      .externalId(domainModel.externalId)
      .parent(parent)
      .selection(selection)
      .build();
  }
}
