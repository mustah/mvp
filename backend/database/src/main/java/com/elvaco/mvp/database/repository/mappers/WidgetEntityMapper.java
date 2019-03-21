package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.Widget;
import com.elvaco.mvp.database.entity.dashboard.WidgetEntity;
import com.elvaco.mvp.database.entity.meter.JsonField;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.experimental.UtilityClass;

@UtilityClass
public class WidgetEntityMapper {

  public static Widget toDomainModel(WidgetEntity entity) {
    return Widget.builder()
      .id(entity.id)
      .dashboardId(entity.dashboardId)
      .ownerUserId(entity.ownerUserId)
      .organisationId(entity.organisationId)
      .type(entity.type)
      .title(entity.title)
      .settings(entity.settings.getJson())
      .build();
  }

  public static WidgetEntity toEntity(Widget model) {
    return new WidgetEntity(
      model.id,
      model.dashboardId,
      model.ownerUserId,
      model.organisationId,
      model.type,
      model.title,
      new JsonField((ObjectNode) model.settings)
    );
  }
}
