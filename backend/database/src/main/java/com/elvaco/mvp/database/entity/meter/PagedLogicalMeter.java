package com.elvaco.mvp.database.entity.meter;

import java.time.ZonedDateTime;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.SelectionPeriod;
import com.elvaco.mvp.core.util.LogicalMeterHelper;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;

public class PagedLogicalMeter {

  public final UUID id;
  public final UUID organisationId;
  public final String externalId;
  public final ZonedDateTime created;
  public final MeterDefinitionEntity meterDefinition;
  public final GatewayEntity gateway;
  public final PhysicalMeterStatusLogEntity currentStatus;
  public final LocationEntity location;
  public final PhysicalMeterEntity activePhysicalMeter;
  public final Long missingReadingCount;

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
      null,
      0L
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
    @Nullable PhysicalMeterStatusLogEntity currentStatus,
    @Nullable Long missingReadingCount
  ) {
    this.id = id;
    this.organisationId = organisationId;
    this.externalId = externalId;
    this.created = created;
    this.meterDefinition = meterDefinition;
    this.location = location;
    this.activePhysicalMeter = activePhysicalMeter;
    this.gateway = gateway;
    this.currentStatus = currentStatus;
    this.missingReadingCount = missingReadingCount;
  }

  public long expectedReadingCount(SelectionPeriod selectionPeriod) {
    if (activePhysicalMeter == null) {
      return 0;
    }

    return LogicalMeterHelper.calculateExpectedReadOuts(
      activePhysicalMeter.readIntervalMinutes,
      selectionPeriod
    );
  }

  public PagedLogicalMeter withMissingReadingCount(@Nullable Long missingReadingCount) {
    return new PagedLogicalMeter(
      id,
      organisationId,
      externalId,
      created,
      meterDefinition,
      location,
      activePhysicalMeter,
      gateway,
      currentStatus,
      missingReadingCount
    );
  }

  public PagedLogicalMeter withCurrentStatus(PhysicalMeterStatusLogEntity currentStatus) {
    return new PagedLogicalMeter(
      id,
      organisationId,
      externalId,
      created,
      meterDefinition,
      location,
      activePhysicalMeter,
      gateway,
      currentStatus,
      missingReadingCount
    );
  }
}
