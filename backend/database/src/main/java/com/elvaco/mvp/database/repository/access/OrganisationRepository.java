package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.mappers.OrganisationMapper;

import static java.util.stream.Collectors.toList;

public class OrganisationRepository implements Organisations {

  private final OrganisationJpaRepository organisationJpaRepository;
  private final OrganisationMapper organisationMapper;

  public OrganisationRepository(
    OrganisationJpaRepository organisationJpaRepository,
    OrganisationMapper organisationMapper
  ) {
    this.organisationJpaRepository = organisationJpaRepository;
    this.organisationMapper = organisationMapper;
  }

  @Override
  public List<Organisation> findAll() {
    return organisationJpaRepository.findAll()
      .stream()
      .map(organisationMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public Optional<Organisation> findById(Long id) {
    return Optional.ofNullable(organisationJpaRepository.findOne(id))
      .map(organisationMapper::toDomainModel);
  }

  @Override
  public Organisation create(Organisation organisation) {
    OrganisationEntity entity = organisationMapper.toEntity(organisation);
    return organisationMapper.toDomainModel(organisationJpaRepository.save(entity));
  }

  @Override
  public Organisation update(Organisation organisation) {
    OrganisationEntity entity = organisationMapper.toEntity(organisation);
    return organisationMapper.toDomainModel(organisationJpaRepository.save(entity));
  }

  @Override
  public void deleteById(Long id) {
    organisationJpaRepository.delete(id);
  }
}
