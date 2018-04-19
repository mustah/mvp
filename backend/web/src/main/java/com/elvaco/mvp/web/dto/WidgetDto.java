package com.elvaco.mvp.web.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class WidgetDto {

  public String type;
  public String status;
  public double total;
  public double pending;
}
