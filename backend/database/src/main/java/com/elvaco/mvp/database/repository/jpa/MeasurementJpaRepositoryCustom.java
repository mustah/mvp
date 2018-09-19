package com.elvaco.mvp.database.repository.jpa;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface MeasurementJpaRepositoryCustom
  extends QueryDslPredicateExecutor<MeasurementEntity> {

  Optional<MeasurementEntity> findBy(
    UUID physicalMeterId,
    String quantity,
    ZonedDateTime created
  );

  @Override
  Page<MeasurementEntity> findAll(Predicate predicate, Pageable pageable);
}
