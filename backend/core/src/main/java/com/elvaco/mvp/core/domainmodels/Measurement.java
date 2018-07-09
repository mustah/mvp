package com.elvaco.mvp.core.domainmodels;

import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.annotation.Nullable;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Builder
@EqualsAndHashCode(doNotUseGetters = true)
@ToString
public class Measurement implements Identifiable<Long>, Serializable {

  private static final long serialVersionUID = 7241986426136993573L;

  @Nullable
  public final Long id;
  public final ZonedDateTime created;
  public final String quantity;
  public final double value;
  public final String unit;
  public final PhysicalMeter physicalMeter;

  public Measurement(
    @Nullable Long id,
    ZonedDateTime created,
    String quantity,
    double value,
    String unit,
    PhysicalMeter physicalMeter
  ) {
    this.id = id;
    this.created = created;
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

  public Quantity getQuantity() {
    return new Quantity(quantity, unit);
  }

  public Measurement withValue(double value) {
    return new Measurement(
      id,
      created,
      quantity,
      value,
      unit,
      physicalMeter
    );
  }

  public Measurement withUnit(String unit) {
    return new Measurement(
      id,
      created,
      quantity,
      value,
      unit,
      physicalMeter
    );
  }

  public Measurement withQuantity(String quantity) {
    return new Measurement(
      id,
      created,
      quantity,
      value,
      unit,
      physicalMeter
    );
  }
}
