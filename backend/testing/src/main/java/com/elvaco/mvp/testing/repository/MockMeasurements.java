package com.elvaco.mvp.testing.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.spi.repository.Measurements;

public class MockMeasurements extends MockRepository<Long, Measurement> implements Measurements {

  @Override
  public List<Measurement> findAllByScale(
    String scale, Map<String, List<String>> filterParams
  ) {
    throw new UnsupportedOperationException("findAllByScale not implemented!");
  }

  @Override
  public List<Measurement> findAll(Map<String, List<String>> filterParams) {
    if (!filterParams.isEmpty()) {
      throw new UnsupportedOperationException("filter params not implemented!");
    }
    return allMocks();
  }

  @Override
  public Optional<Measurement> findById(Long id) {
    return filter(measurement -> Objects.equals(measurement.id, id)).findFirst();
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
