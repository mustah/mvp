package com.elvaco.mvp.web.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class CollectionStatDto {
  public UUID id;
  public Instant when;
  public String facility;
  public Long readIntervalMinutes;
  public Double collectionPercentage;
  public String latency;
}
