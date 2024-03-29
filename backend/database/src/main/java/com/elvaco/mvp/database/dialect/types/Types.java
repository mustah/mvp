package com.elvaco.mvp.database.dialect.types;

public enum Types {

  JsonField("json-field"),
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
