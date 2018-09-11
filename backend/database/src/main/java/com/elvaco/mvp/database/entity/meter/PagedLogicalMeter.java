package com.elvaco.mvp.database.entity.meter;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.SelectionPeriod;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;

import static com.elvaco.mvp.core.util.LogicalMeterHelper.calculateExpectedReadOuts;

public class PagedLogicalMeter {

  public final UUID id;
  public final UUID organisationId;
  public final String externalId;
  public final ZonedDateTime created;
  public final MeterDefinitionEntity meterDefinition;
  public final GatewayEntity gateway;
  public final LocationEntity location;
  public final PhysicalMeterEntity activePhysicalMeter;
  public final Long missingReadingCount;
  public final MeterAlarmLogEntity alarm;

  public PagedLogicalMeter(
    UUID id,
    UUID organisationId,
    String externalId,
    ZonedDateTime created,
    MeterDefinitionEntity meterDefinition,
    String country,
    String city,
    String streetAddress,
    PhysicalMeterEntity activePhysicalMeter,
    GatewayEntity gateway
  ) {
    this(
      id,
      organisationId,
      externalId,
      created,
      meterDefinition,
      new LocationEntity(id, country, city, streetAddress),
      activePhysicalMeter,
      gateway,
      0L,
      null
    );
  }

  private PagedLogicalMeter(
    UUID id,
    UUID organisationId,
    String externalId,
    ZonedDateTime created,
    MeterDefinitionEntity meterDefinition,
    LocationEntity location,
    @Nullable PhysicalMeterEntity activePhysicalMeter,
    @Nullable GatewayEntity gateway,
    @Nullable Long missingReadingCount,
    @Nullable MeterAlarmLogEntity alarm
  ) {
    this.id = id;
    this.organisationId = organisationId;
    this.externalId = externalId;
    this.created = created;
    this.meterDefinition = meterDefinition;
    this.location = location;
    this.activePhysicalMeter = activePhysicalMeter;
    this.gateway = gateway;
    this.missingReadingCount = missingReadingCount;
    this.alarm = alarm;
  }

  public long expectedReadingCount(SelectionPeriod selectionPeriod) {
    return Optional.ofNullable(activePhysicalMeter)
      .map(meter -> calculateExpectedReadOuts(meter.readIntervalMinutes, selectionPeriod))
      .orElse(0L);
  }

  public PagedLogicalMeter withMetaData(
    @Nullable Long missingReadingCount,
    @Nullable MeterAlarmLogEntity alarm
  ) {
    return new PagedLogicalMeter(
      id,
      organisationId,
      externalId,
      created,
      meterDefinition,
      location,
      activePhysicalMeter,
      gateway,
      missingReadingCount,
      alarm
    );
  }
}
