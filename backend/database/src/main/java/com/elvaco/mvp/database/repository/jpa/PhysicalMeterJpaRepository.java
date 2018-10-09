package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PhysicalMeterJpaRepository {

  <S extends PhysicalMeterEntity> S save(S entity);

  Optional<PhysicalMeterEntity> findById(UUID id);

  List<PhysicalMeterEntity> findByMedium(String medium);

  Optional<PhysicalMeterEntity> findByOrganisationIdAndExternalIdAndAddress(
    UUID organisationId,
    String externalId,
    String address
  );

  List<PhysicalMeterEntity> findAll();

  Page<PhysicalMeterEntity> findAll(Predicate predicate, Pageable pageable);

  void deleteAll();

  void deleteById(UUID meterId);

  Page<String> findAddresses(Predicate predicate, Pageable pageable);

  Page<String> findFacilities(Predicate predicate, Pageable pageable);
}
