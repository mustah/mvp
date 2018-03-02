package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.querydsl.core.types.Predicate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface LogicalMeterJpaRepository
  extends
  QueryDslPredicateExecutor<LogicalMeterEntity>,
  JpaRepository<LogicalMeterEntity, UUID> {

  Optional<LogicalMeterEntity> findById(UUID id);

  @Override
  List<LogicalMeterEntity> findAll(Predicate predicate);

  Optional<LogicalMeterEntity> findByOrganisationIdAndExternalId(
    UUID organisationId,
    String externalId
  );

  Optional<LogicalMeterEntity> findByOrganisationIdAndId(UUID organisationId, UUID id);

  List<LogicalMeterEntity> findByOrganisationId(UUID organisationId);
}
