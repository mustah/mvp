package com.elvaco.mvp.database.repository.mappers;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import lombok.experimental.UtilityClass;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@UtilityClass
public class LogicalMeterMapper {

  public static LogicalMeter toDomainModel(LogicalMeterEntity logicalMeterEntity) {
    List<PhysicalMeter> physicalMeters = logicalMeterEntity.physicalMeters
      .stream()
      .map(PhysicalMeterMapper::toDomainModel)
      .collect(toList());
    return toLogicalMeter(logicalMeterEntity, physicalMeters);
  }

  public static LogicalMeter toDomainModel(
    LogicalMeterEntity logicalMeterEntity,
    Map<UUID, List<PhysicalMeterStatusLogEntity>> meterStatusMap,
    Map<UUID, Long> meterMeasurementCount
  ) {

    List<PhysicalMeter> physicalMeters = logicalMeterEntity.physicalMeters
      .stream()
      .map(physicalMeterEntity -> PhysicalMeterMapper.toDomainModel(
        physicalMeterEntity,
        meterMeasurementCount.get(physicalMeterEntity.getId()),
        meterStatusMap.getOrDefault(physicalMeterEntity.getId(), emptyList())
      ))
      .collect(toList());

    return toLogicalMeter(
      logicalMeterEntity,
      physicalMeters
    );
  }

  private static LogicalMeter toLogicalMeter(
    LogicalMeterEntity logicalMeterEntity,
    List<PhysicalMeter> physicalMeters
  ) {
    return new LogicalMeter(
      logicalMeterEntity.id,
      logicalMeterEntity.externalId,
      logicalMeterEntity.organisationId,
      LocationMapper.toDomainModel(logicalMeterEntity.location),
      logicalMeterEntity.created,
      physicalMeters,
      MeterDefinitionMapper.toDomainModel(logicalMeterEntity.meterDefinition),
      toGateways(logicalMeterEntity.gateways)
    );
  }

  private static List<Gateway> toGateways(Set<GatewayEntity> gateways) {
    return gateways
      .stream()
      .map(GatewayMapper::toDomainModelWithoutStatusLogs)
      .collect(toList());
  }

  public static LogicalMeterEntity toEntity(LogicalMeter logicalMeter) {
    LogicalMeterEntity logicalMeterEntity = new LogicalMeterEntity(
      logicalMeter.id,
      logicalMeter.externalId,
      logicalMeter.organisationId,
      logicalMeter.created,
      MeterDefinitionMapper.toEntity(logicalMeter.meterDefinition)
    );

    logicalMeterEntity.location = LocationMapper.toEntity(logicalMeter.id, logicalMeter.location);

    logicalMeterEntity.physicalMeters = logicalMeter.physicalMeters
      .stream()
      .map(PhysicalMeterMapper::toEntity)
      .collect(toSet());

    logicalMeterEntity.gateways = logicalMeter.gateways
      .stream()
      .map(GatewayMapper::toEntity)
      .collect(toSet());

    return logicalMeterEntity;
  }

  public static LogicalMeter justLocationModel(LogicalMeterEntity entity) {
    return new LogicalMeter(
      entity.getId(),
      entity.externalId,
      entity.organisationId,
      LocationMapper.toDomainModel(entity.location),
      entity.created
    );
  }
}
