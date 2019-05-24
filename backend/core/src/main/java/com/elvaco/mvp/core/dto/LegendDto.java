package com.elvaco.mvp.core.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class LegendDto {
  public UUID logicalMeterId;
  public String facility;
  public String medium;
}
