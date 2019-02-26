package com.elvaco.mvp.testing.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class MockMeterDefinitions extends MockRepository<Long, MeterDefinition>
  implements MeterDefinitions {

  public MockMeterDefinitions() {
    this(emptyList());
  }

  public MockMeterDefinitions(List<MeterDefinition> meterDefinitions) {
    meterDefinitions.forEach(this::saveMock);
  }

  @Override
  public MeterDefinition save(MeterDefinition meterDefinition) {
    return saveMock(meterDefinition);
  }

  @Override
  public Optional<MeterDefinition> findById(Long id) {
    return allMocks().stream().filter(meterDefinition -> id.equals(meterDefinition.getId()))
      .findAny();
  }

  @Override
  public Optional<MeterDefinition> findSystemMeterDefinition(Medium medium) {
    return Optional.empty();
  }

  @Override
  public List<MeterDefinition> findAll(UUID organisationId) {
    return allMocks().stream()
      .filter(md -> (md.organisation != null && md.organisation.id.equals(organisationId)))
      .collect(toList());
  }

  @Override
  public List<MeterDefinition> findAll() {
    return null;
  }

  @Override
  public void deleteById(Long id) {
    deleteMockById(id);
  }

  @Override
  protected MeterDefinition copyWithId(
    Long id, MeterDefinition entity
  ) {
    return new MeterDefinition(
      id,
      entity.organisation,
      entity.name,
      entity.medium,
      entity.autoApply,
      entity.quantities
    );
  }

  @Override
  protected Long generateId(MeterDefinition entity) {
    return nextId();
  }
}
