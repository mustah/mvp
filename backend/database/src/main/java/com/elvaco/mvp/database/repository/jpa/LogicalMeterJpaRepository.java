package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LogicalMeterCollectionStats;
import com.elvaco.mvp.core.domainmodels.QuantityParameter;
import com.elvaco.mvp.core.dto.CollectionStatsDto;
import com.elvaco.mvp.core.dto.CollectionStatsPerDateDto;
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

  Optional<LogicalMeterEntity> findBy(RequestParameters parameters);

  Page<String> findSecondaryAddresses(RequestParameters parameters, Pageable pageable);

  Page<String> findFacilities(RequestParameters parameters, Pageable pageable);

  List<LogicalMeterEntity> findByOrganisationId(UUID organisationId);

  Set<LogicalMeterEntity> findAll(RequestParameters parameters);

  Page<LogicalMeterSummaryDto> findAll(RequestParameters parameters, Pageable pageable);

  List<LogicalMeterCollectionStats> findMeterCollectionStats(RequestParameters parameters);

  Page<CollectionStatsDto> findAllCollectionStats(RequestParameters parameters, Pageable pageable);

  List<CollectionStatsPerDateDto> findAllCollectionStatsPerDate(RequestParameters parameters);

  void delete(UUID id, UUID organisationId);

  void deleteAll();

  void changeMeterDefinition(
    UUID organisationId,
    Long fromMeterDefinitionId,
    Long toMeterDefinitionId
  );

  List<QuantityParameter> getPreferredQuantityParameters(RequestParameters parameters);
}
