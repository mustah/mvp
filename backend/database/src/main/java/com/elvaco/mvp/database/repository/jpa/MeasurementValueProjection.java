package com.elvaco.mvp.database.repository.jpa;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import com.elvaco.mvp.database.entity.measurement.MeasurementUnit;

public interface MeasurementValueProjection {

  Timestamp getWhen();

  String getValue();

  default Optional<MeasurementUnit> getMeasurementUnit() {
    if (this.getValue() == null) {
      return Optional.empty();
    }
    return Optional.of(new MeasurementUnit(this.getValue()));
  }

  default Instant getInstant() {
    return getWhen().toInstant();
  }

  default Double getValueValue() {
    return getMeasurementUnit().map(MeasurementUnit::getValue).orElse(null);
  }

  default String getUnit() {
    return getMeasurementUnit().map(MeasurementUnit::getUnit).orElse(null);
  }
}
