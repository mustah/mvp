package com.elvaco.mvp.core.domainmodels;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

@EqualsAndHashCode
@ToString
public class PhysicalMeter implements Identifiable<Long> {

  @Nullable
  public final Long id;
  public final Organisation organisation;
  public final String address;
  public final String externalId;
  public final String medium;
  @Nullable
  public final UUID logicalMeterId;
  public final String manufacturer;
  public final List<MeterStatusLog> meterStatusLogs;
  private final List<Measurement> measurements;

  public PhysicalMeter(
    @Nullable Long id,
    Organisation organisation,
    String address,
    String externalId,
    String medium,
    String manufacturer,
    @Nullable UUID logicalMeterId,
    List<MeterStatusLog> meterStatusLogs
  ) {
    this.id = id;
    this.organisation = organisation;
    this.address = address;
    this.externalId = externalId;
    this.medium = medium;
    this.manufacturer = manufacturer;
    this.logicalMeterId = logicalMeterId;
    this.measurements = new ArrayList<>();
    this.meterStatusLogs = unmodifiableList(meterStatusLogs);
  }

  public PhysicalMeter(
    Organisation organisation,
    String address,
    String externalId,
    String medium,
    String manufacturer
  ) {
    this(
      null,
      organisation,
      address,
      externalId,
      medium,
      manufacturer,
      null,
      emptyList()
    );
  }

  public PhysicalMeter(
    @Nullable Long id,
    Organisation organisation,
    String address,
    String externalId,
    String medium,
    String manufacturer
  ) {
    this(
      id,
      organisation,
      address,
      externalId,
      medium,
      manufacturer,
      null,
      emptyList()
    );
  }

  public PhysicalMeter(
    Organisation organisation,
    String address,
    String externalId,
    String medium,
    String manufacturer,
    UUID logicalMeterId,
    List<MeterStatusLog> meterStatusLogs
  ) {
    this(
      null,
      organisation,
      address,
      externalId,
      medium,
      manufacturer,
      logicalMeterId,
      meterStatusLogs
    );
  }

  @Nullable
  @Override
  public Long getId() {
    return id;
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
      meterStatusLogs
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
      meterStatusLogs
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
      meterStatusLogs
    );
  }
}
