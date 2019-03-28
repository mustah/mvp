package com.elvaco.mvp.web.mapper;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Widget;
import com.elvaco.mvp.core.domainmodels.WidgetType;
import com.elvaco.mvp.web.dto.WidgetDto;
import com.elvaco.mvp.web.exception.InvalidWidgetType;

import lombok.experimental.UtilityClass;

import static java.util.UUID.randomUUID;

@UtilityClass
public class WidgetDtoMapper {

  public static WidgetDto toDto(Widget widget) {
    return new WidgetDto(
      widget.id,
      widget.dashboardId,
      widget.type.toString(),
      widget.title,
      widget.settings.deepCopy()
    );
  }

  public static Widget toDomainModel(WidgetDto dto, UUID ownerUserId, UUID organisationId) {
    return Widget.builder()
      .id(dto.id != null ? dto.id : randomUUID())
      .dashboardId(dto.dashboardId)
      .ownerUserId(ownerUserId)
      .organisationId(organisationId)
      .type(WidgetType.from(dto.type).orElseThrow(() -> new InvalidWidgetType(dto.type)))
      .title(dto.title)
      .settings(dto.settings)
      .build();
  }
}
