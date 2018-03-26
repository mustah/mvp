package com.elvaco.mvp.web.mapper;

import java.time.Instant;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.Quantity;

public class LabeledMeasurementValue {

  public final String label;
  public final Instant when;
  public final Double value;
  public final Quantity quantity;

  public LabeledMeasurementValue(
    String label,
    Instant when,
    Double value,
    Quantity quantity
  ) {
    this.label = label;
    this.when = when;
    this.value = value;
    this.quantity = quantity;
  }

  public static LabeledMeasurementValue from(Measurement measurement) {
    return new LabeledMeasurementValue(
      measurement.physicalMeter.externalId,
      measurement.created.toInstant(),
      measurement.value,
      measurement.getQuantity()
    );
  }
}
