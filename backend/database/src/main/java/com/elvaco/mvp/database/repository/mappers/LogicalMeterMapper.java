package com.elvaco.mvp.database.repository.mappers;

import java.util.ArrayList;
import java.util.List;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterStatusLog;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class LogicalMeterMapper {

  private final MeterDefinitionMapper meterDefinitionMapper;
  private final LocationMapper locationMapper;
  private final PhysicalMeterMapper physicalMeterMapper;
  private final GatewayMapper gatewayMapper;

  public LogicalMeterMapper(
    MeterDefinitionMapper meterDefinitionMapper,
    LocationMapper locationMapper,
    PhysicalMeterMapper physicalMeterMapper,
    GatewayMapper gatewayMapper
  ) {
    this.meterDefinitionMapper = meterDefinitionMapper;
    this.locationMapper = locationMapper;
    this.physicalMeterMapper = physicalMeterMapper;
    this.gatewayMapper = gatewayMapper;
  }

  public LogicalMeter toDomainModel(LogicalMeterEntity logicalMeterEntity) {
    List<MeterStatusLog> meterStatusLogs = new ArrayList<>();

    List<PhysicalMeter> physicalMeters = logicalMeterEntity.physicalMeters
      .stream()
      .map(physicalMeterMapper::toDomainModel)
      .peek(physicalMeter -> meterStatusLogs.addAll(physicalMeter.meterStatusLogs))
      .collect(toList());

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
      LocationEntity locationEntity = locationMapper.toEntity(logicalMeter.location);
      logicalMeterEntity.setLocation(locationEntity);
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
