package com.elvaco.mvp.database.repository.jpa;

import java.util.UUID;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LocationJpaRepository {

  Page<LocationEntity> findAll(
    RequestParameters parameters,
    Predicate predicate,
    Pageable pageable
  );

  <S extends LocationEntity> S save(S entity);

  LocationEntity findByLogicalMeterId(UUID logicalMeterId);
}
