package com.elvaco.mvp.database.repository.mappers;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.access.MediumProvider;
import com.elvaco.mvp.core.access.SystemMeterDefinitionProvider;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.database.entity.meter.EntityPk;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterWithLocation;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;

import lombok.RequiredArgsConstructor;

import static com.elvaco.mvp.database.repository.mappers.PhysicalMeterEntityMapper.toDomainModels;
import static com.elvaco.mvp.database.repository.mappers.PhysicalMeterEntityMapper.toDomainModelsWithoutStatusLogs;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
public final class LogicalMeterEntityMapper {

  private final MeterDefinitionEntityMapper meterDefinitionEntityMapper;
  private final SystemMeterDefinitionProvider meterDefinitionProvider;
  private final MediumProvider mediumProvider;

  public LogicalMeter toDomainModelWithLocation(LogicalMeterWithLocation logicalMeter) {
    return LogicalMeter.builder()
      .id(logicalMeter.id)
      .externalId(logicalMeter.externalId)
      .organisationId(logicalMeter.organisationId)
      .meterDefinition(
        meterDefinitionProvider.getByMediumOrThrow(
          mediumProvider.getByNameOrThrow(logicalMeter.medium)
        )
      )
      .location(LocationEntityMapper.toDomainModel(logicalMeter.location))
      .utcOffset(logicalMeter.utcOffset)
      .build();
  }

  public LogicalMeter toDomainModelWithoutStatuses(LogicalMeterEntity entity) {
    return newLogicalMeter(
      entity,
      toDomainModelsWithoutStatusLogs(entity.physicalMeters),
      null
    );
  }

  public LogicalMeter toDomainModel(LogicalMeterEntity entity) {
    return newLogicalMeter(
      entity,
      toDomainModels(entity.physicalMeters),
      null
    );
  }

  public LogicalMeter toDomainModel(
    LogicalMeterEntity logicalMeterEntity,
    Map<UUID, List<PhysicalMeterStatusLogEntity>> meterStatusMap,
    @Nullable Double collectionPercentage
  ) {
    List<PhysicalMeter> physicalMeters = logicalMeterEntity.physicalMeters.stream()
      .map(physicalMeterEntity ->
        PhysicalMeterEntityMapper.toDomainModel(
          physicalMeterEntity,
          meterStatusMap.getOrDefault(physicalMeterEntity.getId(), emptyList())
        ))
      .collect(toList());

    return newLogicalMeter(
      logicalMeterEntity,
      physicalMeters,
      collectionPercentage
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

    logicalMeterEntity.gateways = logicalMeter.gateways.stream()
      .map(GatewayEntityMapper::toEntity)
      .collect(toSet());

    return logicalMeterEntity;
  }

  private LogicalMeter newLogicalMeter(
    LogicalMeterEntity entity,
    List<PhysicalMeter> physicalMeters,
    @Nullable Double collectionPercentage
  ) {
    return LogicalMeter.builder()
      .id(entity.getLogicalMeterId())
      .organisationId(entity.getOrganisationId())
      .externalId(entity.externalId)
      .meterDefinition(meterDefinitionEntityMapper.toDomainModel(entity.meterDefinition))
      .created(entity.created)
      .physicalMeters(physicalMeters)
      .gateways(entity.gateways.stream()
        .map(GatewayEntityMapper::toDomainModel)
        .collect(toList()))
      .location(LocationEntityMapper.toDomainModel(entity.location))
      .collectionPercentage(collectionPercentage)
      .alarms(MeterAlarmLogEntityMapper.toLatestActiveAlarms(physicalMeters))
      .utcOffset(entity.utcOffset)
      .build();
  }
}
