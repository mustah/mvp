package com.elvaco.mvp.testing.repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.MeterSummary;
import com.elvaco.mvp.core.domainmodels.QuantityParameter;
import com.elvaco.mvp.core.dto.CollectionStatsDto;
import com.elvaco.mvp.core.dto.CollectionStatsPerDateDto;
import com.elvaco.mvp.core.dto.LegendDto;
import com.elvaco.mvp.core.dto.LogicalMeterSummaryDto;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.testing.exception.NotImplementedYet;

import static com.elvaco.mvp.core.spi.data.RequestParameter.ORGANISATION;
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
  public Optional<LogicalMeter> findByPrimaryKey(UUID organisationId, UUID id) {
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
  public Page<String> findSecondaryAddresses(RequestParameters parameters, Pageable pageable) {
    throw new NotImplementedYet();
  }

  @Override
  public Page<String> findFacilities(
    RequestParameters parameters, Pageable pageable
  ) {
    throw new NotImplementedYet();
  }

  @Override
  public Page<LogicalMeterSummaryDto> findAll(RequestParameters parameters, Pageable pageable) {
    throw new NotImplementedYet();
  }

  @Override
  public Page<CollectionStatsDto> findAllCollectionStats(
    RequestParameters parameters, Pageable pageable
  ) {
    return null;
  }

  @Override
  public List<LegendDto> findAllLegendsBy(RequestParameters parameters) {
    throw new NotImplementedYet();
  }

  @Override
  public List<LogicalMeter> findAllBy(RequestParameters parameters) {
    return filter(isWithinOrganisation(parameters))
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
  public long meterCount(RequestParameters parameters) {
    throw new NotImplementedYet();
  }

  @Override
  public List<CollectionStatsPerDateDto> findAllCollectionStatsPerDate(
    RequestParameters parameters
  ) {
    return null;
  }

  @Override
  public LogicalMeter delete(LogicalMeter logicalMeter) {
    throw new NotImplementedYet();
  }

  @Override
  public void changeMeterDefinition(
    UUID organisationId,
    MeterDefinition fromMeterDefinition,
    MeterDefinition toMeterDefinition
  ) {
    filter(isSameOrganisationId(organisationId))
      .filter(lm -> lm.getMeterDefinition().id.equals(fromMeterDefinition.id))
      .forEach(lm -> saveMock(lm.toBuilder().meterDefinition(toMeterDefinition).build()));
  }

  @Override
  public List<QuantityParameter> getPreferredQuantityParameters(RequestParameters parameters) {
    return emptyList();
  }

  @Override
  protected LogicalMeter copyWithId(UUID id, LogicalMeter logicalMeter) {
    return LogicalMeter.builder()
      .id(id)
      .externalId(logicalMeter.externalId)
      .organisationId(logicalMeter.organisationId)
      .meterDefinition(logicalMeter.meterDefinition)
      .created(logicalMeter.created)
      .physicalMeters(logicalMeter.physicalMeters)
      .location(logicalMeter.location)
      .alarms(logicalMeter.alarms)
      .build();
  }

  @Override
  protected UUID generateId(LogicalMeter entity) {
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
      !parameters.hasParam(ORGANISATION) || Objects.equals(
        parameters.getFirst(ORGANISATION),
        logicalMeter.organisationId.toString()
      );
  }
}
