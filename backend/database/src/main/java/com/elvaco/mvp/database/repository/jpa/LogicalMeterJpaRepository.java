package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.MeterSummary;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface LogicalMeterJpaRepository {

  <S extends LogicalMeterEntity> S save(S entity);

  List<LogicalMeterEntity> findAll();

  List<LogicalMeterEntity> findAll(RequestParameters parameters, Predicate predicate);

  List<LogicalMeterEntity> findAll(RequestParameters parameters, Predicate predicate, Sort sort);

  Page<PagedLogicalMeter> findAll(
    RequestParameters parameters,
    Predicate predicate,
    Pageable pageable
  );

  List<LogicalMeterEntity> findByOrganisationId(UUID organisationId);

  Optional<LogicalMeterEntity> findById(UUID id);

  Optional<LogicalMeterEntity> findOneBy(UUID organisationId, String externalId);

  Optional<LogicalMeterEntity> findOneBy(RequestParameters parameters);

  MeterSummary summary(RequestParameters parameters, Predicate predicate);

  void deleteAll();

  void delete(UUID id, UUID organisationId);

  Map<UUID, Long> findMeasurementCounts(Predicate predicate);
}
