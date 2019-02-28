package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.DoubleStream;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.testing.fixture.UserTestData;
import com.elvaco.mvp.web.dto.DashboardDto;
import com.elvaco.mvp.web.dto.WidgetDto;
import com.elvaco.mvp.web.dto.WidgetType;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DEFAULT_GAS;
import static com.elvaco.mvp.core.spi.data.RequestParameter.AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.BEFORE;
import static com.elvaco.mvp.core.spi.data.RequestParameter.CITY;
import static com.elvaco.mvp.core.util.Json.toJsonNode;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.ELVACO;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.MARVEL;
import static com.elvaco.mvp.testing.fixture.UserSelectionTestData.CITIES_JSON_STRING;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ALL")
public class DashboardControllerTest extends IntegrationTest {

  @After
  public void tearDown() {
    measurementJpaRepository.deleteAll();
  }

  @Test
  public void collectionStatusNoPeriod_ReturnsEmptyCollectionStatus() {
    ResponseEntity<DashboardDto> response = asUser()
      .get("/dashboards/current", DashboardDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    WidgetDto widget = response.getBody().widgets.get(0);
    assertThat(widget).isEqualTo(
      new WidgetDto(
        WidgetType.COLLECTION.name,
        Double.NaN
      )
    );
  }

  @Test
  public void collectionStatusNoMeters_ReturnsEmptyCollectionStatus() {
    ZonedDateTime start = context().now();
    ZonedDateTime end = start.plusHours(2);
    ResponseEntity<DashboardDto> response = asUser()
      .get(Url.builder()
        .path("/dashboards/current")
        .period(start, end)
        .build(), DashboardDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    WidgetDto widget = response.getBody().widgets.get(0);
    assertThat(widget).isEqualTo(
      new WidgetDto(
        WidgetType.COLLECTION.name,
        Double.NaN
      )
    );
  }

  @Ignore
  @Test
  public void findCollectionStatsEnsureOrganisationFiltersAreApplied() {
    UserSelection selection = UserSelection.builder()
      .selectionParameters(toJsonNode(CITIES_JSON_STRING))
      .organisationId(MARVEL.id)
      .build();

    Organisation organisation = MARVEL.toBuilder()
      .selection(selection)
      .parent(ELVACO)
      .build();

    User user = UserTestData.subOrgUser().organisation(organisation).build();

    var url = Url.builder()
      .path("/dashboards/current")
      .parameter(CITY, "norge,oslo")
      .parameter(BEFORE, "2018-01-01T00:00:00Z")
      .parameter(AFTER, "2018-12-31T00:00:00Z")
      .build();

    var response = as(user).get(url, DashboardDto.class);

    assertThat(response.getBody().widgets).isEmpty();
  }

  @Test
  public void findAllWithCollectionStatusForMediumGas() {
    var meter = given(logicalMeter().meterDefinition(DEFAULT_GAS));
    ZonedDateTime start = context().now();
    ZonedDateTime end = start.plusDays(1);
    given(series(
      meter,
      Quantity.VOLUME,
      start,
      DoubleStream.iterate(0, d -> d + 1.0).limit(12).toArray()
    ));

    ResponseEntity<DashboardDto> response = asUser()
      .get(
        Url.builder()
          .path("/dashboards/current")
          .period(start, end)
          .parameter("medium", "Gas")
          .build(),
        DashboardDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().widgets.get(0))
      .isEqualTo(new WidgetDto(
        WidgetType.COLLECTION.name,
        50
      ));
  }

  @Test
  public void findAllWithCollectionStatus() {
    ZonedDateTime start = context().now();
    ZonedDateTime end = start.plusDays(2);

    PhysicalMeter phys24 = physicalMeter().readIntervalMinutes(1440).build();
    List<LogicalMeter> meters = new ArrayList<>(given(
      logicalMeter(),
      logicalMeter().physicalMeters((List<PhysicalMeter>) Arrays.asList(phys24))
    ));
    //36 out of 48  (75%)
    given(series(
      meters.get(0),
      Quantity.POWER,
      start,
      DoubleStream.iterate(0, d -> d + 1.0).limit(36).toArray()

    ));
    //One out of two (50%)
    given(series(
      meters.get(1),
      Quantity.POWER,
      start,
      DoubleStream.iterate(0, d -> d + 1.0).limit(1).toArray()

    ));

    ResponseEntity<DashboardDto> response = asUser()
      .get(
        Url.builder().path("/dashboards/current")
          .period(start, end).build(),
        DashboardDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    DashboardDto dashboardDtos = response.getBody();

    assertThat(dashboardDtos.widgets.get(0))
      .isEqualTo(new WidgetDto(
        WidgetType.COLLECTION.name,
        62.5
      ));
  }
}
