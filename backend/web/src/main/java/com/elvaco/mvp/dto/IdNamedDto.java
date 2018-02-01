package com.elvaco.mvp.dto;

import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class IdNamedDto {

  @Nullable
  public Long id;
  public String name;

  public IdNamedDto() {}

  public IdNamedDto(@Nullable Long id, String name) {
    this.id = id;
    this.name = name;
  }

  public IdNamedDto(String name) {
    this(null, name);
  }
}
