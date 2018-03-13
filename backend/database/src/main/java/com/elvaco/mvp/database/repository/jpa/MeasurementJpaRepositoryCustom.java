package com.elvaco.mvp.database.repository.jpa;

import java.util.List;

import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface MeasurementJpaRepositoryCustom extends
  QueryDslPredicateExecutor<MeasurementEntity> {

  @Override
  List<MeasurementEntity> findAll(Predicate predicate);

  List<MeasurementEntity> findAllScaled(String scale, Predicate predicate);

  Page<MeasurementEntity> findAllScaled(
    String scale,
    Predicate predicate,
    Pageable pageable
  );
}
