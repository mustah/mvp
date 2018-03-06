package com.elvaco.mvp.web.dto;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class MeterStatusLogDto {
  public Long id;
  public String statusId;
  public String name;
  public String start;
  public String stop;

  public MeterStatusLogDto() { }

  public MeterStatusLogDto(Long id, String statusId, String name, String start, String stop) {
    this.id = id;
    this.statusId = statusId;
    this.name = name;
    this.start = start;
    this.stop = stop;
  }
}
