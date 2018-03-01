package com.elvaco.mvp.core.domainmodels;

import java.time.Instant;
import java.util.Date;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class Measurement implements Identifiable<Long> {

  @Nullable
  public final Long id;
  public final Date created;
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
    this(null, Date.from(Instant.now()), quantity.name, value, unit, physicalMeter);
  }

  public Measurement(
    @Nullable Long id,
    Date created,
    String quantity,
    double value,
    String unit,
    PhysicalMeter physicalMeter
  ) {
    this.id = id;
    this.created = new Date(created.getTime());
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
