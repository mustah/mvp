package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Dashboard;
import com.elvaco.mvp.core.domainmodels.Widget;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.repository.Dashboards;
import com.elvaco.mvp.core.spi.repository.Widgets;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DashboardUseCases {

  private final AuthenticatedUser currentUser;
  private final Dashboards dashboards;
  private final Widgets widgets;

  public List<Dashboard> findDashboardsForCurrentUser() {
    return dashboards.findByOwnerUserIdAndOrganisationId(
      currentUser.getUserId(),
      currentUser.getOrganisationId()
    );
  }

  public Optional<Dashboard> findDashboardByIdForCurrentUser(UUID dashboardId) {
    return dashboards.findByIdAndOwnerUserIdAndOrganisationId(
      dashboardId,
      currentUser.getUserId(),
      currentUser.getOrganisationId()
    );
  }

  public List<Widget> findWidgetsForCurrentUserAndDashboard(UUID dashboardId) {
    return widgets.findByDashboardIdAndOwnerUserIdAndOrganisationId(
      dashboardId,
      currentUser.getUserId(),
      currentUser.getOrganisationId()
    );
  }

  public Optional<Widget> findWidgetForCurrentUser(UUID widgetId) {
    return widgets.findByIdAndOwnerUserIdAndOrganisationId(
      widgetId, currentUser.getUserId(), currentUser.getOrganisationId()
    );
  }

  public Widget save(Widget widget) {
    if (hasPermission(widget)) {
      return widgets.save(widget);
    } else {
      throw new Unauthorized("User '" + currentUser.getUsername()
        + "' is not allowed to save " + widget);
    }
  }

  public Dashboard save(Dashboard dashboard) {
    if (isCurrentUser(dashboard.ownerUserId, dashboard.organisationId)) {
      validateWidgets(dashboard);
      Dashboard saved = dashboards.save(dashboard);
      dashboard.widgets.forEach(widgets::save);
      return saved;
    } else {
      throw new Unauthorized("User '" + currentUser.getUsername()
        + "' is not allowed to save " + dashboard);
    }
  }

  public Dashboard update(Dashboard dashboard) {
    if (hasPermission(dashboard)) {
      return dashboards.save(dashboard);
    } else {
      throw new Unauthorized("User '" + currentUser.getUsername()
        + "' is not allowed to update " + dashboard);
    }
  }

  public Optional<Dashboard> deleteDashboard(UUID dashboardId) {
    return findDashboardByIdForCurrentUser(dashboardId)
      .map(d -> {
        dashboards.deleteById(d.id);
        return d;
      });
  }

  public Optional<Widget> deleteWidget(UUID widgetId) {
    return findWidgetForCurrentUser(widgetId)
      .map(w -> {
        widgets.deleteById(w.id);
        return w;
      });
  }

  private void validateWidgets(Dashboard dashboard) {
    if (dashboard.widgets.stream()
      .filter(w -> !w.dashboardId.equals(dashboard.id)
        || !w.ownerUserId.equals(dashboard.ownerUserId)
        || !w.organisationId.equals(dashboard.organisationId))
      .findFirst().isPresent()) {
      throw new Unauthorized("Invalid dashboard configuration " + dashboard);
    }
  }

  private boolean isCurrentUser(UUID ownerUserId, UUID organisationId) {
    return currentUser.getUserId().equals(ownerUserId) && currentUser.isWithinOrganisation(
      organisationId);
  }

  private boolean hasPermission(Dashboard dashboard) {
    return currentUser.getUserId().equals(dashboard.ownerUserId)
      && currentUser.isWithinOrganisation(dashboard.organisationId)
      && findDashboardByIdForCurrentUser(dashboard.id).isPresent();
  }

  private boolean hasPermission(Widget widget) {
    return currentUser.getUserId().equals(widget.ownerUserId)
      && currentUser.isWithinOrganisation(widget.organisationId)
      && findDashboardByIdForCurrentUser(widget.dashboardId).isPresent();
  }
}
