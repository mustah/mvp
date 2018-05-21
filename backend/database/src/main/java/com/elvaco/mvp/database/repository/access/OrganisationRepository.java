package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.mappers.OrganisationEntityMapper;
import lombok.RequiredArgsConstructor;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class OrganisationRepository implements Organisations {

  private final OrganisationJpaRepository organisationJpaRepository;

  @Override
  public List<Organisation> findAll() {
    return organisationJpaRepository.findAll()
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
  public Organisation save(Organisation organisation) {
    OrganisationEntity entity = OrganisationEntityMapper.toEntity(organisation);
    return OrganisationEntityMapper.toDomainModel(organisationJpaRepository.save(entity));
  }

  @Override
  public void deleteById(UUID id) {
    organisationJpaRepository.delete(id);
  }

  @Override
  public Optional<Organisation> findBySlug(String slug) {
    return organisationJpaRepository.findBySlug(slug)
      .map(OrganisationEntityMapper::toDomainModel);
  }

  @Override
  public Optional<Organisation> findByExternalId(String externalId) {
    return organisationJpaRepository.findByExternalId(externalId)
      .map(OrganisationEntityMapper::toDomainModel);
  }
}
