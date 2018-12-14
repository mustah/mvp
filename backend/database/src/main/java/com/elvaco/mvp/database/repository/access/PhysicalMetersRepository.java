package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.mappers.PhysicalMeterEntityMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;

import static com.elvaco.mvp.database.repository.mappers.PhysicalMeterEntityMapper.toDomainModel;
import static com.elvaco.mvp.database.repository.mappers.PhysicalMeterEntityMapper.toEntity;
import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
public class PhysicalMetersRepository implements PhysicalMeters {

  private final PhysicalMeterJpaRepository physicalMeterJpaRepository;

  @Override
  public List<PhysicalMeter> findByMedium(String medium) {
    return physicalMeterJpaRepository.findByMedium(medium).stream()
      .map(PhysicalMeterEntityMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public List<PhysicalMeter> findAll() {
    return physicalMeterJpaRepository.findAll().stream()
      .map(PhysicalMeterEntityMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  @CacheEvict(
    cacheNames = {
      "physicalMeter.organisationIdExternalIdAddress",
      "physicalMeter.organisationIdExternalIdAddress.withStatuses"
    },
    key = "#physicalMeter.organisationId + #physicalMeter.externalId + #physicalMeter.address"
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
    cacheNames = "physicalMeter.organisationIdExternalIdAddress.withStatuses",
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
  public List<PhysicalMeter> findBy(
    UUID organisationId,
    String externalId
  ) {
    return physicalMeterJpaRepository.findByOrganisationIdAndExternalId(
      organisationId,
      externalId
    ).stream().map(PhysicalMeterEntityMapper::toDomainModelWithoutStatusLogs).collect(toList());
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
