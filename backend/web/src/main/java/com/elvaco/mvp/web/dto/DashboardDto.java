package com.elvaco.mvp.web.dto;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class DashboardDto {

  public UUID id;
  public String name;
  public ObjectNode layout;

  @Nullable
  public List<WidgetDto> widgets;
}
