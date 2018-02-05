package com.elvaco.mvp.repository.access;

import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.MeteringPoint;
import com.elvaco.mvp.core.domainmodels.UserProperty;
import com.elvaco.mvp.dto.propertycollection.UserPropertyDto;
import com.elvaco.mvp.entity.meteringpoint.LocationEntity;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.elvaco.mvp.entity.meteringpoint.PropertyCollection;

public class MeteringPointMapper {

  private final LocationMapper locationMapper;

  public MeteringPointMapper(LocationMapper locationMapper) {
    this.locationMapper = locationMapper;
  }

  MeteringPoint toDomainModel(MeteringPointEntity meteringPointEntity) {
    PropertyCollection props = meteringPointEntity.propertyCollection;

    UserProperty userProperty = props
      .asObject("user", UserPropertyDto.class)
      .map(this::toUserProperty)
      .orElse(null);

    Location location = locationMapper.toDomainModel(meteringPointEntity.getLocation());

    return new MeteringPoint(
      meteringPointEntity.id,
      meteringPointEntity.status,
      location,
      meteringPointEntity.created,
      new com.elvaco.mvp.core.domainmodels.PropertyCollection(userProperty)
    );
  }

  public MeteringPointEntity toEntity(MeteringPoint meteringPoint) {
    MeteringPointEntity meteringPointEntity = new MeteringPointEntity(
      meteringPoint.id,
      meteringPoint.created,
      meteringPoint.status
    );

    Optional.ofNullable(meteringPoint.propertyCollection.userProperty)
      .map(this::toUserPropertyDto)
      .map(userPropertyDto ->
             meteringPointEntity.propertyCollection.put("user", userPropertyDto));

    LocationEntity locationEntity = locationMapper.toEntity(meteringPoint.location);
    meteringPointEntity.setLocation(locationEntity);

    return meteringPointEntity;
  }

  private UserPropertyDto toUserPropertyDto(UserProperty userProperty) {
    return new UserPropertyDto(userProperty.externalId, userProperty.project);
  }

  private UserProperty toUserProperty(UserPropertyDto userPropertyDto) {
    return new UserProperty(userPropertyDto.externalId, userPropertyDto.project);
  }
}
