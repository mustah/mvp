package com.elvaco.mvp.dto;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class IdNamedDto {

  public Long id;
  public String name;

  public IdNamedDto() {}

  public IdNamedDto(Long id, String name) {
    this.id = id;
    this.name = name;
  }
}
