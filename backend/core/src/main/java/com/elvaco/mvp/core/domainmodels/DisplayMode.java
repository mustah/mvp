package com.elvaco.mvp.core.domainmodels;

public enum DisplayMode {
  UNKNOWN("unknown"),
  READOUT("readout"),
  CONSUMPTION("consumption");

  private final String name;

  DisplayMode(String name) {
    this.name = name;
  }

  public static DisplayMode from(String name) {
    return switch (name) {
      case "readout" -> READOUT;
      case "consumption" -> CONSUMPTION;
      default -> UNKNOWN;
    };
  }

  public static DisplayMode from(Integer ordinal) {
    return switch (ordinal) {
      case 1 -> READOUT;
      case 2 -> CONSUMPTION;
      default -> UNKNOWN;
    };
  }

  @Override
  public String toString() {
    return name;
  }
}
