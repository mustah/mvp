package com.elvaco.mvp.database.repository.mappers;

import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.database.entity.selection.UserSelectionEntity;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;

import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

@UtilityClass
public class OrganisationEntityMapper {

  public static Organisation toDomainModel(OrganisationEntity entity) {
    return new Organisation(
      entity.id,
      entity.name,
      entity.slug,
      entity.externalId,
      entity.shortPrefix,
      entity.parent != null ? toDomainModel(entity.parent) : null,
      entity.selection != null
        ? UserSelectionEntityMapper.toDomainModel(entity.selection)
        : null
    );
  }

  public static OrganisationEntity toEntity(Organisation domainModel) {
    return OrganisationEntity.builder()
      .id(domainModel.id)
      .name(domainModel.name)
      .slug(domainModel.slug)
      .externalId(domainModel.externalId)
      .shortPrefix(StringUtils.hasText(domainModel.shortPrefix)
        ? domainModel.shortPrefix.trim()
        : null)
      .parent(domainModel.parent != null ? toEntity(domainModel.parent) : null)
      .selection(toUserSelectionEntity(domainModel.selection))
      .build();
  }

  @Nullable
  private static UserSelectionEntity toUserSelectionEntity(UserSelection selection) {
    return selection != null ? UserSelectionEntityMapper.toEntity(selection) : null;
  }
}
