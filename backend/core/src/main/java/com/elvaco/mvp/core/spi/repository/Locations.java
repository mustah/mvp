package com.elvaco.mvp.core.spi.repository;

import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LocationWithId;

public interface Locations {

  LocationWithId save(LocationWithId location);

  Optional<LocationWithId> findById(UUID logicalMeterId);
}
