package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.database.entity.dashboard.WidgetEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WidgetJpaRepository extends JpaRepository<WidgetEntity, UUID> {

  List<WidgetEntity> findByDashboardIdAndOwnerUserIdAndOrganisationId(
    UUID dashboardId,
    UUID ownerUserId,
    UUID organisationId
  );

  Optional<WidgetEntity> findByIdAndOwnerUserIdAndOrganisationId(
    UUID widgetId, UUID ownerUserId, UUID organisationId
  );
}
