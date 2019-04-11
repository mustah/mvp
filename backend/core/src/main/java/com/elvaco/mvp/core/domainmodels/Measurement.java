package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.UUID;

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
  // TODO: rename to readoutTime
  public final ZonedDateTime readoutTime;
  public final ZonedDateTime receivedTime;
  public final ZonedDateTime expectedTime;
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
    return idOf(readoutTime, quantity, physicalMeter);
  }

  @ToString
  @EqualsAndHashCode
  public static class Id {
    public final UUID organisationId;
    public final ZonedDateTime created;
    public final String quantity;
    public final PhysicalMeter physicalMeter;

    private Id(ZonedDateTime created, String quantity, PhysicalMeter physicalMeter) {
      this.organisationId = physicalMeter.organisationId;
      this.created = created;
      this.quantity = quantity;
      this.physicalMeter = physicalMeter;
    }
  }
}
