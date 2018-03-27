package com.elvaco.mvp.web.dto;

public class WidgetDto {
  public String type;
  public double total;
  public String status;
  public double pending;

  public WidgetDto() {}

  public WidgetDto(
    String type,
    double total,
    String status,
    double pending
  ) {
    this.type = type;
    this.total = total;
    this.status = status;
    this.pending = pending;
  }
}
