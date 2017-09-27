package com.elvaco.mvp.dto;

import lombok.ToString;

@ToString
public class DashboardDTO {

  public Long id;
  public String author;
  public String title;
  public SystemOverviewDTO systemOverview;
}
