package com.elvaco.mvp.core.domainmodels;

public enum DisplayMode {
  UNKNOWN("unknown"),
  READOUT("readout"),
  CONSUMPTION("consumption");

  private final String name;

  DisplayMode(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
