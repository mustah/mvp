package com.elvaco.mvp.dto;

import lombok.ToString;

import java.util.List;

@ToString
public class SystemOverviewDTO {

  public String title;
  public List<WidgetDTO> widgets;
}
