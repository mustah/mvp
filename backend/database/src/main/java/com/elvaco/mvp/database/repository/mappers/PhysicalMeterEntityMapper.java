package com.elvaco.mvp.database.repository.mappers;

import java.util.Collection;
import java.util.List;
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

  public static List<PhysicalMeter> toDomainModelsWithoutStatusLogs(
    Collection<PhysicalMeterEntity> entities
  ) {
    return entities.stream()
      .map(PhysicalMeterEntityMapper::toDomainModelWithoutStatusLogs)
      .collect(toList());
  }

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
      .statuses(statuses.stream().map(MeterStatusLogEntityMapper::toDomainModel).collect(toList()))
      .alarms(toAlarms(entity))
      .build();
  }

  public static PhysicalMeterEntity toEntity(PhysicalMeter domainModel) {
    return new PhysicalMeterEntity(
      domainModel.id,
      domainModel.organisationId,
      domainModel.address,
      domainModel.externalId,
      domainModel.medium,
      domainModel.manufacturer,
      domainModel.logicalMeterId,
      domainModel.activePeriod,
      domainModel.readIntervalMinutes,
      domainModel.revision,
      domainModel.mbusDeviceType,
      domainModel.statuses.stream().map(MeterStatusLogEntityMapper::toEntity).collect(toSet()),
      domainModel.alarms.stream().map(MeterAlarmLogEntityMapper::toEntity).collect(toSet())
    );
  }

  static List<PhysicalMeter> toDomainModels(Collection<PhysicalMeterEntity> entities) {
    return entities.stream()
      .map(PhysicalMeterEntityMapper::toDomainModel)
      .collect(toList());
  }

  private static Set<AlarmLogEntry> toAlarms(PhysicalMeterEntity entity) {
    return entity.alarms.stream()
      .map(MeterAlarmLogEntityMapper::toDomainModel)
      .collect(toSet());
  }

  private static PhysicalMeter.PhysicalMeterBuilder physicalMeterBuilderFrom(
    PhysicalMeterEntity entity
  ) {
    return PhysicalMeter.builder()
      .id(entity.id)
      .organisationId(entity.getOrganisationId())
      .address(entity.address)
      .externalId(entity.externalId)
      .medium(entity.medium)
      .manufacturer(entity.manufacturer)
      .logicalMeterId(entity.getLogicalMeterId())
      .readIntervalMinutes(entity.readIntervalMinutes)
      .revision(entity.revision)
      .mbusDeviceType(entity.mbusDeviceType)
      .activePeriod(entity.activePeriod);
  }
}
