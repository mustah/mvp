package com.elvaco.mvp.database.repository.access;

import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LocationWithId;
import com.elvaco.mvp.core.spi.repository.Locations;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.elvaco.mvp.database.repository.jpa.LocationJpaRepository;
import com.elvaco.mvp.database.repository.mappers.LocationMapper;
import lombok.RequiredArgsConstructor;

import static com.elvaco.mvp.database.repository.mappers.LocationMapper.toEntity;
import static com.elvaco.mvp.database.repository.mappers.LocationMapper.toLocationWithId;

@RequiredArgsConstructor
public class LocationRepository implements Locations {

  private final LocationJpaRepository locationJpaRepository;

  @Override
  public LocationWithId save(LocationWithId location) {
    LocationEntity savedEntity = locationJpaRepository.save(toEntity(location));
    return toLocationWithId(savedEntity);
  }

  @Override
  public Optional<LocationWithId> findById(UUID logicalMeterId) {
    return Optional.ofNullable(locationJpaRepository.findOne(logicalMeterId))
      .map(LocationMapper::toLocationWithId);
  }
}
