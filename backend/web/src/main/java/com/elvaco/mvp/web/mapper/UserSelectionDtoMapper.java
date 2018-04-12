package com.elvaco.mvp.web.mapper;

import java.io.IOException;

import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.web.dto.UserSelectionDto;
import com.elvaco.mvp.web.exception.InvalidJson;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static java.util.UUID.randomUUID;

public class UserSelectionDtoMapper {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public UserSelectionDto toDto(UserSelection userSelection) {
    return new UserSelectionDto(
      userSelection.id,
      userSelection.ownerUserId,
      userSelection.name,
      userSelection.data.toString(),
      userSelection.organisationId
    );
  }

  public UserSelection toDomainModel(UserSelectionDto dto) {
    JsonNode jsonData;

    try {
      jsonData = OBJECT_MAPPER.readTree(dto.data);
    } catch (IOException e) {
      throw new InvalidJson(dto.data);
    }

    return new UserSelection(
      dto.id != null ? dto.id : randomUUID(),
      dto.ownerUserId,
      dto.name,
      jsonData,
      dto.organisationId
    );
  }
}
