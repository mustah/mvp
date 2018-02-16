package com.elvaco.mvp.core.domainmodels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import static java.util.Collections.unmodifiableList;

@EqualsAndHashCode
@ToString
public class PhysicalMeter {

  @Nullable
  public final Long id;
  public final Organisation organisation;
  public final String identity;
  public final String medium;
  @Nullable
  public final Long logicalMeterId;
  public final String manufacturer;
  private final List<Measurement> measurements;
  public final List<MeterStatusLog> meterStatusLogs;

  public PhysicalMeter(
    @Nullable Long id,
    Organisation organisation,
    String identity,
    String medium,
    String manufacturer,
    @Nullable Long logicalMeterId,
    List<MeterStatusLog> meterStatusLogs
  ) {
    this.id = id;
    this.organisation = organisation;
    this.identity = identity;
    this.medium = medium;
    this.manufacturer = manufacturer;
    this.logicalMeterId = logicalMeterId;
    this.measurements = new ArrayList<>();
    this.meterStatusLogs = unmodifiableList(meterStatusLogs);
  }

  public PhysicalMeter(
    Organisation organisation,
    String identity,
    String medium,
    String manufacturer
  ) {
    this(
      null,
      organisation,
      identity,
      medium,
      manufacturer,
      null,
      Collections.emptyList()
    );
  }

  public PhysicalMeter(
    @Nullable Long id,
    Organisation organisation,
    String identity,
    String medium,
    String manufacturer
  ) {
    this(id, organisation, identity, medium, manufacturer, null, Collections.emptyList());
  }

  public PhysicalMeter(
    Organisation organisation,
    String identity,
    String medium,
    String manufacturer,
    Long logicalMeterId,
    List<MeterStatusLog> meterStatusLogs
  ) {
    this(
      null,
      organisation,
      identity,
      medium,
      manufacturer,
      logicalMeterId,
      meterStatusLogs
    );
  }
}
