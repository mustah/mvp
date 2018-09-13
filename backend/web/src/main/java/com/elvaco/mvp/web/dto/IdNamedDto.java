package com.elvaco.mvp.web.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class IdNamedDto {

  public String id;
  public String name;

  public IdNamedDto(String name) {
    this(name, name);
  }
}
