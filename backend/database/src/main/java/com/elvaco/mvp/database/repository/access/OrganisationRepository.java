package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.adapters.spring.PageAdapter;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.mappers.OrganisationEntityMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static com.elvaco.mvp.database.repository.mappers.OrganisationEntityMapper.toDomainModel;
import static com.elvaco.mvp.database.repository.mappers.OrganisationEntityMapper.toEntity;
import static com.elvaco.mvp.database.repository.queryfilters.SortUtil.getSortOrUnsorted;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class OrganisationRepository implements Organisations {

  private final OrganisationJpaRepository organisationJpaRepository;
  private final EntityManager entityManager;

  @Override
  public List<Organisation> findAll() {
    return organisationJpaRepository.findAllByOrderByNameAsc().stream()
      .map(OrganisationEntityMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public List<Organisation> findOrganisationAndSubOrganisations(UUID organisationId) {
    return organisationJpaRepository.findOrganisationAndSubOrganisations(organisationId).stream()
      .map(OrganisationEntityMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public List<Organisation> findAllSubOrganisations(UUID organisationId) {
    return organisationJpaRepository.findAllSubOrganisations(organisationId).stream()
      .map(OrganisationEntityMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public Page<Organisation> findAllMainOrganisations(
    RequestParameters parameters,
    Pageable pageable
  ) {
    return new PageAdapter<>(
      organisationJpaRepository.findAllMainOrganisations(
        parameters,
        PageRequest.of(
          pageable.getPageNumber(),
          pageable.getPageSize(),
          getSortOrUnsorted(parameters)
        )
      )
    );
  }

  @Override
  public Optional<Organisation> findById(UUID id) {
    return organisationJpaRepository.findById(id)
      .map(OrganisationEntityMapper::toDomainModel);
  }

  @Override
  @Caching(
    evict = {
      @CacheEvict(cacheNames = "organisation.slug", key = "#result.slug"),
      @CacheEvict(cacheNames = "organisation.externalId", key = "#result.externalId"),
    }
  )
  @Transactional
  public Organisation saveAndFlush(Organisation organisation) {
    var org = organisationJpaRepository.save(toEntity(organisation));
    entityManager.flush();
    return toDomainModel(org);
  }

  @Override
  @Caching(
    evict = {
      @CacheEvict(cacheNames = "organisation.slug", allEntries = true),
      @CacheEvict(cacheNames = "organisation.externalId", allEntries = true),
    }
  )
  public void deleteById(UUID id) {
    organisationJpaRepository.deleteById(id);
  }

  @Override
  @Cacheable(
    cacheNames = "organisation.slug",
    unless = "#result==null"
  )
  public Optional<Organisation> findBySlug(String slug) {
    return organisationJpaRepository.findBySlug(slug)
      .map(OrganisationEntityMapper::toDomainModel);
  }

  @Override
  @Cacheable(
    cacheNames = "organisation.externalId",
    unless = "#result==null"
  )
  public Optional<Organisation> findByExternalId(String externalId) {
    return organisationJpaRepository.findByExternalId(externalId)
      .map(OrganisationEntityMapper::toDomainModel);
  }
}
