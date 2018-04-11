package com.elvaco.geoservice.service.geocodefarm;

import java.util.stream.Stream;

public enum Accuracy {

  EXACT_MATCH(1.0),
  HIGH_ACCURACY(0.75),
  MEDIUM_ACCURACY(0.5),
  NO_MATCH(0.0);

  public final double value;

  Accuracy(double value) {
    this.value = value;
  }

  public static Accuracy from(String accuracyResponse) {
    return Stream.of(values())
      .filter(accuracy -> accuracy.name().equalsIgnoreCase(accuracyResponse))
      .findFirst()
      .orElse(NO_MATCH);
  }
}
