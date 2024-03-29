package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterSummary;
import com.elvaco.mvp.core.dto.LegendDto;
import com.elvaco.mvp.core.dto.LogicalMeterSummaryDto;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LogicalMeterUseCases {

  private final AuthenticatedUser currentUser;
  private final LogicalMeters logicalMeters;

  public List<LegendDto> findAllLegendItemsBy(RequestParameters parameters, Pageable pageable) {
    return logicalMeters.findAllLegendItemsBy(
      parameters.ensureOrganisationFilters(currentUser),
      pageable
    );
  }

  public List<LogicalMeter> findAllBy(RequestParameters parameters) {
    return logicalMeters.findAllBy(parameters.ensureOrganisationFilters(currentUser));
  }

  public Page<LogicalMeterSummaryDto> findAll(RequestParameters parameters, Pageable pageable) {
    return logicalMeters.findAll(parameters.ensureOrganisationFilters(currentUser), pageable);
  }

  public LogicalMeter save(LogicalMeter logicalMeter) {
    if (hasTenantAccess(logicalMeter.organisationId)) {
      return logicalMeters.save(logicalMeter);
    }
    throw new Unauthorized(
      "User '" + currentUser.getUsername() + "' is not allowed to create this meter."
    );
  }

  public Optional<LogicalMeter> findBy(UUID organisationId, String externalId) {
    if (currentUser.isWithinOrganisation(organisationId) || currentUser.isSuperAdmin()) {
      return logicalMeters.findByOrganisationIdAndExternalId(organisationId, externalId);
    }
    return Optional.empty();
  }

  public Optional<LogicalMeter> findById(UUID id) {
    if (currentUser.isSuperAdmin()) {
      return logicalMeters.findById(id);
    } else {
      return logicalMeters.findByPrimaryKey(
        currentUser.subOrganisationParameters().getEffectiveOrganisationId(), id);
    }
  }

  public Optional<LogicalMeter> deleteById(UUID id) {
    if (!currentUser.isSuperAdmin()) {
      throw new Unauthorized(
        "User '" + currentUser.getUsername() + "' is not allowed to delete this meter."
      );
    }
    return findById(id).map(logicalMeters::delete);
  }

  public MeterSummary summary(RequestParameters parameters) {
    return logicalMeters.summary(parameters.ensureOrganisationFilters(currentUser));
  }

  public Page<String> findSecondaryAddresses(RequestParameters parameters, Pageable pageable) {
    return logicalMeters.findSecondaryAddresses(
      parameters.ensureOrganisationFilters(currentUser),
      pageable
    );
  }

  public Page<String> findFacilities(RequestParameters parameters, Pageable pageable) {
    return logicalMeters.findFacilities(
      parameters.ensureOrganisationFilters(currentUser), pageable
    );
  }

  public Long meterCount(RequestParameters parameters) {
    return logicalMeters.meterCount(parameters.ensureOrganisationFilters(currentUser));
  }

  private boolean hasTenantAccess(UUID organisationId) {
    return currentUser.isSuperAdmin() || currentUser.isWithinOrganisation(organisationId);
  }
}
