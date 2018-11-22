package com.elvaco.mvp.database.repository.access;

import java.util.Set;

import com.elvaco.mvp.adapters.spring.PageAdapter;
import com.elvaco.mvp.core.domainmodels.Address;
import com.elvaco.mvp.core.domainmodels.City;
import com.elvaco.mvp.core.domainmodels.LocationWithId;
import com.elvaco.mvp.core.domainmodels.MapMarker;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.Locations;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.elvaco.mvp.database.repository.jpa.LocationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MapMarkerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;

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
  public Set<MapMarker> findAllMeterMapMarkers(RequestParameters parameters) {
    return meterMapQueryDslJpaRepository.findAllMapMarkers(parameters);
  }

  @Override
  public Set<MapMarker> findAllGatewayMapMarkers(RequestParameters parameters) {
    return gatewayMapQueryDslJpaRepository.findAllMapMarkers(parameters);
  }

  @Override
  public Page<City> findAllCities(RequestParameters parameters, Pageable pageable) {
    return new PageAdapter<>(locationJpaRepository.findAllCities(
      parameters,
      PageRequest.of(pageable.getPageNumber(), pageable.getPageSize())
    ));
  }

  @Override
  public Page<Address> findAllAddresses(RequestParameters parameters, Pageable pageable) {
    return new PageAdapter<>(locationJpaRepository.findAllAddresses(
      parameters,
      PageRequest.of(pageable.getPageNumber(), pageable.getPageSize())
    ));
  }
}
