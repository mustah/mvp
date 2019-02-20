package com.elvaco.mvp.database.repository.jpa;

import java.util.List;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;

public interface MeasurementJpaRepositoryCustom {

  List<MeasurementEntity> findAll(RequestParameters parameters);
}
