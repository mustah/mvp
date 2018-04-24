package com.elvaco.mvp.core.exception;

public class MixedDimensionForMeterQuantity extends RuntimeException {

  public MixedDimensionForMeterQuantity(String gotDimension, String expectedDimension) {
    super(String.format(
      "Mixing dimensions for meter quantity is not allowed (got: '%s', expected: '%s'",
      gotDimension,
      expectedDimension
    ));
  }
}
