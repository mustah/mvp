package com.elvaco.mvp.testing.repository;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementKey;
import com.elvaco.mvp.core.domainmodels.MeasurementParameter;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.testing.exception.NotImplementedYet;

import static java.util.Collections.emptyMap;

public class MockMeasurements extends MockRepository<Measurement.Id, Measurement>
  implements Measurements {

  @Override
  public Measurement save(Measurement measurement) {
    return saveMock(measurement);
  }

  @Override
  public void createOrUpdate(Measurement measurement) {
    saveMock(measurement);
  }

  @Override
  public Map<String, List<MeasurementValue>> findAverageForPeriod(
    MeasurementParameter parameter
  ) {
    return emptyMap();
  }

  @Override
  public Map<MeasurementKey, List<MeasurementValue>> findSeriesForPeriod(
    MeasurementParameter parameter
  ) {
    return emptyMap();
  }

  @Override
  public Map<MeasurementKey, List<MeasurementValue>> findAllForPeriod(
    MeasurementParameter parameter
  ) {
    return null;
  }

  @Override
  public Map<String, List<MeasurementValue>> findAverageAllForPeriod(
    MeasurementParameter parameter
  ) {
    return emptyMap();
  }

  @Override
  public List<Measurement> findAll(RequestParameters parameters) {
    throw new NotImplementedYet();
  }

  @Override
  public Optional<Measurement> firstForPhysicalMeterWithinDateRange(
    UUID physicalMeterId, ZonedDateTime after, ZonedDateTime beforeOrEqual
  ) {
    return filter(measurement -> measurement.physicalMeter.id.equals(physicalMeterId))
      .sorted(Comparator.comparing(o -> o.created))
      .filter(measurement -> measurement.created.isAfter(after)
        && ((measurement.created.isBefore(beforeOrEqual)
               || measurement.created.isEqual(beforeOrEqual))))
      .findFirst();
  }

  @Override
  protected Measurement copyWithId(Measurement.Id id, Measurement entity) {
    return Measurement.builder()
      .created(entity.created)
      .quantity(entity.quantity)
      .value(entity.value)
      .unit(entity.unit)
      .physicalMeter(entity.physicalMeter)
      .build();
  }

  @Override
  protected Measurement.Id generateId(Measurement entity) {
    return Measurement.idOf(entity.created, entity.quantity, entity.physicalMeter);
  }
}
