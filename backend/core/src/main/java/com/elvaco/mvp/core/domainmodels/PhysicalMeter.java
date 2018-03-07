package com.elvaco.mvp.core.domainmodels;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class PhysicalMeter implements Identifiable<UUID> {

  public final UUID id;
  public final Organisation organisation;
  public final String address;
  public final String externalId;
  public final String medium;
  @Nullable
  public final UUID logicalMeterId;
  public final String manufacturer;
  private final List<Measurement> measurements;
  public final long readInterval;
  private final Long measurementCount;

  public PhysicalMeter(
    UUID id,
    Organisation organisation,
    String address,
    String externalId,
    String medium,
    String manufacturer,
    @Nullable UUID logicalMeterId,
    long readInterval,
    @Nullable Long measurementCount
  ) {
    this.id = id;
    this.organisation = organisation;
    this.address = address;
    this.externalId = externalId;
    this.medium = medium;
    this.manufacturer = manufacturer;
    this.logicalMeterId = logicalMeterId;
    this.measurements = new ArrayList<>();
    this.readInterval = readInterval;
    this.measurementCount = measurementCount;
  }

  public PhysicalMeter(
    UUID id,
    String address,
    String externalId,
    String medium,
    String manufacturer,
    Organisation organisation,
    long readInterval
  ) {
    this(
      id,
      organisation,
      address,
      externalId,
      medium,
      manufacturer,
      null,
      readInterval,
      null
    );
  }

  public PhysicalMeter(
    UUID id,
    Organisation organisation,
    String address,
    String externalId,
    String medium,
    String manufacturer,
    long readInterval
  ) {
    this(id, address, externalId, medium, manufacturer, organisation, readInterval);
  }

  @Override
  public UUID getId() {
    return id;
  }

  public Optional<Long> getMeasurementCount() {
    return Optional.ofNullable(measurementCount);
  }

  public PhysicalMeter withMedium(String medium) {
    return new PhysicalMeter(
      id,
      organisation,
      address,
      externalId,
      medium,
      manufacturer,
      logicalMeterId,
      readInterval,
      measurementCount
    );
  }

  public PhysicalMeter withManufacturer(String manufacturer) {
    return new PhysicalMeter(
      id,
      organisation,
      address,
      externalId,
      medium,
      manufacturer,
      logicalMeterId,
      readInterval,
      measurementCount
    );
  }

  public PhysicalMeter withLogicalMeterId(UUID logicalMeterId) {
    return new PhysicalMeter(
      id,
      organisation,
      address,
      externalId,
      medium,
      manufacturer,
      logicalMeterId,
      readInterval,
      measurementCount
    );
  }
}
