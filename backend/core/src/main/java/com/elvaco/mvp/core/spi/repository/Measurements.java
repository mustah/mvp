package com.elvaco.mvp.core.spi.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementKey;
import com.elvaco.mvp.core.domainmodels.MeasurementParameter;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
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

  Map<String, List<MeasurementValue>> findAverageForPeriod(MeasurementParameter parameter);

  Map<MeasurementKey, List<MeasurementValue>> findSeriesForPeriod(MeasurementParameter parameter);

  List<Measurement> findAll(RequestParameters parameters);

  Optional<Measurement> firstForPhysicalMeterWithinDateRange(
    UUID physicalMeterId,
    ZonedDateTime after,
    ZonedDateTime beforeOrEqual
  );
}
