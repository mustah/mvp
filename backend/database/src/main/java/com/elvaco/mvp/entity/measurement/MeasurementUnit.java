package com.elvaco.mvp.entity.measurement;

public class MeasurementUnit {
  private double value;
  private String unit;

  public MeasurementUnit() {}

  public MeasurementUnit(String valueUnit) {
    int i = valueUnit.lastIndexOf(' ');
    if (i < 0) {
      throw new IllegalArgumentException(valueUnit);
    }
    String[] parts = {valueUnit.substring(0, i), valueUnit.substring(i + 1)};
    try {
      value = Double.parseDouble(parts[0]);
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException(String.format("Not a number: %s", valueUnit));
    }
    unit = parts[1];
  }

  public MeasurementUnit(String unit, double value) {
    this.unit = unit;
    this.value = value;
  }

  @Override
  public String toString() {
    if (value == (long) value) {
      return String.format("%d %s", (long) value, unit);
    } else {
      return String.format("%s %s", value, unit);
    }
  }

  public double getValue() {
    return value;
  }

  public String getUnit() {
    return unit;
  }
}
