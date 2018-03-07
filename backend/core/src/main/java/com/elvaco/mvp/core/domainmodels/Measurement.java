package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.Date;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class Measurement implements Identifiable<Long> {

  @Nullable
  public final Long id;
  public final ZonedDateTime created;
  public final String quantity;
  public final double value;
  public final String unit;
  public final PhysicalMeter physicalMeter;

  public Measurement(
    Quantity quantity,
    double value,
    String unit,
    PhysicalMeter physicalMeter
  ) {
    this(null, ZonedDateTime.now(), quantity.name, value, unit, physicalMeter);
  }

  public Measurement(
    @Nullable Long id,
    ZonedDateTime created,
    String quantity,
    double value,
    String unit,
    PhysicalMeter physicalMeter
  ) {
    this.id = id;
    this.created = ZonedDateTime.from(created);
    this.quantity = quantity;
    this.value = value;
    this.unit = unit;
    this.physicalMeter = physicalMeter;
  }

  @Nullable
  @Override
  public Long getId() {
    return id;
  }
}
