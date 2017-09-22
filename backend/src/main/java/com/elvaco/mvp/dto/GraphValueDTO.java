package com.elvaco.mvp.dto;

import lombok.ToString;

@ToString
public class GraphValueDTO {

  public String name;
  public Float value;

  public GraphValueDTO(String name, Float value) {
    this.name = name;
    this.value = value;
  }
}
