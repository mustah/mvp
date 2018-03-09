package com.elvaco.mvp.testing.repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

public class MockLogicalMeters extends MockRepository<UUID, LogicalMeter> implements LogicalMeters {

  public MockLogicalMeters() {
    this(emptyList());
  }

  public MockLogicalMeters(List<LogicalMeter> logicalMeters) {
    logicalMeters.forEach(this::saveMock);
  }

  @Override
  protected LogicalMeter copyWithId(UUID id, LogicalMeter entity) {
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
  protected UUID generateId() {
    return randomUUID();
  }

  @Override
  public Optional<LogicalMeter> findById(UUID id) {
    return filter(logicalMeter -> logicalMeter.id != null)
      .filter(logicalMeter -> Objects.equals(logicalMeter.id, id))
      .findFirst();
  }

  @Override
  public Optional<LogicalMeter> findByOrganisationIdAndId(UUID organisationId, UUID id) {
    return filter(logicalMeter -> logicalMeter.organisationId.equals(organisationId)).filter(
      logicalMeter -> Objects.equals(logicalMeter.id, id))
      .findFirst();
  }

  @Override
  public List<LogicalMeter> findAll() {
    return allMocks();
  }

  @Override
  public Page<LogicalMeter> findAll(RequestParameters parameters, Pageable pageable) {
    return null;
  }

  @Override
  public List<LogicalMeter> findAll(RequestParameters parameters) {
    return null;
  }

  @Override
  public LogicalMeter save(LogicalMeter logicalMeter) {
    return saveMock(logicalMeter);
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
