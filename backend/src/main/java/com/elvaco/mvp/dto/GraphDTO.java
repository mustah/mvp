package com.elvaco.mvp.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.ToString;

@ToString
public class GraphDTO extends WidgetDTO {

  public List<GraphValueDTO> records;

  public GraphDTO() {
    this.records = new ArrayList<>();
  }
}
