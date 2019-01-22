package com.elvaco.mvp.core.domainmodels;

import java.util.UUID;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LogicalMeterCollectionStats {

  public final UUID id;
  public final Double collectionPercentage;
}
