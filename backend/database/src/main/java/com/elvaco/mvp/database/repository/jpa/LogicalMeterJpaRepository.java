package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LogicalMeterCollectionStats;
import com.elvaco.mvp.core.dto.LogicalMeterSummaryDto;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterWithLocation;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
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

  List<LogicalMeterEntity> findAll(RequestParameters parameters);

  Page<LogicalMeterSummaryDto> findAll(RequestParameters parameters, Pageable pageable);

  List<LogicalMeterWithLocation> findAllForSelectionTree(RequestParameters parameters);

  List<LogicalMeterCollectionStats> findMissingMeterReadingsCounts(RequestParameters parameters);

  Map<UUID, List<PhysicalMeterStatusLogEntity>> findStatusesGroupedByPhysicalMeterId(
    RequestParameters parameters
  );

  void delete(UUID id, UUID organisationId);

  void deleteAll();
}
