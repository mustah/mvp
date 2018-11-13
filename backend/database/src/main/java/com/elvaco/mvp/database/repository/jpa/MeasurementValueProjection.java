package com.elvaco.mvp.database.repository.jpa;

import java.sql.Timestamp;
import java.time.Instant;
import javax.annotation.Nullable;

public interface MeasurementValueProjection {

  @Nullable
  Timestamp getWhen();

  @Nullable
  Double getValue();

  @Nullable
  default Instant getInstant() {
    Timestamp timestamp = getWhen();
    if (timestamp == null) {
      return null;
    }
    return getWhen().toInstant();
  }
}
