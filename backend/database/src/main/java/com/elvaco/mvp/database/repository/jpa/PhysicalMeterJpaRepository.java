package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PhysicalMeterJpaRepository
  extends PagingAndSortingRepository<PhysicalMeterEntity, UUID>,
          QueryDslPredicateExecutor<PhysicalMeterEntity> {

  List<PhysicalMeterEntity> findByMedium(String medium);

  Optional<PhysicalMeterEntity> findByOrganisationIdAndExternalIdAndAddress(
    UUID organisationId,
    String externalId,
    String address
  );
}
