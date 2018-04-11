package com.elvaco.geoservice.service.geocodefarm;

import java.util.stream.Stream;

public enum Accuracy {

  EXACT_MATCH(1),
  HIGH_ACCURACY(0.75),
  MEDIUM_ACCURACY(0.5),;

  public final double value;

  Accuracy(double value) {
    this.value = value;
  }

  public static double from(String accuracyResponse) {
    return Stream.of(values())
      .filter(accuracy -> accuracy.name().equalsIgnoreCase(accuracyResponse))
      .map(accuracy -> accuracy.value)
      .findFirst()
      .orElse(0.0);
  }
}
