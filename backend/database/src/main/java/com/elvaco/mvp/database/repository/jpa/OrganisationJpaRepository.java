package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrganisationJpaRepository {

  <S extends OrganisationEntity> S save(S entity);

  void delete(OrganisationEntity entity);

  void deleteAll();

  void deleteById(UUID id);

  Optional<OrganisationEntity> findBySlug(String slug);

  Optional<OrganisationEntity> findById(UUID id);

  Optional<OrganisationEntity> findByExternalId(String externalId);

  List<OrganisationEntity> findAllByOrderByNameAsc();

  List<OrganisationEntity> findOrganisationAndSubOrganisations(UUID organisationId);

  List<OrganisationEntity> findAllSubOrganisations(UUID organisationId);

  Page<Organisation> findAllMainOrganisations(RequestParameters parameters, Pageable pageable);

  List<OrganisationEntity> findAll();
}
