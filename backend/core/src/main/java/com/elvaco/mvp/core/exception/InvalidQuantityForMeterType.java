package com.elvaco.mvp.core.exception;

public class InvalidQuantityForMeterType extends RuntimeException {

  private static final long serialVersionUID = 4881928094596865827L;

  public InvalidQuantityForMeterType(String quantityName, String meterType) {
    super(String.format("Invalid quantity '%s' for %s meter", quantityName, meterType));
  }
}
