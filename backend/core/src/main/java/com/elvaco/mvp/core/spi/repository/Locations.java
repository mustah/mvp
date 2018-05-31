package com.elvaco.mvp.core.spi.repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LocationWithId;
import com.elvaco.mvp.core.domainmodels.MapMarker;
import com.elvaco.mvp.core.spi.data.RequestParameters;

public interface Locations {

  LocationWithId save(LocationWithId location);

  Optional<LocationWithId> findById(UUID logicalMeterId);

  Set<MapMarker> findAllMeterMapMarkers(RequestParameters parameters);

  Set<MapMarker> findAllGatewayMapMarkers(RequestParameters parameters);
}
