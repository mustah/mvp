package com.elvaco.mvp.database.entity.meter;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.SelectionPeriod;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.measurement.MissingMeasurementEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import static com.elvaco.mvp.core.util.LogicalMeterHelper.calculateExpectedReadOuts;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PagedLogicalMeter {

  public final UUID id;
  public final UUID organisationId;
  public final String externalId;
  public final ZonedDateTime created;
  public final MeterDefinitionEntity meterDefinition;
  public final LocationEntity location;
  public final PhysicalMeterEntity activePhysicalMeter;
  public final GatewayEntity gateway;
  public final Long missingReadingCount;
  public final MeterAlarmLogEntity alarm;
  public final PhysicalMeterStatusLogEntity status;

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
    GatewayEntity gateway,
    Set<MissingMeasurementEntity> missingMeasurements,
    Set<MeterAlarmLogEntity> alarms,
    Set<PhysicalMeterStatusLogEntity> statuses
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
      missingMeasurements.stream().distinct().count(),
      alarms.stream()
        .max(alarmLogComparator())
        .orElse(null),
      statuses.stream()
        .max(statusLogComparator())
        .orElse(null)
    );
  }

  public long expectedReadingCount(SelectionPeriod selectionPeriod) {
    return Optional.ofNullable(activePhysicalMeter)
      .map(meter -> calculateExpectedReadOuts(meter.readIntervalMinutes, selectionPeriod))
      .orElse(0L);
  }

  private static Comparator<MeterAlarmLogEntity> alarmLogComparator() {
    return Comparator
      .comparing((MeterAlarmLogEntity meterAlarmLogEntity) -> meterAlarmLogEntity.start)
      .thenComparing(meterAlarmLogEntity -> meterAlarmLogEntity.stop);
  }

  private static Comparator<PhysicalMeterStatusLogEntity> statusLogComparator() {
    return Comparator
      .comparing((PhysicalMeterStatusLogEntity meterAlarmLogEntity) -> meterAlarmLogEntity.start)
      .thenComparing(meterAlarmLogEntity -> meterAlarmLogEntity.stop);
  }
}
