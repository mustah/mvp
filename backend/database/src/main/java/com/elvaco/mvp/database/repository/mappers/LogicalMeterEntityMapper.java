package com.elvaco.mvp.database.repository.mappers;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.LogicalMeterCollectionStats;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Pk;
import com.elvaco.mvp.core.domainmodels.SelectionPeriod;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterWithLocation;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import lombok.experimental.UtilityClass;

import static com.elvaco.mvp.core.util.LogicalMeterHelper.calculateExpectedReadOuts;
import static com.elvaco.mvp.database.repository.mappers.PhysicalMeterEntityMapper.toDomainModels;
import static com.elvaco.mvp.database.repository.mappers.PhysicalMeterEntityMapper.toDomainModelsWithoutStatusLogs;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@UtilityClass
public class LogicalMeterEntityMapper {

  public static LogicalMeter toDomainModelWithoutStatuses(LogicalMeterEntity entity) {
    return newLogicalMeter(
      entity,
      toDomainModelsWithoutStatusLogs(entity.physicalMeters),
      null,
      null
    );
  }

  public static LogicalMeter toDomainModel(LogicalMeterEntity entity) {
    return newLogicalMeter(
      entity,
      toDomainModels(entity.physicalMeters),
      null,
      null
    );
  }

  public static LogicalMeter toDomainModel(
    LogicalMeterEntity logicalMeterEntity,
    Map<UUID, List<PhysicalMeterStatusLogEntity>> mappedStatuses
  ) {
    return toDomainModel(logicalMeterEntity, mappedStatuses, null, null);
  }

  public static LogicalMeter toDomainModel(
    LogicalMeterEntity logicalMeterEntity,
    Map<UUID, List<PhysicalMeterStatusLogEntity>> meterStatusMap,
    @Nullable Long expectedMeasurementCount,
    @Nullable Long missingMeasurementCount
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
      expectedMeasurementCount,
      missingMeasurementCount
    );
  }

  public static LogicalMeterCollectionStats toDomainModel(
    LogicalMeterCollectionStats logicalMeterCollectionStats,
    SelectionPeriod selectionPeriod
  ) {
    return new LogicalMeterCollectionStats(
      logicalMeterCollectionStats.id,
      logicalMeterCollectionStats.missingReadingCount,
      calculateExpectedReadOuts(logicalMeterCollectionStats.readInterval, selectionPeriod)
    );
  }

  public static LogicalMeter toDomainModelWithLocation(LogicalMeterWithLocation logicalMeter) {
    return LogicalMeter.builder()
      .id(logicalMeter.id)
      .externalId(logicalMeter.externalId)
      .organisationId(logicalMeter.organisationId)
      .meterDefinition(MeterDefinition.fromMedium(Medium.from(logicalMeter.medium)))
      .location(LocationEntityMapper.toDomainModel(logicalMeter.location))
      .utcOffset(logicalMeter.utcOffset)
      .build();
  }

  public static LogicalMeter toSimpleDomainModel(LogicalMeterEntity entity) {
    return LogicalMeter.builder()
      .id(entity.getLogicalMeterId())
      .organisationId(entity.getOrganisationId())
      .externalId(entity.externalId)
      .meterDefinition(MeterDefinitionEntityMapper.toDomainModel(entity.meterDefinition))
      .created(entity.created)
      .physicalMeters(toDomainModelsWithoutStatusLogs(entity.physicalMeters))
      .location(LocationEntityMapper.toDomainModel(entity.location))
      .utcOffset(entity.utcOffset)
      .build();
  }

  public static LogicalMeterEntity toEntity(LogicalMeter logicalMeter) {
    LogicalMeterEntity logicalMeterEntity = new LogicalMeterEntity(
      logicalMeter.id,
      logicalMeter.externalId,
      logicalMeter.organisationId,
      logicalMeter.created,
      MeterDefinitionEntityMapper.toEntity(logicalMeter.meterDefinition),
      logicalMeter.utcOffset
    );

    var pk = new Pk(logicalMeter.id, logicalMeter.organisationId);
    logicalMeterEntity.location = LocationEntityMapper.toEntity(pk, logicalMeter.location);

    logicalMeterEntity.physicalMeters = logicalMeter.physicalMeters.stream()
      .map(PhysicalMeterEntityMapper::toEntity)
      .collect(toSet());

    logicalMeterEntity.gateways = logicalMeter.gateways.stream()
      .map(GatewayEntityMapper::toEntity)
      .collect(toSet());

    return logicalMeterEntity;
  }

  private static LogicalMeter newLogicalMeter(
    LogicalMeterEntity entity,
    List<PhysicalMeter> physicalMeters,
    @Nullable Long expectedMeasurementCount,
    @Nullable Long missingMeasurementCount
  ) {
    return LogicalMeter.builder()
      .id(entity.getLogicalMeterId())
      .organisationId(entity.getOrganisationId())
      .externalId(entity.externalId)
      .meterDefinition(MeterDefinitionEntityMapper.toDomainModel(entity.meterDefinition))
      .created(entity.created)
      .physicalMeters(physicalMeters)
      .gateways(toGatewaysWithoutStatusLogs(entity.gateways))
      .location(LocationEntityMapper.toDomainModel(entity.location))
      .expectedMeasurementCount(expectedMeasurementCount)
      .missingMeasurementCount(missingMeasurementCount)
      .alarm(MeterAlarmLogEntityMapper.toLatestActiveAlarm(physicalMeters))
      .utcOffset(entity.utcOffset)
      .build();
  }

  private static List<Gateway> toGatewaysWithoutStatusLogs(Set<GatewayEntity> gateways) {
    return gateways.stream()
      .map(GatewayEntityMapper::toDomainModelWithoutStatusLogs)
      .collect(toList());
  }
}
