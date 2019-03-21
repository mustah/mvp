package com.elvaco.mvp.web.mapper;

import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Dashboard;
import com.elvaco.mvp.web.dto.DashboardDto;

import lombok.experimental.UtilityClass;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

@UtilityClass
public class DashboardDtoMapper {

  public static DashboardDto toDto(Dashboard dashboard) {
    return new DashboardDto(
      dashboard.id,
      dashboard.name,
      dashboard.layout.deepCopy(),
      dashboard.widgets.stream()
        .map(WidgetDtoMapper::toDto)
        .collect(toList())
    );
  }

  public static Dashboard toDomainModel(
    DashboardDto dto,
    UUID ownerUserId,
    UUID organisationId
  ) {
    return Dashboard.builder()
      .id(dto.id != null ? dto.id : randomUUID())
      .name(dto.name)
      .layout(dto.layout)
      .organisationId(organisationId)
      .ownerUserId(ownerUserId)
      .widgets(
        Optional.ofNullable(dto.widgets)
          .map(widgets -> widgets.stream()
            .map(widgetDto -> WidgetDtoMapper.toDomainModel(widgetDto, ownerUserId, organisationId))
            .collect(toList()))
          .orElse(emptyList()))
      .build();
  }
}
