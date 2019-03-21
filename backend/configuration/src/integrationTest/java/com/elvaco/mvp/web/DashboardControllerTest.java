package com.elvaco.mvp.web;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Dashboard;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.domainmodels.Widget;
import com.elvaco.mvp.core.util.Json;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.web.dto.DashboardDto;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.WidgetDto;
import com.elvaco.mvp.web.dto.WidgetType;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class DashboardControllerTest extends IntegrationTest {

  @Test
  public void get_dashboards() {
    User user = given(user());
    User otherUser = given(user());
    Dashboard myDashboard = given(dashboard().ownerUserId(user.id));

    given(dashboard().ownerUserId(otherUser.id));

    List<DashboardDto> response = as(user).getList(
      dashboardsUrl(),
      DashboardDto.class
    ).getBody();

    assertThat(response).hasSize(1)
      .extracting(d -> d.id)
      .containsExactly(myDashboard.id);
  }

  @Test
  public void get_widgets() {
    User user = given(user());
    Dashboard myDashboard1 = given(dashboard().ownerUserId(user.id));
    Widget widget1 = given(widget().dashboardId(myDashboard1.id).ownerUserId(user.id));
    Widget widget2 = given(widget().dashboardId(myDashboard1.id).ownerUserId(user.id));

    Dashboard myDashboard2 = given(dashboard().ownerUserId(user.id));
    given(widget().dashboardId(myDashboard2.id).ownerUserId(user.id));

    User anotherUser = given(user());
    Dashboard anotherUsersDashboard = given(dashboard().ownerUserId(anotherUser.id));
    given(widget().dashboardId(anotherUsersDashboard.id).ownerUserId(anotherUser.id));

    List<WidgetDto> response = as(user).getList(
      widgetsForDashboardUrl(myDashboard1.id),
      WidgetDto.class
    ).getBody();

    assertThat(response).hasSize(2)
      .extracting(w -> w.id)
      .containsExactly(widget1.id, widget2.id);
  }

  @Test
  public void get_dashboards_noDashboardFound() {
    List<DashboardDto> response = asUser().getList(
      dashboardsUrl(),
      DashboardDto.class
    ).getBody();

    assertThat(response).hasSize(0);
  }

  @Test
  public void add_dashboard() {
    User user = given(user());
    DashboardDto dto = new DashboardDto(randomUUID(), randomName(), randomLayout(), emptyList());

    ResponseEntity<DashboardDto> response = as(user).post(
      dashboardsUrl(),
      dto,
      DashboardDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isEqualTo(dto);

    assertThat(dashboardJpaRepository.findById(dto.id)).isPresent().get()
      .extracting(d -> d.ownerUserId, d -> d.organisationId, d -> d.layout.getJson())
      .containsExactly(user.id, user.organisation.id, dto.layout);
  }

  @Test
  public void add_dashboard_handleNullWidgets() {
    User user = given(user());
    DashboardDto dto = new DashboardDto(randomUUID(), randomName(), randomLayout(), null);

    ResponseEntity<DashboardDto> response = as(user).post(
      dashboardsUrl(),
      dto,
      DashboardDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isEqualToIgnoringNullFields(dto);

    assertThat(dashboardJpaRepository.findById(dto.id)).isPresent().get()
      .extracting(d -> d.ownerUserId, d -> d.organisationId, d -> d.layout.getJson())
      .containsExactly(user.id, user.organisation.id, dto.layout);
  }

  @Test
  public void add_dashboard_saves_widgets() {
    User user = given(user());
    UUID dashboardId = randomUUID();
    WidgetDto widget1 = new WidgetDto(
      randomUUID(),
      dashboardId,
      WidgetType.COLLECTION.toString(),
      randomName(),
      randomSettings()
    );

    WidgetDto widget2 = new WidgetDto(
      randomUUID(),
      dashboardId,
      WidgetType.COLLECTION.toString(),
      randomName(),
      randomSettings()
    );

    DashboardDto dto = new DashboardDto(
      dashboardId,
      randomName(),
      randomLayout(),
      asList(widget1, widget2)
    );

    ResponseEntity<DashboardDto> response = as(user).post(
      dashboardsUrl(),
      dto,
      DashboardDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isEqualToIgnoringGivenFields(dto, "widgets");

    assertThat(widgetJpaRepository.findByDashboardIdAndOwnerUserIdAndOrganisationId(
      dashboardId,
      user.id,
      user.organisation.id
    )).extracting(w -> w.id)
      .containsExactlyInAnyOrder(widget1.id, widget2.id);
  }

  @Test
  public void add_widget() {
    User user = given(user());
    Dashboard dashboard = given(dashboard().ownerUserId(user.id));
    WidgetDto dto = new WidgetDto(
      randomUUID(),
      dashboard.id,
      WidgetType.COLLECTION.toString(),
      randomName(),
      randomSettings()
    );

    ResponseEntity<WidgetDto> response = as(user).post(
      widgetsUrl(),
      dto,
      WidgetDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isEqualTo(dto);
    assertThat(widgetJpaRepository.findById(dto.id).get())
      .extracting(w -> w.dashboardId, w -> w.organisationId, w -> w.settings.getJson())
      .containsExactly(dashboard.id, user.organisation.id, dto.settings);
  }

  @Test
  public void add_widget_DashboardDoesNotExist() {
    WidgetDto dto = new WidgetDto(
      randomUUID(),
      randomUUID(),
      WidgetType.COLLECTION.toString(),
      randomName(),
      randomSettings()
    );
    ResponseEntity<ErrorMessageDto> response = asUser().post(
      widgetsUrl(),
      dto,
      ErrorMessageDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getBody().message)
      .contains("not allowed to save Widget")
      .contains(dto.id.toString());
  }

  @Test
  public void add_widget_notMyDashboard() {
    User user = given(user());
    User otherUser = given(user());
    Dashboard dashboard = given(dashboard().ownerUserId(otherUser.id));

    WidgetDto dto = new WidgetDto(
      randomUUID(),
      dashboard.id,
      WidgetType.COLLECTION.toString(),
      randomName(),
      randomSettings()
    );

    ResponseEntity<ErrorMessageDto> response = as(user).post(
      widgetsUrl(),
      dto,
      ErrorMessageDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getBody().message)
      .contains("not allowed to save Widget")
      .contains(dto.dashboardId.toString());
  }

  @Test
  public void delete_dashboard() {
    User user = given(user());
    Dashboard dashboard = given(dashboard().ownerUserId(user.id));
    given(widget().dashboardId(dashboard.id).ownerUserId(user.id));

    ResponseEntity<DashboardDto> response = as(user).delete(
      dashboardsUrl(dashboard.id),
      DashboardDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).extracting(d -> d.id).isEqualTo(dashboard.id);

    assertThat(dashboards.findAll()).hasSize(0);
    assertThat(widgets.findAll()).hasSize(0);
  }

  @Test
  public void delete_dashboard_failForAnotherUser() {
    User user = given(user());
    User otherUser = given(user());
    Dashboard dashboard = given(dashboard().ownerUserId(otherUser.id));

    ResponseEntity<ErrorMessageDto> response = as(user).delete(
      dashboardsUrl(dashboard.id),
      ErrorMessageDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody().message).contains("dashboard with id '" + dashboard.id);
  }

  @Test
  public void delete_widget() {
    User user = given(user());
    Dashboard dashboard = given(dashboard().ownerUserId(user.id));
    Widget widget1 = given(widget().dashboardId(dashboard.id).ownerUserId(user.id));
    Widget widget2 = given(widget().dashboardId(dashboard.id).ownerUserId(user.id));

    ResponseEntity<WidgetDto> response = as(user).delete(
      widgetsUrl(widget2.id),
      WidgetDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).extracting(w -> w.id).isEqualTo(widget2.id);

    List<WidgetDto> getResponse = as(user).getList(
      widgetsForDashboardUrl(dashboard.id),
      WidgetDto.class
    ).getBody();

    assertThat(getResponse).hasSize(1).extracting(w -> w.id).containsExactly(widget1.id);
    assertThat(widgets.findByDashboardIdAndOwnerUserIdAndOrganisationId(
      dashboard.id,
      user.id,
      user.organisation.id
    )).extracting(w -> w.id).containsExactly(widget1.id);
  }

  @Test
  public void delete_widget_failForAnotherUser() {
    User user = given(user());
    Dashboard dashboard = given(dashboard().ownerUserId(given(user()).id));
    Widget widget = given(widget().dashboardId(dashboard.id).ownerUserId(dashboard.ownerUserId));

    ResponseEntity<ErrorMessageDto> response = as(user).delete(
      widgetsUrl(widget.id),
      ErrorMessageDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody().message).contains("widget with id '" + widget.id);
  }

  @Test
  public void update_dashboard() {
    User user = given(user());
    Dashboard dashboard = given(dashboard().ownerUserId(user.id));

    DashboardDto dto = new DashboardDto(dashboard.id, randomName(), randomLayout(), null);

    ResponseEntity<DashboardDto> response = as(user).put(
      dashboardsUrl(),
      dto,
      DashboardDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(dashboardJpaRepository.findAll())
      .extracting(d -> d.ownerUserId, d -> d.organisationId, d -> d.name, d -> d.layout.getJson())
      .containsExactly(tuple(user.id, user.organisation.id, dto.name, dto.layout));
  }

  @Test
  public void update_dashboard_doesNotUpdateWidgets() {
    User user = given(user());
    Dashboard dashboard = given(dashboard().ownerUserId(user.id));
    Widget widget = given(widget().dashboardId(dashboard.id).ownerUserId(user.id));

    DashboardDto dto = new DashboardDto(dashboard.id, randomName(), randomLayout(), null);

    ResponseEntity<DashboardDto> response = as(user).put(
      dashboardsUrl(),
      dto,
      DashboardDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(widgetJpaRepository.findById(widget.id)).isPresent().get()
      .extracting(w -> w.organisationId, w -> w.ownerUserId, w -> w.settings.getJson())
      .containsExactly(user.organisation.id, user.id, widget.settings);

    dto = new DashboardDto(dashboard.id, randomName(), randomLayout(), emptyList());

    response = as(user).put(
      dashboardsUrl(),
      dto,
      DashboardDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(widgetJpaRepository.findById(widget.id)).isPresent().get()
      .extracting(w -> w.organisationId, w -> w.ownerUserId, w -> w.settings.getJson())
      .containsExactly(user.organisation.id, user.id, widget.settings);
  }

  @Test
  public void update_dashboard_failForAnotherUser() {
    User user = given(user());
    User otherUser = given(user());
    Dashboard dashboard = given(dashboard().ownerUserId(otherUser.id));

    DashboardDto dto = new DashboardDto(dashboard.id, randomName(), randomLayout(), null);

    ResponseEntity<ErrorMessageDto> response = as(user).put(
      dashboardsUrl(),
      dto,
      ErrorMessageDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getBody().message)
      .contains("not allowed to update")
      .contains(dashboard.id.toString());
  }

  @Test
  public void update_widget() {
    User user = given(user());
    Dashboard dashboard = given(dashboard().ownerUserId(user.id));
    Widget widget = given(widget().dashboardId(dashboard.id).ownerUserId(user.id));

    WidgetDto dto = new WidgetDto(
      widget.id,
      dashboard.id,
      WidgetType.MAP.toString(),
      randomName(),
      randomSettings()
    );

    ResponseEntity<WidgetDto> response = as(user).put(
      widgetsUrl(),
      dto,
      WidgetDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(widgetJpaRepository.findAll())
      .extracting(
        w -> w.id,
        w -> w.ownerUserId,
        w -> w.organisationId,
        w -> w.type.toString(),
        w -> w.title,
        w -> w.settings.getJson()
      )
      .containsExactly(tuple(
        widget.id,
        user.id,
        user.organisation.id,
        WidgetType.MAP.toString(),
        dto.title,
        dto.settings
      ));
  }

  @Test
  public void update_widget_failForAnotherUser() {
    User user = given(user());
    Dashboard dashboard = given(dashboard().ownerUserId(given(user()).id));
    Widget widget = given(widget().dashboardId(dashboard.id).ownerUserId(dashboard.ownerUserId));

    WidgetDto dto = new WidgetDto(
      widget.id,
      dashboard.id,
      WidgetType.COLLECTION.toString(),
      randomName(),
      randomSettings()
    );

    ResponseEntity<ErrorMessageDto> response = as(user).put(
      widgetsUrl(),
      dto,
      ErrorMessageDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getBody().message)
      .contains("not allowed to save Widget")
      .contains(widget.id.toString());
  }

  private String randomName() {
    return randomUUID().toString();
  }

  private ObjectNode randomLayout() {
    try {
      return (ObjectNode) Json.OBJECT_MAPPER.readTree(
        "{\"layout\":\"test layout " + randomUUID() + "\"}");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private ObjectNode randomSettings() {
    try {
      return (ObjectNode) Json.OBJECT_MAPPER.readTree(
        "{\"settings\":\"test settings " + randomUUID() + "\"}");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Url dashboardsUrl() {
    return Url.builder().path("/dashboards").build();
  }

  private static Url dashboardsUrl(UUID dashboardId) {
    return Url.builder().path("/dashboards/" + dashboardId).build();
  }

  private static Url widgetsUrl() {
    return Url.builder().path("/dashboards/widgets").build();
  }

  private static Url widgetsUrl(UUID widgetId) {
    return Url.builder()
      .path("/dashboards/widgets/" + widgetId)
      .build();
  }

  private static Url widgetsForDashboardUrl(UUID dashboardId) {
    return Url.builder()
      .path("/dashboards/widgets")
      .parameter("dashboardId", dashboardId)
      .build();
  }
}
