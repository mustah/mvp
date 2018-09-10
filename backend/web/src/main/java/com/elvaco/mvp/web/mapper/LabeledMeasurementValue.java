package com.elvaco.mvp.web.mapper;

import java.time.Instant;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.Quantity;

public class LabeledMeasurementValue {

  public final String id;
  public final String label;
  public final String city;
  public final String address;
  public final String medium;
  public final Instant when;
  public final Double value;
  public final Quantity quantity;

  public LabeledMeasurementValue(
    String id,
    String label,
    @Nullable String city,
    @Nullable String address,
    @Nullable String medium,
    Instant when,
    Double value,
    Quantity quantity
  ) {
    this.id = id;
    this.label = label;
    this.city = city;
    this.address = address;
    this.medium = medium;
    this.when = when;
    this.value = value;
    this.quantity = quantity;
  }

  public LabeledMeasurementValue(
    String id,
    String label,
    Instant when,
    Double value,
    Quantity quantity
  ) {
    this(id, label, null, null, null, when, value, quantity);
  }

  public static LabeledMeasurementValue of(
    Measurement measurement,
    UUID logicalMeterId
  ) {
    return of(measurement, logicalMeterId, null, null, null);
  }

  public static LabeledMeasurementValue of(
    Measurement measurement,
    UUID logicalMeterId,
    @Nullable String city,
    @Nullable String address,
    @Nullable String medium
  ) {
    return new LabeledMeasurementValue(
      logicalMeterId.toString(),
      measurement.physicalMeter.externalId,
      city,
      address,
      medium,
      measurement.created.toInstant(),
      measurement.value,
      measurement.getQuantity()
    );
  }
}
