package com.elvaco.mvp.testing.repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.LogicalMeterCollectionStats;
import com.elvaco.mvp.core.domainmodels.MeterSummary;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.testing.exception.NotImplementedYet;

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
  public Optional<LogicalMeter> findById(UUID id) {
    return filter(logicalMeter -> logicalMeter.id != null)
      .filter(isSameId(id))
      .findFirst();
  }

  @Override
  public Optional<LogicalMeter> findByOrganisationIdAndId(UUID organisationId, UUID id) {
    return filter(isSameOrganisationId(organisationId))
      .filter(isSameId(id))
      .findFirst();
  }

  @Override
  public Optional<LogicalMeter> findByOrganisationIdAndExternalId(
    UUID organisationId,
    String externalId
  ) {
    return filter(logicalMeter -> logicalMeter.externalId.equals(externalId))
      .filter(isSameOrganisationId(organisationId))
      .findFirst();
  }

  @Override
  public Optional<LogicalMeter> findBy(RequestParameters parameters) {
    return Optional.empty();
  }

  @Override
  public Page<LogicalMeter> findAllWithStatuses(RequestParameters parameters, Pageable pageable) {
    return null;
  }

  @Override
  public List<LogicalMeter> findAllWithStatuses(RequestParameters parameters) {
    return filter(isWithinOrganisation(parameters))
      .collect(toList());
  }

  @Override
  public List<LogicalMeter> findAllBy(RequestParameters parameters) {
    return emptyList();
  }

  @Override
  public List<LogicalMeter> findAllByOrganisationId(UUID organisationId) {
    return filter(isSameOrganisationId(organisationId))
      .collect(toList());
  }

  @Override
  public LogicalMeter save(LogicalMeter logicalMeter) {
    return saveMock(logicalMeter);
  }

  @Override
  public MeterSummary summary(RequestParameters parameters) {
    throw new NotImplementedYet();
  }

  @Override
  public List<LogicalMeterCollectionStats> findMissingMeasurements(RequestParameters parameters) {
    return emptyList();
  }

  @Override
  public void delete(LogicalMeter logicalMeter) {
    throw new NotImplementedYet();
  }

  @Override
  protected LogicalMeter copyWithId(UUID id, LogicalMeter entity) {
    return new LogicalMeter(
      id,
      entity.externalId,
      entity.organisationId,
      entity.meterDefinition,
      entity.created,
      entity.physicalMeters,
      emptyList(),
      entity.latestReadouts,
      entity.location,
      entity.expectedMeasurementCount,
      entity.missingMeasurementCount,
      entity.currentStatus
    );
  }

  @Override
  protected UUID generateId() {
    return randomUUID();
  }

  private Predicate<LogicalMeter> isSameOrganisationId(UUID organisationId) {
    return logicalMeter -> logicalMeter.organisationId == organisationId;
  }

  private Predicate<LogicalMeter> isSameId(UUID id) {
    return logicalMeter -> Objects.equals(logicalMeter.id, id);
  }

  private Predicate<LogicalMeter> isWithinOrganisation(RequestParameters parameters) {
    return logicalMeter ->
      !parameters.hasName("organisation")
        || Objects.equals(
        parameters.getFirst("organisation"),
        logicalMeter.organisationId.toString()
      );
  }
}
