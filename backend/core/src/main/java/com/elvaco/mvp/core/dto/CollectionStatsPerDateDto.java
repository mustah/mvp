package com.elvaco.mvp.core.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CollectionStatsPerDateDto {
  public LocalDate date;
  public Double collectionPercentage;
}
