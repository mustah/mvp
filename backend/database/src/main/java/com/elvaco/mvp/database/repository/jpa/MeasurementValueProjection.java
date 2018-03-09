package com.elvaco.mvp.database.repository.jpa;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import com.elvaco.mvp.database.entity.measurement.MeasurementUnit;

public interface MeasurementValueProjection {

  Timestamp getWhen();

  String getUnitValue();

  default Optional<MeasurementUnit> getMeasurementUnit() {
    if (getUnitValue() == null) {
      return Optional.empty();
    }
    return Optional.of(new MeasurementUnit(getUnitValue()));
  }

  default Instant getInstant() {
    return getWhen().toInstant();
  }

  default Double getValue() {
    return getMeasurementUnit().map(MeasurementUnit::getValue).orElse(null);
  }

  default String getUnit() {
    return getMeasurementUnit().map(MeasurementUnit::getUnit).orElse(null);
  }
}
