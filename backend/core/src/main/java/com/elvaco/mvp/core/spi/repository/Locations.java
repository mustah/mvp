package com.elvaco.mvp.core.spi.repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Address;
import com.elvaco.mvp.core.domainmodels.City;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationWithId;
import com.elvaco.mvp.core.domainmodels.MapMarker;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;

public interface Locations {

  LocationWithId save(LocationWithId location);

  Optional<LocationWithId> findById(UUID logicalMeterId);

  Set<MapMarker> findAllMeterMapMarkers(RequestParameters parameters);

  Set<MapMarker> findAllGatewayMapMarkers(RequestParameters parameters);

  Page<Location> findAll(RequestParameters parameters, Pageable pageable);

  Page<City> findAllCities(RequestParameters parameters, Pageable pageable);

  Page<Address> findAllAddresses(RequestParameters parameters, Pageable pageable);
}
