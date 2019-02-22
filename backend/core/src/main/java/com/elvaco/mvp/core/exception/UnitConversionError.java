package com.elvaco.mvp.core.exception;

public class UnitConversionError extends RuntimeException {

  private static final long serialVersionUID = 1718837177888358761L;

  public UnitConversionError(String message) {
    super(message);
  }

  public UnitConversionError(String from, String to) {
    this(String.format("Can not convert from unit '%s' to '%s'", from, to));
  }

  public static UnitConversionError unknownUnit(String unknownUnit) {
    return new UnitConversionError(String.format(
      "Can not convert to unknown unit '%s'",
      unknownUnit
    ));
  }
}
