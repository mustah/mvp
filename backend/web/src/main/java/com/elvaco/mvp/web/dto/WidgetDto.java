package com.elvaco.mvp.web.dto;

import java.util.UUID;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class WidgetDto {

  public UUID id;
  public UUID dashboardId;
  public String type;
  public String title;
  public ObjectNode settings;
}
