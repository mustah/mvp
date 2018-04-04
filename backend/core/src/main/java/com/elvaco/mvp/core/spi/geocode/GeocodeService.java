package com.elvaco.mvp.core.spi.geocode;

import com.elvaco.mvp.core.domainmodels.LocationWithId;

public interface GeocodeService {

  void fetchCoordinates(LocationWithId location);
}
