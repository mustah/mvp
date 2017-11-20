package com.elvaco.mvp.entity.measurement;

public class MeasurementUnit {
  private double value;
  private String unit;

  /**
   *
   * @param valueUnit
   */
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
    return String.format("%f %s", value, unit);
  }

  public double getValue() {
    return value;
  }

  public String getUnit() {
    return unit;
  }
}
