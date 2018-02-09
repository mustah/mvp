package com.elvaco.mvp.database.repository.mappers;

import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.UserProperty;
import com.elvaco.mvp.database.dto.propertycollection.UserPropertyDto;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PropertyCollection;

public class LogicalMeterMapper {

  private final LocationMapper locationMapper;

  public LogicalMeterMapper(LocationMapper locationMapper) {
    this.locationMapper = locationMapper;
  }

  public LogicalMeter toDomainModel(LogicalMeterEntity logicalMeterEntity) {
    PropertyCollection props = logicalMeterEntity.propertyCollection;

    UserProperty userProperty = props
      .asObject("user", UserPropertyDto.class)
      .map(this::toUserProperty)
      .orElse(null);

    Location location = locationMapper.toDomainModel(logicalMeterEntity.getLocation());

    return new LogicalMeter(
      logicalMeterEntity.id,
      logicalMeterEntity.status,
      location,
      logicalMeterEntity.created,
      new com.elvaco.mvp.core.domainmodels.PropertyCollection(userProperty)
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

    LocationEntity locationEntity = locationMapper.toEntity(logicalMeter.location);
    logicalMeterEntity.setLocation(locationEntity);

    return logicalMeterEntity;
  }

  private UserPropertyDto toUserPropertyDto(UserProperty userProperty) {
    return new UserPropertyDto(userProperty.externalId, userProperty.project);
  }

  private UserProperty toUserProperty(UserPropertyDto userPropertyDto) {
    return new UserProperty(userPropertyDto.externalId, userPropertyDto.project);
  }
}
