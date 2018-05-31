package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LocationWithId;
import com.elvaco.mvp.core.domainmodels.MapMarker;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.Locations;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.elvaco.mvp.database.repository.jpa.LocationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MapMarkerJpaRepository;
import com.elvaco.mvp.database.repository.mappers.LocationEntityMapper;
import lombok.RequiredArgsConstructor;

import static com.elvaco.mvp.database.repository.mappers.LocationEntityMapper.toEntity;
import static com.elvaco.mvp.database.repository.mappers.LocationEntityMapper.toLocationWithId;

@RequiredArgsConstructor
public class LocationRepository implements Locations {

  private final LocationJpaRepository locationJpaRepository;
  private final MapMarkerJpaRepository meterMapQueryDslJpaRepository;
  private final MapMarkerJpaRepository gatewayMapQueryDslJpaRepository;

  @Override
  public LocationWithId save(LocationWithId location) {
    LocationEntity savedEntity = locationJpaRepository.save(toEntity(location));
    return toLocationWithId(savedEntity);
  }

  @Override
  public Optional<LocationWithId> findById(UUID logicalMeterId) {
    return Optional.ofNullable(locationJpaRepository.findOne(logicalMeterId))
      .map(LocationEntityMapper::toLocationWithId);
  }

  @Override
  public List<MapMarker> findAllMeterMapMarkers(RequestParameters parameters) {
    return meterMapQueryDslJpaRepository.findAllMapMarkers(parameters);
  }

  @Override
  public List<MapMarker> findAllGatewayMapMarkers(RequestParameters parameters) {
    return gatewayMapQueryDslJpaRepository.findAllMapMarkers(parameters);
  }
}
