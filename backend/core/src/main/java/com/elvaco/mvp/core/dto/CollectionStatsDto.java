package com.elvaco.mvp.core.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class CollectionStatsDto {
  @Nullable
  public UUID id;
  @Nullable
  public String facility;
  @Nullable
  public Integer readInterval;
  public Double collectionPercentage;
  @Nullable
  public OffsetDateTime lastData;
}
