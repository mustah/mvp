package com.elvaco.mvp.core.spi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterSummary;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;

public interface LogicalMeters {

  Optional<LogicalMeter> findById(UUID id);

  Optional<LogicalMeter> findByOrganisationIdAndId(UUID organisationId, UUID id);

  Page<LogicalMeter> findAllWithStatuses(RequestParameters parameters, Pageable pageable);

  List<LogicalMeter> findAllWithStatuses(RequestParameters parameters);

  List<LogicalMeter> findAllBy(RequestParameters parameters);

  List<LogicalMeter> findAllByOrganisationId(UUID organisationId);

  LogicalMeter save(LogicalMeter logicalMeter);

  Optional<LogicalMeter> findByOrganisationIdAndExternalId(UUID organisationId, String externalId);

  MeterSummary summary(RequestParameters parameters);

  void delete(LogicalMeter logicalMeter);

  Optional<LogicalMeter> findOneBy(RequestParameters parameters);
}
