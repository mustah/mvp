package com.elvaco.mvp.core.usecase;

import java.util.Set;

import com.elvaco.mvp.core.domainmodels.MapMarker;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.Locations;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MapUseCases {

  private final AuthenticatedUser currentUser;
  private final Locations locations;

  public Set<MapMarker> findAllMeterMapMarkers(RequestParameters parameters) {
    return locations.findAllMeterMapMarkers(parameters.ensureOrganisationFilters(currentUser));
  }

  public Set<MapMarker> findAllGatewayMapMarkers(RequestParameters parameters) {
    return locations.findAllGatewayMapMarkers(parameters.ensureOrganisationFilters(currentUser));
  }
}
