package com.elvaco.mvp.core.spi.repository;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;

public interface Measurements {

  Optional<Measurement> findById(Long id);

  Measurement save(Measurement measurement);

  Collection<Measurement> save(Collection<Measurement> measurement);

  List<MeasurementValue> findAverageForPeriod(
    List<UUID> meterIds,
    Quantity seriesQuantity,
    ZonedDateTime from,
    ZonedDateTime to,
    TemporalResolution resolution
  );

  List<MeasurementValue> findSeriesForPeriod(
    UUID meterId,
    Quantity seriesQuantity,
    ZonedDateTime from,
    ZonedDateTime to,
    TemporalResolution resolution
  );

  Optional<Measurement> findBy(
    UUID physicalMeterId,
    ZonedDateTime created,
    String quantity
  );

  Optional<Measurement> findLatestReadout(
    UUID physicalMeterId,
    ZonedDateTime before,
    Quantity quantity
  );
}
