package com.elvaco.mvp.core.spi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;

public interface Organisations {

  List<Organisation> findAll();

  List<Organisation> findOrganisationAndSubOrganisations(UUID organisationId);

  List<Organisation> findAllSubOrganisations(UUID organisationId);

  Page<Organisation> findAllMainOrganisations(RequestParameters parameters, Pageable pageable);

  Optional<Organisation> findById(UUID id);

  Organisation save(Organisation organisation);

  void deleteById(UUID id);

  Optional<Organisation> findBySlug(String slug);

  Optional<Organisation> findByExternalId(String externalId);
}
