package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Builder
@EqualsAndHashCode(doNotUseGetters = true)
@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Measurement implements Identifiable<Measurement.Id> {

  public final ZonedDateTime created;
  public final String quantity;
  public final Double value;
  public final String unit;
  public final PhysicalMeter physicalMeter;

  public static Measurement.Id idOf(
    ZonedDateTime created,
    String quantity,
    PhysicalMeter physicalMeter
  ) {
    return new Measurement.Id(created, quantity, physicalMeter);
  }

  public Quantity getQuantity() {
    return new Quantity(null, quantity, unit, DisplayMode.UNKNOWN);
  }

  @Override
  public Measurement.Id getId() {
    return idOf(created, quantity, physicalMeter);
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
