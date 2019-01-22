package com.elvaco.mvp.core.spi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.LogicalMeterCollectionStats;
import com.elvaco.mvp.core.domainmodels.MeterSummary;
import com.elvaco.mvp.core.dto.LogicalMeterSummaryDto;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;

public interface LogicalMeters {

  Optional<LogicalMeter> findById(UUID id);

  Optional<LogicalMeter> findByPrimaryKey(UUID organisationId, UUID id);

  Optional<LogicalMeter> findByOrganisationIdAndExternalId(UUID organisationId, String externalId);

  Optional<LogicalMeter> findBy(RequestParameters parameters);

  Page<String> findSecondaryAddresses(RequestParameters parameters, Pageable pageable);

  Page<String> findFacilities(RequestParameters parameters, Pageable pageable);

  Page<LogicalMeterSummaryDto> findAll(RequestParameters parameters, Pageable pageable);

  List<LogicalMeter> findAllWithDetails(RequestParameters parameters);

  List<LogicalMeter> findAllBy(RequestParameters parameters);

  List<LogicalMeter> findAllByOrganisationId(UUID organisationId);

  List<LogicalMeter> findAllForSelectionTree(RequestParameters parameters);

  LogicalMeter save(LogicalMeter logicalMeter);

  MeterSummary summary(RequestParameters parameters);

  List<LogicalMeterCollectionStats> findMeterCollectionStats(RequestParameters parameters);

  LogicalMeter delete(LogicalMeter logicalMeter);
}
