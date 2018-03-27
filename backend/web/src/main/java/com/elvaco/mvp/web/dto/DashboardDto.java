package com.elvaco.mvp.web.dto;

import java.util.List;
import java.util.UUID;

public class DashboardDto {
  public UUID id;
  public List<WidgetDto> widgets;

  public DashboardDto() {}

  public DashboardDto(UUID id, List<WidgetDto> widgets) {
    this.id = id;
    this.widgets = widgets;
  }
}
