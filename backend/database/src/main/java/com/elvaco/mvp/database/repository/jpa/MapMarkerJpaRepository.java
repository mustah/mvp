package com.elvaco.mvp.database.repository.jpa;

import java.util.Set;

import com.elvaco.mvp.core.domainmodels.MapMarker;
import com.elvaco.mvp.core.spi.data.RequestParameters;

public interface MapMarkerJpaRepository {

  Set<MapMarker> findAllMapMarkers(RequestParameters parameters);
}
