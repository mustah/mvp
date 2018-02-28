package com.elvaco.mvp.web.dto;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class IdNamedDto {

  public static final IdNamedDto OK = new IdNamedDto(0L, "Ok");
  public static final IdNamedDto UNKNOWN = new IdNamedDto(4L, "Unknown");

  public Long id;
  public String name;

  public IdNamedDto() {}

  private IdNamedDto(Long id, String name) {
    this.id = id;
    this.name = name;
  }

  public IdNamedDto(String name) {
    this((long) name.hashCode(), name);
  }
}
