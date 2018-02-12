package com.elvaco.mvp.database.repository.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.MeterStatusLog;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.UserProperty;
import com.elvaco.mvp.database.dto.propertycollection.UserPropertyDto;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PropertyCollection;

public class LogicalMeterMapper {

  private final MeterDefinitionMapper meterDefinitionMapper;
  private final LocationMapper locationMapper;
  private final PhysicalMeterMapper physicalMeterMapper;

  public LogicalMeterMapper(
    MeterDefinitionMapper meterDefinitionMapper,
    LocationMapper locationMapper,
    PhysicalMeterMapper physicalMeterMapper
  ) {
    this.meterDefinitionMapper = meterDefinitionMapper;
    this.locationMapper = locationMapper;
    this.physicalMeterMapper = physicalMeterMapper;
  }

  public LogicalMeter toDomainModel(LogicalMeterEntity logicalMeterEntity) {
    PropertyCollection props = logicalMeterEntity.propertyCollection;

    UserProperty userProperty = props
      .asObject("user", UserPropertyDto.class)
      .map(this::toUserProperty)
      .orElse(new UserProperty());

    Location location = locationMapper.toDomainModel(logicalMeterEntity.getLocation());

    MeterDefinition meterDefinition = null;
    if (logicalMeterEntity.meterDefinition != null) {
      meterDefinition = meterDefinitionMapper.toDomainModel(logicalMeterEntity.meterDefinition);
    }

    List<PhysicalMeter> physicalMeters = logicalMeterEntity.physicalMeters.stream()
      .map(physicalMeterMapper::toDomainModel)
      .collect(Collectors.toList());

    List<MeterStatusLog> meterStatusLogs = new ArrayList<>();

    physicalMeters.forEach(physicalMeter -> meterStatusLogs.addAll(physicalMeter.meterStatusLogs));

    return new LogicalMeter(
      logicalMeterEntity.id,
      logicalMeterEntity.status,
      location,
      logicalMeterEntity.created,
      new com.elvaco.mvp.core.domainmodels.PropertyCollection(userProperty),
      physicalMeters,
      meterDefinition,
      meterStatusLogs
    );
  }

  public LogicalMeterEntity toEntity(LogicalMeter logicalMeter) {
    LogicalMeterEntity logicalMeterEntity = new LogicalMeterEntity(
      logicalMeter.id,
      logicalMeter.created,
      logicalMeter.status
    );

    Optional.ofNullable(logicalMeter.propertyCollection.userProperty)
      .map(this::toUserPropertyDto)
      .map(userPropertyDto ->
             logicalMeterEntity.propertyCollection.put("user", userPropertyDto));

    if (logicalMeter.hasMeterDefinition()) {
      logicalMeterEntity.meterDefinition =
        meterDefinitionMapper.toEntity(logicalMeter.meterDefinition);
    }
    logicalMeterEntity.physicalMeters = logicalMeter.physicalMeters.stream()
      .map(physicalMeterMapper::toEntity)
      .collect(Collectors.toSet());

    if (logicalMeter.location != null) {
      LocationEntity locationEntity = locationMapper.toEntity(logicalMeter.location);
      logicalMeterEntity.setLocation(locationEntity);
    }

    return logicalMeterEntity;
  }

  private UserPropertyDto toUserPropertyDto(UserProperty userProperty) {
    return new UserPropertyDto(userProperty.externalId, userProperty.project);
  }

  private UserProperty toUserProperty(UserPropertyDto userPropertyDto) {
    return new UserProperty(userPropertyDto.externalId, userPropertyDto.project);
  }
}
