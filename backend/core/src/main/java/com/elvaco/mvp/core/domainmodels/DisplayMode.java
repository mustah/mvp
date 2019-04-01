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
    switch (name) {
      case "readout":
        return READOUT;
      case "consumption":
        return CONSUMPTION;
      default:
        return UNKNOWN;
    }
  }

  public static DisplayMode from(Integer ordinal) {
    switch (ordinal) {
      case 1:
        return READOUT;
      case 2:
        return CONSUMPTION;
      default:
        return UNKNOWN;
    }
  }

  @Override
  public String toString() {
    return name;
  }
}
