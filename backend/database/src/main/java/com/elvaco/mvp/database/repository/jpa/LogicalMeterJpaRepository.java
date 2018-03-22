package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface LogicalMeterJpaRepository {

  <S extends LogicalMeterEntity> S save(S entity);

  List<LogicalMeterEntity> findAll();

  List<LogicalMeterEntity> findAll(Predicate predicate);

  List<LogicalMeterEntity> findAll(Predicate predicate, Sort sort);

  Page<LogicalMeterEntity> findAll(Predicate predicate, Pageable pageable);

  List<LogicalMeterEntity> findByOrganisationId(UUID organisationId);

  Optional<LogicalMeterEntity> findById(UUID id);

  Optional<LogicalMeterEntity> findBy(UUID organisationId, String externalId);

  Optional<LogicalMeterEntity> findBy(UUID organisationId, UUID id);

  void deleteAll();
}
