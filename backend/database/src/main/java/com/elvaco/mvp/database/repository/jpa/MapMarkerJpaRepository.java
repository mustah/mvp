package com.elvaco.mvp.database.repository.jpa;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.MapMarker;
import com.elvaco.mvp.core.spi.data.RequestParameters;

public interface MapMarkerJpaRepository {

  List<MapMarker> findAllMapMarkers(RequestParameters parameters);
}
