package com.elvaco.mvp.core.spi.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementKey;
import com.elvaco.mvp.core.domainmodels.MeasurementParameter;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;

public interface Measurements {

  Measurement save(Measurement measurement, LogicalMeter logicalMeter);

  void createOrUpdate(Measurement measurement, LogicalMeter logicalMeter);

  Map<String, List<MeasurementValue>> findAverageForPeriod(MeasurementParameter parameter);

  Map<MeasurementKey, List<MeasurementValue>> findSeriesForPeriod(MeasurementParameter parameter);

  Map<MeasurementKey, List<MeasurementValue>> findAllForPeriod(MeasurementParameter parameter);

  Map<String, List<MeasurementValue>> findAverageAllForPeriod(MeasurementParameter parameter);

  Optional<Measurement> firstForPhysicalMeterWithinDateRange(
    UUID organisationId,
    UUID physicalMeterId,
    ZonedDateTime after,
    ZonedDateTime beforeOrEqual
  );

  int popAndCalculate(int limit, long ageMillis, int numberOfWorkers, int workerId);
}
