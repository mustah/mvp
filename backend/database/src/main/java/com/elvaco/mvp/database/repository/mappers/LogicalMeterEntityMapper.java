package com.elvaco.mvp.database.repository.mappers;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.CollectionStats;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.repository.jpa.PagedLogicalMeter;
import lombok.experimental.UtilityClass;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@UtilityClass
public class LogicalMeterEntityMapper {

  public static LogicalMeter toDomainModelWithCollectionPercentage(
    PagedLogicalMeter pagedLogicalMeter, long expectedMeasurementCount
  ) {
    return new LogicalMeter(
      pagedLogicalMeter.id,
      pagedLogicalMeter.externalId,
      pagedLogicalMeter.organisationId,
      LocationEntityMapper.toDomainModel(pagedLogicalMeter.location),
      pagedLogicalMeter.created,
      emptyList(),
      MeterDefinitionEntityMapper.toDomainModel(pagedLogicalMeter.meterDefinition),
      emptyList(),
      new CollectionStats(
        pagedLogicalMeter.measurementCount,
        expectedMeasurementCount
      ).getCollectionPercentage(),
      emptyList(),
      Optional.ofNullable(pagedLogicalMeter.currentStatus)
        .map(MeterStatusLogEntityMapper::toDomainModel).orElse(null)
    );
  }

  public static LogicalMeter toDomainModel(LogicalMeterEntity logicalMeterEntity) {
    List<PhysicalMeter> physicalMeters = logicalMeterEntity.physicalMeters
      .stream()
      .map(PhysicalMeterEntityMapper::toDomainModel)
      .collect(toList());
    return toLogicalMeter(logicalMeterEntity, physicalMeters);
  }

  public static LogicalMeter toDomainModel(PagedLogicalMeter pagedLogicalMeter) {
    return new LogicalMeter(
      pagedLogicalMeter.id,
      pagedLogicalMeter.externalId,
      pagedLogicalMeter.organisationId,
      LocationEntityMapper.toDomainModel(pagedLogicalMeter.location),
      pagedLogicalMeter.created,
      emptyList(),
      MeterDefinitionEntityMapper.toDomainModel(pagedLogicalMeter.meterDefinition),
      emptyList(),
      null,
      emptyList(),
      Optional.ofNullable(pagedLogicalMeter.currentStatus)
        .map(MeterStatusLogEntityMapper::toDomainModel).orElse(null)
    );
  }

  public static LogicalMeter toDomainModel(
    LogicalMeterEntity logicalMeterEntity,
    Map<UUID, List<PhysicalMeterStatusLogEntity>> meterStatusMap,
    Map<UUID, Long> meterMeasurementCount
  ) {

    List<PhysicalMeter> physicalMeters = logicalMeterEntity.physicalMeters
      .stream()
      .map(physicalMeterEntity -> PhysicalMeterEntityMapper.toDomainModel(
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

    logicalMeterEntity.physicalMeters = logicalMeter.physicalMeters
      .stream()
      .map(PhysicalMeterEntityMapper::toEntity)
      .collect(toSet());

    logicalMeterEntity.gateways = logicalMeter.gateways
      .stream()
      .map(GatewayEntityMapper::toEntity)
      .collect(toSet());

    return logicalMeterEntity;
  }

  private static LogicalMeter toLogicalMeter(
    LogicalMeterEntity logicalMeterEntity,
    List<PhysicalMeter> physicalMeters
  ) {
    return new LogicalMeter(
      logicalMeterEntity.id,
      logicalMeterEntity.externalId,
      logicalMeterEntity.organisationId,
      LocationEntityMapper.toDomainModel(logicalMeterEntity.location),
      logicalMeterEntity.created,
      physicalMeters,
      MeterDefinitionEntityMapper.toDomainModel(logicalMeterEntity.meterDefinition),
      toGateways(logicalMeterEntity.gateways)
    );
  }

  private static List<Gateway> toGateways(Set<GatewayEntity> gateways) {
    return gateways
      .stream()
      .map(GatewayEntityMapper::toDomainModelWithoutStatusLogs)
      .collect(toList());
  }
}
