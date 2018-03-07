package com.elvaco.mvp.database.repository.jpa;

import java.util.List;

import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MeasurementJpaRepositoryCustom {

  List<MeasurementEntity> findAllScaled(String scale, Predicate predicate);

  Page<MeasurementEntity> findAllScaled(
    String scale,
    Predicate predicate,
    Pageable pageable
  );
}
