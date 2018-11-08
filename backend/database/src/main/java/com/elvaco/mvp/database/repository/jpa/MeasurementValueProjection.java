package com.elvaco.mvp.database.repository.jpa;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.MeasurementUnit;

public interface MeasurementValueProjection {

  @Nullable
  Timestamp getWhen();

  @Nullable
  String getValue();

  default Optional<MeasurementUnit> getMeasurementUnit() {
    if (this.getValue() == null) {
      return Optional.empty();
    }
    return Optional.of(MeasurementUnit.from(this.getValue()));
  }

  @Nullable
  default Instant getInstant() {
    Timestamp timestamp = getWhen();
    if (timestamp == null) {
      return null;
    }
    return getWhen().toInstant();
  }

  @Nullable
  default Double getDoubleValue() {
    return getMeasurementUnit().map(MeasurementUnit::getValue).orElse(null);
  }

  @Nullable
  default String getUnit() {
    return getMeasurementUnit().map(MeasurementUnit::getUnit).orElse(null);
  }
}
