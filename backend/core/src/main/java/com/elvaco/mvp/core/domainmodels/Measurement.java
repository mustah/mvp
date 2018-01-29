package com.elvaco.mvp.core.domainmodels;

import java.util.Date;
import javax.annotation.Nullable;

public class Measurement {
  @Nullable
  public final Long id;
  public final Date created;
  public final String quantity;
  public final double value;
  public final String unit;
  @Nullable
  public final PhysicalMeter physicalMeter;

  public Measurement(
    @Nullable Long id,
    Date created,
    String quantity,
    double value,
    String unit,
    @Nullable PhysicalMeter physicalMeter
  ) {
    this.id = id;
    this.created = new Date(created.getTime());
    this.quantity = quantity;
    this.value = value;
    this.unit = unit;
    this.physicalMeter = physicalMeter;
  }
}
