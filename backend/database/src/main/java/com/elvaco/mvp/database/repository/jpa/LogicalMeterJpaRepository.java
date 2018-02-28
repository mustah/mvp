package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.querydsl.core.types.Predicate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface LogicalMeterJpaRepository
  extends
  QueryDslPredicateExecutor<LogicalMeterEntity>,
  JpaRepository<LogicalMeterEntity, Long> {

  Optional<LogicalMeterEntity> findById(Long id);

  @Override
  List<LogicalMeterEntity> findAll(Predicate predicate);

  Optional<LogicalMeterEntity> findByOrganisationIdAndExternalId(
    Long organisationId,
    String externalId
  );

  Optional<LogicalMeterEntity> findByOrganisationIdAndId(Long organisationId, Long id);

  List<LogicalMeterEntity> findByOrganisationId(Long organisationId);
}
