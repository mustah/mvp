package com.elvaco.mvp.database.repository.jpa;

import java.util.List;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface MeasurementJpaRepositoryCustom
  extends QuerydslPredicateExecutor<MeasurementEntity> {

  List<MeasurementEntity> findAll(RequestParameters parameters);
}
