package com.elvaco.mvp.core.domainmodels;

import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class Quantity {
  public static final Quantity VOLUME = new Quantity("Volume", "m³");
  public static final Quantity FLOW = new Quantity("Flow", "m³/h");
  public static final Quantity TEMPERATURE = new Quantity("Temperature", "°C");
  public static final Quantity ENERGY = new Quantity("Energy", "kWh");
  public static final Quantity POWER = new Quantity("Power", "W");
  public static final Quantity FORWARD_TEMPERATURE = new Quantity(
    "Forward temperature", "°C");
  public static final Quantity RETURN_TEMPERATURE = new Quantity(
    "Return temperature", "°C");
  public static final Quantity DIFFERENCE_TEMPERATURE = new Quantity(
    "Difference temperature", "°K");

  public final String name;
  public final String unit;

  @Nullable
  public final Long id;

  public Quantity(@Nullable Long id, String name, String unit) {
    this.id = id;
    this.name = name;
    this.unit = unit;
  }

  private Quantity(String name, String unit) {
    this(null, name, unit);
  }
}
