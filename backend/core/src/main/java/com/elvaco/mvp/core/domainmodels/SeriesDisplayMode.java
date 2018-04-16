package com.elvaco.mvp.core.domainmodels;

public enum SeriesDisplayMode {
  UNKNOWN("unknown"),
  READOUT("readout"),
  CONSUMPTION("consumption");

  private final String name;

  SeriesDisplayMode(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
