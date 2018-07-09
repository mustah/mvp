package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.mappers.OrganisationEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import static com.elvaco.mvp.database.repository.mappers.OrganisationEntityMapper.toDomainModel;
import static com.elvaco.mvp.database.repository.mappers.OrganisationEntityMapper.toEntity;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class OrganisationRepository implements Organisations {

  private final OrganisationJpaRepository organisationJpaRepository;

  @Override
  public List<Organisation> findAll() {
    return organisationJpaRepository.findAllByOrderByNameAsc()
      .stream()
      .map(OrganisationEntityMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public Optional<Organisation> findById(UUID id) {
    return Optional.ofNullable(organisationJpaRepository.findOne(id))
      .map(OrganisationEntityMapper::toDomainModel);
  }

  @Override
  @Caching(
    evict = {
      @CacheEvict(cacheNames = "organisation.slug", key = "#result.slug"),
      @CacheEvict(cacheNames = "organisation.externalId", key = "#result.externalId"),
    }
  )
  public Organisation save(Organisation organisation) {
    return toDomainModel(organisationJpaRepository.save(toEntity(organisation)));
  }

  @Override
  @Caching(
    evict = {
      @CacheEvict(cacheNames = "organisation.slug", allEntries = true),
      @CacheEvict(cacheNames = "organisation.externalId", allEntries = true),
    }
  )
  public void deleteById(UUID id) {
    organisationJpaRepository.delete(id);
  }

  @Override
  @Cacheable(cacheNames = "organisation.slug")
  public Optional<Organisation> findBySlug(String slug) {
    return organisationJpaRepository.findBySlug(slug)
      .map(OrganisationEntityMapper::toDomainModel);
  }

  @Override
  @Cacheable(cacheNames = "organisation.externalId")
  public Optional<Organisation> findByExternalId(String externalId) {
    return organisationJpaRepository.findByExternalId(externalId)
      .map(OrganisationEntityMapper::toDomainModel);
  }
}
