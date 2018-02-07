package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.mappers.PhysicalMeterMapper;

import static java.util.stream.Collectors.toList;

public class PhysicalMetersRepository implements PhysicalMeters {

  private final PhysicalMeterJpaRepository jpaRepository;
  private final PhysicalMeterMapper physicalMeterMapper;

  public PhysicalMetersRepository(
    PhysicalMeterJpaRepository jpaRepository,
    PhysicalMeterMapper physicalMeterMapper
  ) {
    this.jpaRepository = jpaRepository;
    this.physicalMeterMapper = physicalMeterMapper;
  }

  @Override
  public Optional<PhysicalMeter> findById(Long id) {
    return jpaRepository.findById(id)
      .map(physicalMeterMapper::toDomainModel);
  }

  @Override
  public Optional<PhysicalMeter> findByIdentity(String identity) {
    return jpaRepository.findByIdentity(identity)
      .map(physicalMeterMapper::toDomainModel);
  }

  @Override
  public List<PhysicalMeter> findByMedium(String medium) {
    return jpaRepository.findByMedium(medium)
      .stream()
      .map(physicalMeterMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public List<PhysicalMeter> findAll() {
    return StreamSupport.stream(jpaRepository.findAll().spliterator(), false)
      .map(physicalMeterMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public PhysicalMeter save(PhysicalMeter physicalMeter) {
    PhysicalMeterEntity entity = jpaRepository.save(physicalMeterMapper.toEntity(physicalMeter));
    return physicalMeterMapper.toDomainModel(entity);
  }
}
