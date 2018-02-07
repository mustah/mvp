package com.elvaco.mvp.repository.jpa;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.entity.meter.PhysicalMeterEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PhysicalMeterJpaRepository
  extends PagingAndSortingRepository<PhysicalMeterEntity, Long> {

  Optional<PhysicalMeterEntity> findById(Long id);

  Optional<PhysicalMeterEntity> findByIdentity(String identity);

  List<PhysicalMeterEntity> findByMedium(String medium);
}
