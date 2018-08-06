package com.elvaco.mvp.web.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WidgetDto {

  public String type;
  public double total;
  public double pending;
}
