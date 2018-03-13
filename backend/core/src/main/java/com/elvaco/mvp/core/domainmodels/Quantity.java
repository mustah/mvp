package com.elvaco.mvp.core.domainmodels;

import java.util.Optional;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
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

  private static final String QUANTITY_UNIT_DELIMITER = ":";

  public final String name;

  @Nullable
  public final String unit;

  @Nullable
  public final Long id;

  public Quantity(@Nullable Long id, String name, @Nullable String unit) {
    this.id = id;
    this.name = name;
    this.unit = unit;
  }

  public Quantity(String name, @Nullable String unit) {
    this(null, name, unit);
  }

  public Quantity(String name) {
    this(null, name, null);
  }

  public static Quantity of(String quantityUnitPair) {
    String[] parts = quantityUnitPair.split(QUANTITY_UNIT_DELIMITER);
    String quantityName = parts[0];
    if (quantityName.isEmpty() || parts.length > 2) {
      throw new RuntimeException("Invalid quantity/unit pair: '" + quantityUnitPair + "'");
    } else if (parts.length == 2) {
      return new Quantity(quantityName, parts[1]);
    } else {
      return new Quantity(quantityName);
    }
  }

  public Optional<String> unit() {
    return Optional.ofNullable(unit);
  }

  public Quantity withUnit(String unit) {
    return new Quantity(id, name, unit);
  }
}
