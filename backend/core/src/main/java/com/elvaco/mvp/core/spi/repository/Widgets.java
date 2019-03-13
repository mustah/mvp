package com.elvaco.mvp.core.spi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Widget;

public interface Widgets {

  List<Widget> findAll();

  List<Widget> findByDashboardIdAndOwnerUserIdAndOrganisationId(
    UUID dashboardId,
    UUID ownerUserId,
    UUID organisationId
  );

  Optional<Widget> findByIdAndOwnerUserIdAndOrganisationId(
    UUID widgetId,
    UUID ownerUserId,
    UUID organisationId
  );

  Widget save(Widget widget);

  void deleteById(UUID id);
}
