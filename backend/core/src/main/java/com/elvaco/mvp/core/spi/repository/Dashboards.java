package com.elvaco.mvp.core.spi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Dashboard;

public interface Dashboards {

  List<Dashboard> findAll();

  Optional<Dashboard> findByIdAndOwnerUserIdAndOrganisationId(
    UUID id,
    UUID ownerUserId,
    UUID organisationId
  );

  List<Dashboard> findByOwnerUserIdAndOrganisationId(UUID ownerUserId, UUID organisationId);

  Dashboard save(Dashboard dashboard);

  void deleteById(UUID id);
}
