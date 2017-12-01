package com.elvaco.mvp.dto;

import lombok.ToString;

@ToString
public class DashboardDto {

  public Long id;
  public String author;
  public String title;
  public SystemOverviewDto systemOverview;
}
