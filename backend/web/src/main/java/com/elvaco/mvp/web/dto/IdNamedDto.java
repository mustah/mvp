package com.elvaco.mvp.web.dto;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class IdNamedDto {

  public Long id;
  public String name;

  private IdNamedDto(Long id, String name) {
    this.id = id;
    this.name = name;
  }

  public IdNamedDto(String name) {
    this((long) name.hashCode(), name);
  }
}
