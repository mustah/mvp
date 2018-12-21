package com.elvaco.mvp.web.mapper;

import java.time.Instant;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.Quantity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LabeledMeasurementValue {

  public final String id;
  public final String label;
  @Nullable
  public final String city;
  @Nullable
  public final String address;
  @Nullable
  public final String medium;
  public final Instant when;
  public final Double value;
  public final Quantity quantity;

  public static LabeledMeasurementValue of(
    Measurement measurement,
    UUID logicalMeterId,
    @Nullable String city,
    @Nullable String address,
    @Nullable String medium
  ) {
    return LabeledMeasurementValue.builder()
      .id(logicalMeterId.toString())
      .label(measurement.physicalMeter.externalId)
      .city(city)
      .address(address)
      .medium(medium)
      .when(measurement.created.toInstant())
      .value(measurement.value)
      .quantity(measurement.getQuantity())
      .build();
  }
}
