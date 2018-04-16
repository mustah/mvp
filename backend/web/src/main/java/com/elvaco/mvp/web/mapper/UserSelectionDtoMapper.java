package com.elvaco.mvp.web.mapper;

import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.web.dto.UserSelectionDto;

import static java.util.UUID.randomUUID;

public class UserSelectionDtoMapper {
  public UserSelectionDto toDto(UserSelection userSelection) {
    return new UserSelectionDto(
      userSelection.id,
      userSelection.ownerUserId,
      userSelection.name,
      userSelection.selectionParameters.deepCopy(),
      userSelection.organisationId
    );
  }

  public UserSelection toDomainModel(UserSelectionDto dto) {
    return new UserSelection(
      dto.id != null ? dto.id : randomUUID(),
      dto.ownerUserId,
      dto.name,
      dto.selectionParameters,
      dto.organisationId
    );
  }
}
