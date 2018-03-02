package com.elvaco.mvp.core.spi.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;

public interface LogicalMeters {

  Optional<LogicalMeter> findById(UUID id);

  Optional<LogicalMeter> findByOrganisationIdAndId(UUID organisationId, UUID id);

  List<LogicalMeter> findAll();

  Page<LogicalMeter> findAll(Map<String, List<String>> filterParams, Pageable pageable);

  List<LogicalMeter> findAll(Map<String, List<String>> filterParams);

  LogicalMeter save(LogicalMeter logicalMeter);

  void deleteAll();

  Optional<LogicalMeter> findByOrganisationIdAndExternalId(UUID organisationId, String externalId);

  List<LogicalMeter> findByOrganisationId(UUID organisationId);
}
