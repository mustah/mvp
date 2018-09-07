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
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.mappers.PhysicalMeterEntityMapper;
import com.elvaco.mvp.database.repository.queryfilters.PhysicalMeterQueryFilters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;

import static com.elvaco.mvp.database.repository.mappers.PhysicalMeterEntityMapper.toDomainModel;
import static com.elvaco.mvp.database.repository.mappers.PhysicalMeterEntityMapper.toEntity;
import static com.elvaco.mvp.database.repository.queryfilters.SortUtil.getSortOrNull;
import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
public class PhysicalMetersRepository implements PhysicalMeters {

  private final PhysicalMeterJpaRepository physicalMeterJpaRepository;

  @Override
  public List<PhysicalMeter> findByMedium(String medium) {
    return physicalMeterJpaRepository.findByMedium(medium)
      .stream()
      .map(PhysicalMeterEntityMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public List<PhysicalMeter> findAll() {
    return StreamSupport.stream(physicalMeterJpaRepository.findAll().spliterator(), false)
      .map(PhysicalMeterEntityMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public Page<PhysicalMeter> findAll(RequestParameters parameters, Pageable pageable) {
    return new PageAdapter<>(physicalMeterJpaRepository.findAll(
      new PhysicalMeterQueryFilters().toExpression(parameters),
      new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), getSortOrNull(parameters))
    ).map(PhysicalMeterEntityMapper::toDomainModel));
  }

  @Override
  @CacheEvict(
    cacheNames = "physicalMeter.organisationIdExternalIdAddress",
    key = "#physicalMeter.organisation.id + #physicalMeter.externalId + #physicalMeter.address"
  )
  public PhysicalMeter save(PhysicalMeter physicalMeter) {
    try {
      return toDomainModel(physicalMeterJpaRepository.save(toEntity(physicalMeter)));
    } catch (DataIntegrityViolationException ex) {
      log.warn("Constraint violation: ", ex);
      return physicalMeter;
    }
  }

  @Override
  @Cacheable(
    cacheNames = "physicalMeter.organisationIdExternalIdAddress",
    key = "#organisationId + #externalId + #address"
  )
  public Optional<PhysicalMeter> findByWithStatuses(
    UUID organisationId,
    String externalId,
    String address
  ) {
    return physicalMeterJpaRepository.findByOrganisationIdAndExternalIdAndAddress(
      organisationId,
      externalId,
      address
    ).map(PhysicalMeterEntityMapper::toDomainModel);
  }

  @Override
  public Optional<PhysicalMeter> findByWithAlarms(
    UUID organisationId,
    String externalId,
    String address
  ) {
    return physicalMeterJpaRepository.findByOrganisationIdAndExternalIdAndAddress(
      organisationId,
      externalId,
      address
    ).map(PhysicalMeterEntityMapper::toDomainModelWithAlarms);
  }

  @Override
  @Cacheable(
    cacheNames = "physicalMeter.organisationIdExternalIdAddress",
    key = "#organisationId + #externalId + #address"
  )
  public Optional<PhysicalMeter> findBy(
    UUID organisationId,
    String externalId,
    String address
  ) {
    return physicalMeterJpaRepository.findByOrganisationIdAndExternalIdAndAddress(
      organisationId,
      externalId,
      address
    ).map(PhysicalMeterEntityMapper::toDomainModelWithoutStatusLogs);
  }
}
