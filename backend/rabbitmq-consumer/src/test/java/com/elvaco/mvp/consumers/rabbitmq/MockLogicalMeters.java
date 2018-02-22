package com.elvaco.mvp.consumers.rabbitmq;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;

import static java.util.Collections.emptyList;

class MockLogicalMeters extends MockRepository<LogicalMeter> implements LogicalMeters {
  @Override
  Optional<Long> getId(LogicalMeter entity) {
    return Optional.ofNullable(entity.id);
  }

  @Override
  LogicalMeter copyWithId(Long id, LogicalMeter entity) {
    return new LogicalMeter(
      id,
      entity.externalId,
      entity.organisationId,
      entity.location,
      entity.created,
      entity.physicalMeters,
      entity.meterDefinition,
      entity.meterStatusLogs,
      emptyList()
    );
  }

  @Override
  public Optional<LogicalMeter> findById(Long id) {
    return filter(logicalMeter -> logicalMeter.id != null)
      .filter(logicalMeter -> Objects.equals(logicalMeter.id, id))
      .findFirst();
  }

  @Override
  public List<LogicalMeter> findAll() {
    return allMocks();
  }

  @Override
  public Page<LogicalMeter> findAll(
    Map<String, List<String>> filterParams, Pageable pageable
  ) {
    return null;
  }

  @Override
  public List<LogicalMeter> findAll(Map<String, List<String>> filterParams) {
    return null;
  }

  @Override
  public LogicalMeter save(LogicalMeter logicalMeter) {
    return saveMock(logicalMeter);
  }

  @Override
  public void deleteAll() {

  }

  @Override
  public Optional<LogicalMeter> findByOrganisationIdAndExternalId(
    Long organisationId, String externalId
  ) {
    return filter(logicalMeter -> logicalMeter.externalId.equals(externalId))
      .filter(logicalMeter -> logicalMeter.organisationId.equals(organisationId))
      .findFirst();
  }
}
