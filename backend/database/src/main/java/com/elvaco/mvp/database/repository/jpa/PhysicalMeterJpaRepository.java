package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PhysicalMeterJpaRepository
  extends PagingAndSortingRepository<PhysicalMeterEntity, Long> {

  Optional<PhysicalMeterEntity> findById(Long id);

  List<PhysicalMeterEntity> findByMedium(String medium);

  Optional<PhysicalMeterEntity> findByOrganisationIdAndExternalIdAndAddress(
    Long organisationId,
    String externalId,
    String address
  );
}
