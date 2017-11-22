package com.elvaco.mvp.dialect.types;

public enum Types {
  PropertyCollection("property-collection"),
  MeasurementUnit("measurement-unit");
  private final String name;

  Types(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

}
