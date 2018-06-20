package com.elvaco.mvp.database.repository.mappers;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PagedLogicalMeter;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import lombok.experimental.UtilityClass;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@UtilityClass
public class LogicalMeterEntityMapper {

  public static LogicalMeter toDomainModelWithCollectionPercentage(
    PagedLogicalMeter pagedLogicalMeter,
    long expectedMeasurementCount
  ) {
    return newLogicalMeter(pagedLogicalMeter, expectedMeasurementCount);
  }

  public static LogicalMeter toDomainModel(LogicalMeterEntity logicalMeterEntity) {
    List<PhysicalMeter> physicalMeters = toPhysicalMeters(logicalMeterEntity);
    return toLogicalMeter(logicalMeterEntity, physicalMeters, null, null);
  }

  public static LogicalMeter toDomainModel(PagedLogicalMeter pagedLogicalMeter) {
    return newLogicalMeter(pagedLogicalMeter, null);
  }

  public static LogicalMeter toDomainModel(
    LogicalMeterEntity logicalMeterEntity,
    Map<UUID, List<PhysicalMeterStatusLogEntity>> meterStatusMap,
    @Nullable Long expectedMeasurementCount,
    @Nullable Long actualMeasurementCount
  ) {

    List<PhysicalMeter> physicalMeters = logicalMeterEntity.physicalMeters.stream()
      .map(physicalMeterEntity ->
        PhysicalMeterEntityMapper.toDomainModel(
          physicalMeterEntity,
          meterStatusMap.getOrDefault(physicalMeterEntity.getId(), emptyList())
        ))
      .collect(toList());

    return toLogicalMeter(
      logicalMeterEntity,
      physicalMeters,
      expectedMeasurementCount,
      actualMeasurementCount
    );
  }

  public static LogicalMeter toDomainModel(
    LogicalMeterEntity logicalMeterEntity,
    Map<UUID, List<PhysicalMeterStatusLogEntity>> mappedStatuses
  ) {
    return toDomainModel(logicalMeterEntity, mappedStatuses, null, null);
  }

  public static LogicalMeterEntity toEntity(LogicalMeter logicalMeter) {
    LogicalMeterEntity logicalMeterEntity = new LogicalMeterEntity(
      logicalMeter.id,
      logicalMeter.externalId,
      logicalMeter.organisationId,
      logicalMeter.created,
      MeterDefinitionEntityMapper.toEntity(logicalMeter.meterDefinition)
    );

    logicalMeterEntity.location = LocationEntityMapper.toEntity(
      logicalMeter.id,
      logicalMeter.location
    );

    logicalMeterEntity.physicalMeters = logicalMeter.physicalMeters.stream()
      .map(PhysicalMeterEntityMapper::toEntity)
      .collect(toSet());

    logicalMeterEntity.gateways = logicalMeter.gateways.stream()
      .map(GatewayEntityMapper::toEntity)
      .collect(toSet());

    return logicalMeterEntity;
  }

  private static List<PhysicalMeter> toPhysicalMeters(LogicalMeterEntity logicalMeterEntity) {
    return logicalMeterEntity.physicalMeters.stream()
      .map(PhysicalMeterEntityMapper::toDomainModel)
      .collect(toList());
  }

  private static LogicalMeter newLogicalMeter(
    PagedLogicalMeter pagedLogicalMeter,
    @Nullable Long expectedMeasurementCount
  ) {
    List<Gateway> gateways = Optional.ofNullable(pagedLogicalMeter.gateway)
      .map(gateway -> singletonList(GatewayEntityMapper.toDomainModelWithoutStatusLogs(gateway)))
      .orElse(emptyList());

    List<PhysicalMeter> physicalMeters = Optional.ofNullable(pagedLogicalMeter.activePhysicalMeter)
      .map(meter -> singletonList(PhysicalMeterEntityMapper.toDomainModelWithoutStatusLogs(meter)))
      .orElse(emptyList());

    return new LogicalMeter(
      pagedLogicalMeter.id,
      pagedLogicalMeter.externalId,
      pagedLogicalMeter.organisationId,
      MeterDefinitionEntityMapper.toDomainModel(pagedLogicalMeter.meterDefinition),
      pagedLogicalMeter.created,
      physicalMeters,
      gateways,
      emptyList(),
      LocationEntityMapper.toDomainModel(pagedLogicalMeter.location),
      expectedMeasurementCount,
      pagedLogicalMeter.measurementCount,
      Optional.ofNullable(pagedLogicalMeter.currentStatus)
        .map(MeterStatusLogEntityMapper::toDomainModel)
        .orElse(null)
    );
  }

  private static LogicalMeter toLogicalMeter(
    LogicalMeterEntity logicalMeterEntity,
    List<PhysicalMeter> physicalMeters,
    @Nullable Long expectedMeasurementCount,
    @Nullable Long actualMeasurementCount
  ) {
    return new LogicalMeter(
      logicalMeterEntity.id,
      logicalMeterEntity.externalId,
      logicalMeterEntity.organisationId,
      MeterDefinitionEntityMapper.toDomainModel(logicalMeterEntity.meterDefinition),
      logicalMeterEntity.created,
      physicalMeters,
      toGateways(logicalMeterEntity.gateways),
      emptyList(),
      LocationEntityMapper.toDomainModel(logicalMeterEntity.location),
      expectedMeasurementCount,
      actualMeasurementCount,
      null
    );
  }

  private static List<Gateway> toGateways(Set<GatewayEntity> gateways) {
    return gateways
      .stream()
      .map(GatewayEntityMapper::toDomainModelWithoutStatusLogs)
      .collect(toList());
  }
}
