package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;

public interface PhysicalMeterJpaRepository {

  <S extends PhysicalMeterEntity> S save(S entity);

  Optional<PhysicalMeterEntity> findById(UUID id);

  List<PhysicalMeterEntity> findByMedium(String medium);

  List<PhysicalMeterEntity> findByOrganisationIdAndExternalId(
    UUID organisationId,
    String externalId
  );

  Optional<PhysicalMeterEntity> findByOrganisationIdAndExternalIdAndAddress(
    UUID organisationId,
    String externalId,
    String address
  );

  List<PhysicalMeterEntity> findAll();

  void deleteAll();

  void deleteById(UUID id);
}
