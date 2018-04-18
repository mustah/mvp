package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.database.entity.meter.JsonField;
import com.elvaco.mvp.database.entity.selection.UserSelectionEntity;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserSelectionEntityMapper {

  public static UserSelection toDomainModel(UserSelectionEntity entity) {
    return new UserSelection(
      entity.id,
      entity.ownerUserId,
      entity.name,
      entity.selectionParameters.getJson(),
      entity.organisationId
    );
  }

  public static UserSelectionEntity toEntity(UserSelection model) {
    return new UserSelectionEntity(
      model.id,
      model.ownerUserId,
      model.name,
      new JsonField((ObjectNode) model.selectionParameters),
      model.organisationId
    );
  }
}
