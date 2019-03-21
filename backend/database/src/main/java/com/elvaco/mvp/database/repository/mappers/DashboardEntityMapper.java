package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.Dashboard;
import com.elvaco.mvp.database.entity.dashboard.DashboardEntity;
import com.elvaco.mvp.database.entity.meter.JsonField;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DashboardEntityMapper {

  public static Dashboard toDomainModel(DashboardEntity entity) {
    return Dashboard.builder()
      .id(entity.id)
      .ownerUserId(entity.ownerUserId)
      .organisationId(entity.organisationId)
      .name(entity.name)
      .layout(entity.layout.getJson())
      .build();
  }

  public static DashboardEntity toEntity(Dashboard model) {
    return new DashboardEntity(
      model.id,
      model.ownerUserId,
      model.organisationId,
      model.name,
      new JsonField((ObjectNode) model.layout)
    );
  }
}
