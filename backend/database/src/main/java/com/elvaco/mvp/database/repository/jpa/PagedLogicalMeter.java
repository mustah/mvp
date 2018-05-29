package com.elvaco.mvp.database.repository.jpa;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.core.util.LogicalMeterHelper;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;

public class PagedLogicalMeter {

  public final UUID id;
  public final UUID organisationId;
  public final String externalId;
  public final ZonedDateTime created;
  public final MeterDefinitionEntity meterDefinition;
  public final PhysicalMeterStatusLogEntity currentStatus;
  public final LocationEntity location;
  public final Long readIntervalMinutes;
  public final Long measurementCount;

  public PagedLogicalMeter(
    UUID id,
    UUID organisationId,
    String externalId,
    ZonedDateTime created,
    MeterDefinitionEntity meterDefinition,
    String country,
    String city,
    String streetAddress,
    Long readIntervalMinutes
  ) {
    this.id = id;
    this.organisationId = organisationId;
    this.externalId = externalId;
    this.created = created;
    this.meterDefinition = meterDefinition;
    this.location = new LocationEntity(id, country, city, streetAddress);
    this.readIntervalMinutes = readIntervalMinutes;
    currentStatus = null;
    measurementCount = 0L;
  }

  public PagedLogicalMeter(
    UUID id,
    UUID organisationId,
    String externalId,
    ZonedDateTime created,
    MeterDefinitionEntity meterDefinition,
    LocationEntity location,
    Long readIntervalMinutes,
    PhysicalMeterStatusLogEntity currentStatus,
    Long measurementCount
  ) {
    this.id = id;
    this.organisationId = organisationId;
    this.externalId = externalId;
    this.created = created;
    this.meterDefinition = meterDefinition;
    this.location = location;
    this.readIntervalMinutes = readIntervalMinutes;
    this.currentStatus = currentStatus;
    this.measurementCount = measurementCount;
  }

  public long expectedMeasurementCount(ZonedDateTime after, ZonedDateTime before) {
    return (long) LogicalMeterHelper.calculateExpectedReadOuts(
      readIntervalMinutes,
      after,
      before
    ) * meterDefinition.quantities.size();
  }

  PagedLogicalMeter withMeasurementCount(Long measurementCount) {
    return new PagedLogicalMeter(
      id,
      organisationId,
      externalId,
      created,
      meterDefinition,
      location,
      readIntervalMinutes,
      currentStatus,
      measurementCount
    );
  }

  PagedLogicalMeter withCurrentStatus(PhysicalMeterStatusLogEntity currentStatus) {
    return new PagedLogicalMeter(
      id,
      organisationId,
      externalId,
      created,
      meterDefinition,
      location,
      readIntervalMinutes,
      currentStatus,
      measurementCount
    );
  }

}
