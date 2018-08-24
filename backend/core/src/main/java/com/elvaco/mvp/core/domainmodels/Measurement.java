package com.elvaco.mvp.core.domainmodels;

import java.io.Serializable;
import java.time.ZonedDateTime;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Builder
@EqualsAndHashCode(doNotUseGetters = true)
@ToString
public class Measurement implements Identifiable<Measurement.Id>, Serializable {

  private static final long serialVersionUID = 7241986426136993573L;

  public final ZonedDateTime created;
  public final String quantity;
  public final double value;
  public final String unit;
  public final PhysicalMeter physicalMeter;

  public Measurement(
    ZonedDateTime created,
    String quantity,
    double value,
    String unit,
    PhysicalMeter physicalMeter
  ) {
    this.created = created;
    this.quantity = quantity;
    this.value = value;
    this.unit = unit;
    this.physicalMeter = physicalMeter;
  }

  public Quantity getQuantity() {
    return new Quantity(quantity, unit);
  }

  public Measurement withValue(double value) {
    return new Measurement(
      created,
      quantity,
      value,
      unit,
      physicalMeter
    );
  }

  public Measurement withUnit(String unit) {
    return new Measurement(
      created,
      quantity,
      value,
      unit,
      physicalMeter
    );
  }

  public Measurement withQuantity(String quantity) {
    return new Measurement(
      created,
      quantity,
      value,
      unit,
      physicalMeter
    );
  }

  @Override
  public Measurement.Id getId() {
    return idOf(created, quantity, physicalMeter);
  }

  public static Measurement.Id idOf(
    ZonedDateTime created,
    String quantity,
    PhysicalMeter physicalMeter
  ) {
    return new Measurement.Id(created, quantity, physicalMeter);
  }

  @ToString
  @EqualsAndHashCode
  public static class Id {

    public final ZonedDateTime created;
    public final String quantity;
    public final PhysicalMeter physicalMeter;

    private Id(ZonedDateTime created, String quantity, PhysicalMeter physicalMeter) {
      this.created = created;
      this.quantity = quantity;
      this.physicalMeter = physicalMeter;
    }
  }
}
