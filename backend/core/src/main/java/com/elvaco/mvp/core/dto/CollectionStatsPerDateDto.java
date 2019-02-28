package com.elvaco.mvp.core.dto;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class CollectionStatsPerDateDto {
  public OffsetDateTime date;
  public Double collectionPercentage;
}
