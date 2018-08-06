package com.elvaco.mvp.database.repository.jpa;

import com.elvaco.mvp.database.entity.measurement.MissingMeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.MissingMeasurementPk;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissingMeasurementJpaRepository
  extends JpaRepository<MissingMeasurementEntity, MissingMeasurementPk> {
}
