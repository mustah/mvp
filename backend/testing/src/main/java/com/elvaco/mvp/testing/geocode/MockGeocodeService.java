package com.elvaco.mvp.testing.geocode;

import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.LocationWithId;
import com.elvaco.mvp.core.spi.geocode.GeocodeService;

public class MockGeocodeService implements GeocodeService {

  @Nullable
  public UUID requestId;

  @Nullable
  public LocationWithId location;

  @Override
  public void fetchCoordinates(LocationWithId location) {
    this.requestId = location.getId();
    this.location = location;
  }
}
