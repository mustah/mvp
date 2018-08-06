package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PagedLogicalMeter;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface LogicalMeterJpaRepository {

  <S extends LogicalMeterEntity> S save(S entity);

  Optional<LogicalMeterEntity> findById(UUID id);

  Optional<LogicalMeterEntity> findBy(UUID organisationId, String externalId);

  Optional<LogicalMeterEntity> findBy(RequestParameters parameters);

  List<LogicalMeterEntity> findByOrganisationId(UUID organisationId);

  List<LogicalMeterEntity> findAll(RequestParameters parameters, Predicate predicate);

  List<LogicalMeterEntity> findAll(RequestParameters parameters, Predicate predicate, Sort sort);

  Page<PagedLogicalMeter> findAll(
    RequestParameters parameters,
    Predicate predicate,
    Pageable pageable
  );

  Map<UUID, Long> findMeasurementCounts(Predicate predicate);

  Map<UUID, List<PhysicalMeterStatusLogEntity>> findStatusesGroupedByPhysicalMeterId(
    Predicate predicate
  );

  void delete(UUID id, UUID organisationId);

  void deleteAll();
}
