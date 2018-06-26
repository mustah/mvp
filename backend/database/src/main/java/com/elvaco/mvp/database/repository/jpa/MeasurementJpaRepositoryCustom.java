package com.elvaco.mvp.database.repository.jpa;

import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface MeasurementJpaRepositoryCustom
  extends QueryDslPredicateExecutor<MeasurementEntity> {

}
