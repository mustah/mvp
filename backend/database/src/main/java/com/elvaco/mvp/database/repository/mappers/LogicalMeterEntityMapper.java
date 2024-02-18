package com.elvaco.mvp.database.repository.mappers;

import java.util.List;

import com.elvaco.mvp.core.access.MediumProvider;
import com.elvaco.mvp.core.access.SystemMeterDefinitionProvider;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.spi.repository.AlarmDescriptions;
import com.elvaco.mvp.database.entity.meter.EntityPk;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;

import lombok.RequiredArgsConstructor;

import static com.elvaco.mvp.database.repository.mappers.PhysicalMeterEntityMapper.toDomainModels;
import static com.elvaco.mvp.database.repository.mappers.PhysicalMeterEntityMapper.toDomainModelsWithoutStatusLogs;
import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
public final class LogicalMeterEntityMapper {

  private final MeterDefinitionEntityMapper meterDefinitionEntityMapper;
  private final SystemMeterDefinitionProvider meterDefinitionProvider;
  private final MediumProvider mediumProvider;
  private final AlarmDescriptions alarmDescriptions;

  public LogicalMeter toDomainModelWithoutStatuses(LogicalMeterEntity entity) {
    return newLogicalMeter(
      entity,
      toDomainModelsWithoutStatusLogs(entity.physicalMeters)
    );
  }

  public LogicalMeter toDomainModel(LogicalMeterEntity entity) {
    return newLogicalMeter(
      entity,
      toDomainModels(entity.physicalMeters)
    );
  }

  public LogicalMeter toSimpleDomainModel(LogicalMeterEntity entity) {
    return LogicalMeter.builder()
      .id(entity.getLogicalMeterId())
      .organisationId(entity.getOrganisationId())
      .externalId(entity.externalId)
      .meterDefinition(meterDefinitionEntityMapper.toDomainModel(entity.meterDefinition))
      .created(entity.created)
      .physicalMeters(toDomainModelsWithoutStatusLogs(entity.physicalMeters))
      .location(LocationEntityMapper.toDomainModel(entity.location))
      .utcOffset(entity.utcOffset)
      .build();
  }

  public LogicalMeterEntity toEntity(LogicalMeter logicalMeter) {
    var pk = new EntityPk(logicalMeter.id, logicalMeter.organisationId);

    if (logicalMeter.getMeterDefinition().getId() == null) {
      logicalMeter = logicalMeter.toBuilder().meterDefinition(
        meterDefinitionProvider.getByMediumOrThrow(
          mediumProvider.getByNameOrThrow(
            logicalMeter.getMedium().name
          )
        )
      ).build();
    }

    LogicalMeterEntity logicalMeterEntity = new LogicalMeterEntity(
      pk,
      logicalMeter.externalId,
      logicalMeter.created,
      meterDefinitionEntityMapper.toEntity(logicalMeter.meterDefinition),
      logicalMeter.utcOffset
    );

    logicalMeterEntity.location = LocationEntityMapper.toEntity(pk, logicalMeter.location);

    logicalMeterEntity.gatewayMeters = logicalMeter.gateways.stream()
      .map(gw -> GatewayMeterEntityMapper.toEntity(gw, logicalMeterEntity))
      .collect(toSet());

    return logicalMeterEntity;
  }

  private LogicalMeter newLogicalMeter(
    LogicalMeterEntity entity,
    List<PhysicalMeter> physicalMeters
  ) {
    return LogicalMeter.builder()
      .id(entity.getLogicalMeterId())
      .organisationId(entity.getOrganisationId())
      .externalId(entity.externalId)
      .meterDefinition(meterDefinitionEntityMapper.toDomainModel(entity.meterDefinition))
      .created(entity.created)
      .physicalMeters(physicalMeters)
      .gateways(entity.gatewayMeters.stream()
        .map(GatewayMeterEntityMapper::toDomainModel)
        .toList())
      .location(LocationEntityMapper.toDomainModel(entity.location))
      .alarms(MeterAlarmLogEntityMapper.toLatestActiveAlarms(alarmDescriptions, physicalMeters))
      .utcOffset(entity.utcOffset)
      .build();
  }
}
