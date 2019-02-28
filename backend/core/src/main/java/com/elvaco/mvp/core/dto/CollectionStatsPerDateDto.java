package com.elvaco.mvp.core.dto;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CollectionStatsPerDateDto {
  public OffsetDateTime date;
  public Double collectionPercentage;
}
