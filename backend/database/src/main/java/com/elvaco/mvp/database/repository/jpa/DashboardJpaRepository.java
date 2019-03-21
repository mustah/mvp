package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.database.entity.dashboard.DashboardEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DashboardJpaRepository extends JpaRepository<DashboardEntity, UUID> {

  Optional<DashboardEntity> findByIdAndOwnerUserIdAndOrganisationId(
    UUID dashboardId, UUID ownerUserId, UUID organisationId
  );

  List<DashboardEntity> findByOwnerUserIdAndOrganisationId(
    UUID ownerUserId, UUID organisationId
  );
}
