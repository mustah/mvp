package com.elvaco.mvp.core.spi.repository;

import java.util.Set;

import com.elvaco.mvp.core.domainmodels.Address;
import com.elvaco.mvp.core.domainmodels.City;
import com.elvaco.mvp.core.domainmodels.LocationWithId;
import com.elvaco.mvp.core.domainmodels.MapMarker;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;

public interface Locations {

  LocationWithId save(LocationWithId location);

  Set<MapMarker> findAllMeterMapMarkers(RequestParameters parameters);

  Set<MapMarker> findAllGatewayMapMarkers(RequestParameters parameters);

  Page<City> findAllCities(RequestParameters parameters, Pageable pageable);

  Page<Address> findAllAddresses(RequestParameters parameters, Pageable pageable);
}
