package com.elvaco.mvp.core.spi.repository;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;

public interface Measurements {

  Optional<Measurement> findById(Long id);

  Measurement save(Measurement measurement);

  Collection<Measurement> save(Collection<Measurement> measurement);

  List<MeasurementValue> getAverageForPeriod(
    List<UUID> meterIds,
    String quantity,
    String unit,
    ZonedDateTime from,
    ZonedDateTime to,
    TemporalResolution resolution
  );

  List<MeasurementValue> getSeriesForPeriod(
    UUID meterId,
    String quantity,
    String unit,
    String mode,
    ZonedDateTime from,
    ZonedDateTime to
  );

  Optional<Measurement> findByPhysicalMeterIdAndQuantityAndCreated(
    UUID physicalMeterId,
    String quantity,
    ZonedDateTime when
  );

  List<Measurement> findLatestValues(
    UUID physicalMeterId
  );

}
