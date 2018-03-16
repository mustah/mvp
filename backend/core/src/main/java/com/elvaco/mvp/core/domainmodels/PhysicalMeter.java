package com.elvaco.mvp.core.domainmodels;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import static java.util.Collections.emptyList;

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
  public final long readIntervalMinutes;
  public final Long measurementCount;
  public final List<MeterStatusLog> statuses;

  public PhysicalMeter(
    UUID id,
    Organisation organisation,
    String address,
    String externalId,
    String medium,
    String manufacturer,
    @Nullable UUID logicalMeterId,
    long readIntervalMinutes,
    @Nullable Long measurementCount,
    List<MeterStatusLog> statuses
  ) {
    this.id = id;
    this.organisation = organisation;
    this.address = address;
    this.externalId = externalId;
    this.medium = medium;
    this.manufacturer = manufacturer;
    this.logicalMeterId = logicalMeterId;
    this.measurements = new ArrayList<>();
    this.readIntervalMinutes = readIntervalMinutes;
    this.measurementCount = measurementCount;
    this.statuses = statuses;
  }

  public PhysicalMeter(
    UUID id,
    Organisation organisation,
    String address,
    String externalId,
    String medium,
    String manufacturer,
    @Nullable UUID logicalMeterId,
    long readIntervalMinutes,
    Long measurementCount
  ) {
    this(
      id,
      organisation,
      address,
      externalId,
      medium,
      manufacturer,
      logicalMeterId,
      readIntervalMinutes,
      measurementCount,
      emptyList()
    );
  }

  public PhysicalMeter(
    UUID id,
    String address,
    String externalId,
    String medium,
    String manufacturer,
    Organisation organisation,
    long readIntervalMinutes
  ) {
    this(
      id,
      organisation,
      address,
      externalId,
      medium,
      manufacturer,
      null,
      readIntervalMinutes,
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
    long readIntervalMinutes
  ) {
    this(id, address, externalId, medium, manufacturer, organisation, readIntervalMinutes);
  }

  @Override
  public UUID getId() {
    return id;
  }

  public long getMeasurementCountOrZero() {
    return measurementCount != null ? measurementCount : 0;
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
      readIntervalMinutes,
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
      readIntervalMinutes,
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
      readIntervalMinutes,
      measurementCount
    );
  }

}
