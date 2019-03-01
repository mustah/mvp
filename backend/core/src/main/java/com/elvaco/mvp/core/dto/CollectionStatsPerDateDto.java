package com.elvaco.mvp.core.dto;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CollectionStatsPerDateDto {
  public ZonedDateTime date;
  public Double collectionPercentage;
}
