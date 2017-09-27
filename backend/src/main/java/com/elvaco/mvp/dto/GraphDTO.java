package com.elvaco.mvp.dto;

import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public class GraphDTO extends WidgetDTO {

  public List<GraphValueDTO> records;

  public GraphDTO() {
    records = new ArrayList<>();
  }
}
