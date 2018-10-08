package com.elvaco.mvp.database.repository.jpa;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface MeasurementJpaRepositoryCustom
  extends QuerydslPredicateExecutor<MeasurementEntity> {

  Optional<MeasurementEntity> findBy(
    UUID physicalMeterId,
    String quantity,
    ZonedDateTime created
  );
}
