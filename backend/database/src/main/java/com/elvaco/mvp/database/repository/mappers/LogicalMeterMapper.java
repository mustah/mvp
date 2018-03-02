package com.elvaco.mvp.database.repository.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterStatusLog;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class LogicalMeterMapper {

  private final MeterDefinitionMapper meterDefinitionMapper;
  private final LocationMapper locationMapper;
  private final PhysicalMeterMapper physicalMeterMapper;
  private final GatewayMapper gatewayMapper;
  private final MeterStatusLogMapper meterStatusLogMapper;

  public LogicalMeterMapper(
    MeterDefinitionMapper meterDefinitionMapper,
    LocationMapper locationMapper,
    PhysicalMeterMapper physicalMeterMapper,
    GatewayMapper gatewayMapper,
    MeterStatusLogMapper meterStatusLogMapper
  ) {
    this.meterDefinitionMapper = meterDefinitionMapper;
    this.locationMapper = locationMapper;
    this.physicalMeterMapper = physicalMeterMapper;
    this.gatewayMapper = gatewayMapper;
    this.meterStatusLogMapper = meterStatusLogMapper;
  }

  public LogicalMeter toDomainModel(LogicalMeterEntity logicalMeterEntity) {
    List<MeterStatusLog> meterStatusLogs = new ArrayList<>();

    List<PhysicalMeter> physicalMeters = logicalMeterEntity.physicalMeters
      .stream()
      .map(physicalMeterMapper::toDomainModel)
      .collect(toList());

    return getLogicalMeter(logicalMeterEntity, emptyList(), physicalMeters);
  }

  public LogicalMeter toDomainModel(
    LogicalMeterEntity logicalMeterEntity,
    Map<Long, List<PhysicalMeterStatusLogEntity>> map
  ) {

    List<MeterStatusLog> meterStatusLogs = new ArrayList<>();

    List<PhysicalMeter> physicalMeters = logicalMeterEntity.physicalMeters
      .stream()
      .map(physicalMeterMapper::toDomainModel)
      .peek(
        physicalMeter -> meterStatusLogs.addAll(
          map.get(physicalMeter.id).stream().map(meterStatusLogMapper::toDomainModel)
            .collect(toList())
        )
      )
      .collect(toList());

    return getLogicalMeter(logicalMeterEntity, meterStatusLogs, physicalMeters);
  }

  private LogicalMeter getLogicalMeter(
    LogicalMeterEntity logicalMeterEntity,
    List<MeterStatusLog> meterStatusLogs,
    List<PhysicalMeter> physicalMeters
  ) {

    return new LogicalMeter(
      logicalMeterEntity.id,
      logicalMeterEntity.externalId,
      logicalMeterEntity.organisationId,
      locationMapper.toDomainModel(logicalMeterEntity.getLocation()),
      logicalMeterEntity.created,
      physicalMeters,
      meterDefinitionMapper.toDomainModel(logicalMeterEntity.meterDefinition),
      meterStatusLogs,
      toGateways(logicalMeterEntity.gateways)
    );
  }

  public LogicalMeterEntity toEntity(LogicalMeter logicalMeter) {
    LogicalMeterEntity logicalMeterEntity = new LogicalMeterEntity(
      logicalMeter.id,
      logicalMeter.externalId,
      logicalMeter.organisationId,
      logicalMeter.created,
      meterDefinitionMapper.toEntity(logicalMeter.meterDefinition)
    );

    logicalMeterEntity.physicalMeters = logicalMeter.physicalMeters
      .stream()
      .map(physicalMeterMapper::toEntity)
      .collect(toSet());

    logicalMeterEntity.gateways = logicalMeter.gateways
      .stream()
      .map(gatewayMapper::toEntity)
      .collect(toList());

    if (logicalMeter.location != null) {
      LocationEntity location = locationMapper.toEntity(logicalMeter.location);
      location.logicalMeterId = logicalMeterEntity.id;
      logicalMeterEntity.setLocation(location);
    }

    return logicalMeterEntity;
  }

  private List<Gateway> toGateways(List<GatewayEntity> gateways) {
    return gateways
      .stream()
      .map(gatewayMapper::toDomainModel)
      .collect(toList());
  }
}
