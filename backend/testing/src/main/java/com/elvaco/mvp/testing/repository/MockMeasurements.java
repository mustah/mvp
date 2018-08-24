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
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.testing.exception.NotImplementedYet;

import static java.util.Collections.emptyList;

public class MockMeasurements extends MockRepository<Measurement.Id, Measurement>
  implements Measurements {

  @Override
  public Optional<Measurement> findById(Measurement.Id id) {
    return filter(measurement -> Objects.equals(measurement.getId(), id)).findFirst();
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
  public void save(
    PhysicalMeter physicalMeter,
    ZonedDateTime created,
    String quantity,
    String unit,
    double value
  ) {
    Measurement.MeasurementBuilder builder = Measurement.builder()
      .physicalMeter(physicalMeter)
      .created(created)
      .quantity(quantity)
      .unit(unit)
      .value(value);

    Measurement measurement = builder.build();

    saveMock(measurement);
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
    ZonedDateTime to,
    TemporalResolution resolution
  ) {
    return emptyList();
  }

  @Override
  public Optional<Measurement> findBy(
    UUID physicalMeterId,
    ZonedDateTime created,
    String quantity
  ) {
    return filter(measurement -> measurement.physicalMeter.id.equals(physicalMeterId))
      .filter(measurement -> measurement.quantity.equals(quantity))
      .filter(measurement -> measurement.created.equals(created))
      .findAny();
  }

  @Override
  public Optional<Measurement> findLatestReadout(
    UUID meterId,
    ZonedDateTime before,
    Quantity quantity
  ) {
    throw new NotImplementedYet();
  }

  @Override
  protected Measurement copyWithId(Measurement.Id id, Measurement entity) {
    return new Measurement(
      entity.created,
      entity.quantity,
      entity.value,
      entity.unit,
      entity.physicalMeter
    );
  }

  @Override
  protected Measurement.Id generateId(Measurement entity) {
    return Measurement.idOf(entity.created, entity.quantity, entity.physicalMeter);
  }
}
