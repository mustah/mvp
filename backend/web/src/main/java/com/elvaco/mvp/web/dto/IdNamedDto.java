package com.elvaco.mvp.web.dto;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class IdNamedDto {

  public String id;
  public String name;

  private IdNamedDto(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public IdNamedDto(String name) {
    this(name, name);
  }
}
