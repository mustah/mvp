package com.elvaco.mvp.database.repository.access;

import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LocationWithId;
import com.elvaco.mvp.core.spi.repository.Locations;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.elvaco.mvp.database.repository.jpa.LocationJpaRepository;
import com.elvaco.mvp.database.repository.mappers.LocationMapper;

public class LocationRepository implements Locations {

  private final LocationJpaRepository locationJpaRepository;
  private final LocationMapper mapper;

  public LocationRepository(
    LocationJpaRepository locationJpaRepository,
    LocationMapper mapper
  ) {
    this.locationJpaRepository = locationJpaRepository;
    this.mapper = mapper;
  }

  @Override
  public LocationWithId save(LocationWithId location) {
    LocationEntity savedEntity = locationJpaRepository.save(mapper.toEntity(location));
    return mapper.toLocationWithId(savedEntity);
  }

  @Override
  public Optional<LocationWithId> findById(UUID logicalMeterId) {
    return Optional.ofNullable(locationJpaRepository.findOne(logicalMeterId))
      .map(mapper::toLocationWithId);
  }
}
