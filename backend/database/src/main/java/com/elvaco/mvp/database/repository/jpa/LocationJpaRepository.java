package com.elvaco.mvp.database.repository.jpa;

import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Address;
import com.elvaco.mvp.core.domainmodels.City;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LocationJpaRepository {

  Page<City> findAllCities(RequestParameters parameters, Pageable pageable);

  Page<Address> findAllAddresses(RequestParameters parameters, Pageable pageable);

  Optional<LocationEntity> findById(UUID logicalMeterId);

  <S extends LocationEntity> S save(S entity);
}
