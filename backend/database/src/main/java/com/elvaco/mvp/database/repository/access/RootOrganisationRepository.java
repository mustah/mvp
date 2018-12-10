package com.elvaco.mvp.database.repository.access;

import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.mappers.OrganisationEntityMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

import static com.elvaco.mvp.database.repository.mappers.OrganisationEntityMapper.toDomainModel;
import static com.elvaco.mvp.database.repository.mappers.OrganisationEntityMapper.toEntity;

@RequiredArgsConstructor
public class RootOrganisationRepository {

  private final OrganisationJpaRepository organisationJpaRepository;

  @Caching(
    evict = {
      @CacheEvict(cacheNames = "organisation.slug", key = "#result.slug"),
      @CacheEvict(cacheNames = "organisation.externalId", key = "#result.externalId"),
    }
  )
  public Organisation save(Organisation organisation) {
    return toDomainModel(organisationJpaRepository.save(toEntity(organisation)));
  }

  public Optional<Organisation> findBySlug(String slug) {
    return organisationJpaRepository.findBySlug(slug)
      .map(OrganisationEntityMapper::toDomainModel);
  }
}
