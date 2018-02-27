package com.elvaco.mvp.web.dto;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class MeterStatusLogDto {
  public Long id;
  public Long statusId;
  public String name;
  public String start;
  public String stop;
}
