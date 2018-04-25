package com.elvaco.mvp.testing.repository;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.core.spi.repository.Measurements;

import static java.util.Collections.emptyList;

public class MockMeasurements extends MockRepository<Long, Measurement> implements Measurements {

  @Override
  public Optional<Measurement> findById(Long id) {
    return filter(measurement -> Objects.equals(measurement.id, id)).findFirst();
  }

  @Override
  public Optional<Measurement> findByPhysicalMeterIdAndQuantityAndCreated(
    UUID physicalMeterId,
    String quantity,
    ZonedDateTime created
  ) {
    return filter(measurement -> measurement.physicalMeter.id.equals(physicalMeterId))
      .filter(measurement -> measurement.quantity.equals(quantity))
      .filter(measurement -> measurement.created.equals(created))
      .findAny();
  }

  @Override
  public Measurement save(Measurement measurement) {
    return saveMock(measurement);
  }

  @Override
  public Collection<Measurement> save(Collection<Measurement> measurements) {
    return measurements.stream()
      .map(this::saveMock)
      .collect(Collectors.toList());
  }

  @Override
  public List<MeasurementValue> findAverageForPeriod(
    List<UUID> meterIds,
    Quantity seriesQuantity,
    ZonedDateTime from,
    ZonedDateTime to,
    TemporalResolution resolution
  ) {
    return emptyList();
  }

  @Override
  public List<MeasurementValue> findSeriesForPeriod(
    UUID meterId,
    Quantity seriesQuantity,
    ZonedDateTime from,
    ZonedDateTime to
  ) {
    return emptyList();
  }

  @Override
  public List<Measurement> findLatestValues(UUID physicalMeterId) {
    throw new UnsupportedOperationException("findLatestValues not implemented!");
  }

  @Override
  protected Measurement copyWithId(Long id, Measurement entity) {
    return new Measurement(
      id,
      entity.created,
      entity.quantity,
      entity.value,
      entity.unit,
      entity.physicalMeter
    );
  }

  @Override
  protected Long generateId() {
    return nextId();
  }
}
