package com.elvaco.mvp.database.repository.mappers;

import java.util.Collection;
import java.util.Set;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import lombok.experimental.UtilityClass;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@UtilityClass
public class PhysicalMeterEntityMapper {

  public static PhysicalMeter toDomainModelWithoutStatusLogs(PhysicalMeterEntity entity) {
    return physicalMeterBuilderFrom(entity).build();
  }

  public static PhysicalMeter toDomainModel(PhysicalMeterEntity entity) {
    return toDomainModel(entity, entity.statusLogs);
  }

  public static PhysicalMeter toDomainModel(
    PhysicalMeterEntity entity,
    Collection<PhysicalMeterStatusLogEntity> statuses
  ) {
    return physicalMeterBuilderFrom(entity)
      .statuses(statuses.stream()
        .map(MeterStatusLogEntityMapper::toDomainModel).collect(toList()))
      .alarms(toAlarms(entity))
      .build();
  }

  public static PhysicalMeter toDomainModelWithAlarms(PhysicalMeterEntity entity) {
    return physicalMeterBuilderFrom(entity)
      .alarms(toAlarms(entity))
      .build();
  }

  public static PhysicalMeterEntity toEntity(PhysicalMeter domainModel) {
    return new PhysicalMeterEntity(
      domainModel.id,
      OrganisationEntityMapper.toEntity(domainModel.organisation),
      domainModel.address,
      domainModel.externalId,
      domainModel.medium,
      domainModel.manufacturer,
      domainModel.logicalMeterId,
      domainModel.readIntervalMinutes,
      domainModel.statuses.stream().map(MeterStatusLogEntityMapper::toEntity).collect(toSet()),
      domainModel.alarms.stream().map(MeterAlarmLogEntityMapper::toEntity).collect(toSet())
    );
  }

  private static Set<AlarmLogEntry> toAlarms(PhysicalMeterEntity entity) {
    return entity.alarms.stream()
      .map(MeterAlarmLogEntityMapper::toDomainModel).collect(toSet());
  }

  private static PhysicalMeter.PhysicalMeterBuilder physicalMeterBuilderFrom(
    PhysicalMeterEntity entity
  ) {
    return PhysicalMeter.builder()
      .id(entity.id)
      .organisation(OrganisationEntityMapper.toDomainModel(entity.organisation))
      .address(entity.address)
      .externalId(entity.externalId)
      .medium(entity.medium)
      .manufacturer(entity.manufacturer)
      .logicalMeterId(entity.logicalMeterId)
      .readIntervalMinutes(entity.readIntervalMinutes);
  }
}
