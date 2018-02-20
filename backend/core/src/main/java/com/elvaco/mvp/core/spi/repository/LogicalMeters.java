package com.elvaco.mvp.core.spi.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;

public interface LogicalMeters {

  Optional<LogicalMeter> findById(Long id);

  List<LogicalMeter> findAll();

  Page<LogicalMeter> findAll(Map<String, List<String>> filterParams, Pageable pageable);

  List<LogicalMeter> findAll(Map<String, List<String>> filterParams);

  LogicalMeter save(LogicalMeter logicalMeter);

  void deleteAll();

  Optional<LogicalMeter> findByOrganisationIdAndExternalId(Long organisationId, String externalId);
}
