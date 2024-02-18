package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.mappers.PhysicalMeterEntityMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;

import static com.elvaco.mvp.database.repository.mappers.PhysicalMeterEntityMapper.toDomainModel;
import static com.elvaco.mvp.database.repository.mappers.PhysicalMeterEntityMapper.toEntity;
import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
public class PhysicalMetersRepository implements PhysicalMeters {

  private final PhysicalMeterJpaRepository physicalMeterJpaRepository;
  private final EntityManager entityManager;

  @Override
  public List<PhysicalMeter> findByMedium(String medium) {
    return physicalMeterJpaRepository.findByMedium(medium).stream()
      .map(PhysicalMeterEntityMapper::toDomainModel)
      .toList();
  }

  @Override
  public List<PhysicalMeter> findAll() {
    return physicalMeterJpaRepository.findAll().stream()
      .map(PhysicalMeterEntityMapper::toDomainModel)
      .toList();
  }

  @Override
  @Caching(evict = {
    @CacheEvict(
      cacheNames = {
        "physicalMeter.organisationIdExternalIdAddress",
        "physicalMeter.organisationIdExternalIdAddress.withStatuses"
      },
      key = "#physicalMeter.organisationId + #physicalMeter.externalId + #physicalMeter.address"
    ),
    @CacheEvict(
      cacheNames = {
        "logicalMeter.organisationIdExternalId"
      },
      key = "#physicalMeter.organisationId + #physicalMeter.externalId"
    ),
    @CacheEvict(
      cacheNames = {
        "gateway.organisationIdSerial"
      },
      allEntries = true
    )
  })
  public PhysicalMeter save(PhysicalMeter physicalMeter) {
    try {
      return toDomainModel(physicalMeterJpaRepository.save(toEntity(physicalMeter)));
    } catch (DataIntegrityViolationException ex) {
      log.warn("Constraint violation: ", ex);
      return physicalMeter;
    }
  }

  @Override
  @Caching(evict = {
    @CacheEvict(
      cacheNames = {
        "physicalMeter.organisationIdExternalIdAddress",
        "physicalMeter.organisationIdExternalIdAddress.withStatuses"
      },
      key = "#physicalMeter.organisationId + #physicalMeter.externalId + #physicalMeter.address"
    ),
    @CacheEvict(
      cacheNames = {
        "logicalMeter.organisationIdExternalId"
      },
      key = "#physicalMeter.organisationId + #physicalMeter.externalId"
    ),
    @CacheEvict(
      cacheNames = {
        "gateway.organisationIdSerial"
      },
      allEntries = true
    )
  })
  public PhysicalMeter saveAndFlush(PhysicalMeter physicalMeter) {
    try {
      PhysicalMeterEntity entity = physicalMeterJpaRepository.save(toEntity(physicalMeter));
      entityManager.flush();
      return toDomainModel(entity);
    } catch (DataIntegrityViolationException ex) {
      log.warn("Constraint violation: ", ex);
      return physicalMeter;
    }
  }

  @Override
  @Cacheable(
    cacheNames = "physicalMeter.organisationIdExternalIdAddress.withStatuses",
    key = "#organisationId + #externalId + #address",
    unless = "#result==null"
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
  public List<PhysicalMeter> findBy(UUID organisationId, String externalId) {
    return physicalMeterJpaRepository.findByOrganisationIdAndExternalId(
      organisationId,
      externalId
    ).stream().map(PhysicalMeterEntityMapper::toDomainModelWithoutStatusLogs).toList();
  }

  @Override
  @Cacheable(
    cacheNames = "physicalMeter.organisationIdExternalIdAddress",
    key = "#organisationId + #externalId + #address",
    unless = "#result==null"
  )
  public Optional<PhysicalMeter> findBy(UUID organisationId, String externalId, String address) {
    return physicalMeterJpaRepository.findByOrganisationIdAndExternalIdAndAddress(
      organisationId,
      externalId,
      address
    ).map(PhysicalMeterEntityMapper::toDomainModelWithoutStatusLogs);
  }
}
