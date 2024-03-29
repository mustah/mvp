package com.elvaco.mvp.core.spi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.MeterSummary;
import com.elvaco.mvp.core.domainmodels.QuantityParameter;
import com.elvaco.mvp.core.dto.LegendDto;
import com.elvaco.mvp.core.dto.LogicalMeterSummaryDto;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;

public interface LogicalMeters {

  Optional<LogicalMeter> findById(UUID id);

  Optional<LogicalMeter> findByPrimaryKey(UUID organisationId, UUID id);

  Optional<LogicalMeter> findByOrganisationIdAndExternalId(UUID organisationId, String externalId);

  Page<String> findSecondaryAddresses(RequestParameters parameters, Pageable pageable);

  Page<String> findFacilities(RequestParameters parameters, Pageable pageable);

  Page<LogicalMeterSummaryDto> findAll(RequestParameters parameters, Pageable pageable);

  List<LegendDto> findAllLegendItemsBy(RequestParameters parameters, Pageable pageable);

  List<LogicalMeter> findAllBy(RequestParameters parameters);

  LogicalMeter save(LogicalMeter logicalMeter);

  MeterSummary summary(RequestParameters parameters);

  long meterCount(RequestParameters parameters);

  LogicalMeter delete(LogicalMeter logicalMeter);

  void changeMeterDefinition(
    UUID organisationId,
    MeterDefinition fromMeterDefinition,
    MeterDefinition toMeterDefinition
  );

  List<QuantityParameter> getPreferredQuantityParameters(RequestParameters parameters);
}
