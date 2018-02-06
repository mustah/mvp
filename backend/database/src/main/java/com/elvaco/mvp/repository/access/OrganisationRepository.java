package com.elvaco.mvp.repository.access;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.usecase.Organisations;
import com.elvaco.mvp.repository.jpa.OrganisationJpaRepository;

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
    return organisationMapper.toDomainModel(organisationJpaRepository.save(organisationMapper
      .toEntity(organisation)));
  }

  @Override
  public Organisation update(Organisation organisation) {
    return organisationMapper.toDomainModel(organisationJpaRepository.save(organisationMapper
      .toEntity(organisation)));
  }

  @Override
  public void deleteById(Long id) {
    organisationJpaRepository.delete(id);
  }

}
