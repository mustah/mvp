package com.elvaco.mvp.testing.repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class MockLogicalMeters extends MockRepository<Long, LogicalMeter> implements LogicalMeters {

  public MockLogicalMeters(List<LogicalMeter> logicalMeters) {
    logicalMeters.forEach(this::saveMock);
  }

  @Override
  protected LogicalMeter copyWithId(Long id, LogicalMeter entity) {
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
  protected Long generateId() {
    return nextId();
  }

  @Override
  public Optional<LogicalMeter> findById(Long id) {
    return filter(logicalMeter -> logicalMeter.id != null)
      .filter(logicalMeter -> Objects.equals(logicalMeter.id, id))
      .findFirst();
  }

  @Override
  public Optional<LogicalMeter> findByOrganisationIdAndId(UUID organisationId, Long id) {
    return filter(logicalMeter -> logicalMeter.organisationId.equals(organisationId)).filter(
      logicalMeter -> Objects.equals(logicalMeter.id, id))
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
    UUID organisationId,
    String externalId
  ) {
    return filter(logicalMeter -> logicalMeter.externalId.equals(externalId))
      .filter(logicalMeter -> logicalMeter.organisationId == organisationId)
      .findFirst();
  }

  @Override
  public List<LogicalMeter> findByOrganisationId(UUID organisationId) {
    return filter(logicalMeter -> logicalMeter.organisationId == organisationId)
      .collect(toList());
  }
}
