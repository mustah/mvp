package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.QuantityParameter;
import com.elvaco.mvp.core.dto.LegendDto;
import com.elvaco.mvp.core.dto.LogicalMeterSummaryDto;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LogicalMeterJpaRepository {

  <S extends LogicalMeterEntity> S save(S entity);

  Optional<LogicalMeterEntity> findById(UUID id);

  Optional<LogicalMeterEntity> findByPrimaryKey(UUID organisationId, UUID id);

  Optional<LogicalMeterEntity> findBy(UUID organisationId, String externalId);

  Page<String> findSecondaryAddresses(RequestParameters parameters, Pageable pageable);

  Page<String> findFacilities(RequestParameters parameters, Pageable pageable);

  List<LegendDto> findAllLegendItems(RequestParameters parameters, Pageable pageable);

  Set<LogicalMeterEntity> findAll(RequestParameters parameters);

  Page<LogicalMeterSummaryDto> findAll(RequestParameters parameters, Pageable pageable);

  void delete(UUID id, UUID organisationId);

  void deleteAll();

  void changeMeterDefinition(
    UUID organisationId,
    Long fromMeterDefinitionId,
    Long toMeterDefinitionId
  );

  List<QuantityParameter> getPreferredQuantityParameters(RequestParameters parameters);
}
