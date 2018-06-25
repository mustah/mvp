package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

import com.elvaco.mvp.adapters.spring.PageAdapter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.mappers.PhysicalMeterEntityMapper;
import com.elvaco.mvp.database.repository.queryfilters.PhysicalMeterQueryFilters;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class PhysicalMetersRepository implements PhysicalMeters {

  private final PhysicalMeterJpaRepository jpaRepository;

  @Override
  public List<PhysicalMeter> findByMedium(String medium) {
    return jpaRepository.findByMedium(medium)
      .stream()
      .map(PhysicalMeterEntityMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public List<PhysicalMeter> findAll() {
    return StreamSupport.stream(jpaRepository.findAll().spliterator(), false)
      .map(PhysicalMeterEntityMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public Page<PhysicalMeter> findAll(RequestParameters parameters, Pageable pageable) {
    return new PageAdapter<>(jpaRepository.findAll(
      new PhysicalMeterQueryFilters().toExpression(parameters),
      new PageRequest(
        pageable.getPageNumber(),
        pageable.getPageSize()
      )
    ).map(PhysicalMeterEntityMapper::toDomainModel));
  }

  @Override
  public PhysicalMeter save(PhysicalMeter physicalMeter) {
    PhysicalMeterEntity entity = jpaRepository.save(
      PhysicalMeterEntityMapper.toEntity(physicalMeter)
    );
    return PhysicalMeterEntityMapper.toDomainModel(entity);
  }

  @Override
  public Optional<PhysicalMeter> findByOrganisationIdAndExternalIdAndAddress(
    UUID organisationId,
    String externalId,
    String address
  ) {
    return jpaRepository.findByOrganisationIdAndExternalIdAndAddress(
      organisationId,
      externalId,
      address
    ).map(PhysicalMeterEntityMapper::toDomainModel);
  }
}
