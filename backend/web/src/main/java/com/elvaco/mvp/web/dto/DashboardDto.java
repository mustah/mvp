package com.elvaco.mvp.web.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {

  public UUID id;
  public List<WidgetDto> widgets;
}
