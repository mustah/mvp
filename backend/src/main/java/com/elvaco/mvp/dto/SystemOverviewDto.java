package com.elvaco.mvp.dto;

import java.util.List;
import lombok.ToString;

@ToString
public class SystemOverviewDto {

  public String title;
  public List<ColoredBoxDto> indicators;
}
