package com.elvaco.mvp.core.spi.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.core.spi.data.RequestParameters;

public interface Measurements {

  Measurement save(Measurement measurement);

  void createOrUpdate(
    PhysicalMeter physicalMeter,
    ZonedDateTime created,
    String quantity,
    String unit,
    double value
  );

  List<MeasurementValue> findAverageForPeriod(
    List<UUID> meterIds,
    Quantity quantity,
    ZonedDateTime from,
    ZonedDateTime to,
    TemporalResolution resolution
  );

  List<MeasurementValue> findSeriesForPeriod(
    UUID meterId,
    Quantity quantity,
    ZonedDateTime from,
    ZonedDateTime to,
    TemporalResolution resolution
  );

  List<Measurement> findAll(RequestParameters parameters);

  Optional<Measurement> firstForPhysicalMeterWithinDateRange(
    UUID physicalMeterId,
    ZonedDateTime after,
    ZonedDateTime beforeOrEqual
  );
}
