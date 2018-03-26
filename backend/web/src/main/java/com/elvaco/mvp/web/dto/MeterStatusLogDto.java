package com.elvaco.mvp.web.dto;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class MeterStatusLogDto {

  public Long id;
  public String name;
  public String start;
  public String stop;

  public MeterStatusLogDto(Long id, String name, String start, String stop) {
    this.id = id;
    this.name = name;
    this.start = start;
    this.stop = stop;
  }
}
