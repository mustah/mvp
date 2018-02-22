package com.elvaco.mvp.web.dto;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class IdNamedDto {

  public String id;
  public String name;

  public IdNamedDto() {}

  private IdNamedDto(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public IdNamedDto(String name) {
    this(name, name);
  }
}
