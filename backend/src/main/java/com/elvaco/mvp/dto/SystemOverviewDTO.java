package com.elvaco.mvp.dto;

import java.util.List;

import lombok.ToString;

@ToString
public class SystemOverviewDTO {

  public String title;
  public List<ColoredBoxDTO> indicators;
}
