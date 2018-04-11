package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.database.entity.gateway.GatewayStatusLogEntity;
import com.elvaco.mvp.database.repository.jpa.GatewayJpaRepository;
import com.elvaco.mvp.database.repository.jpa.GatewayStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.GatewayDto;
import com.elvaco.mvp.web.dto.LocationDto;
import com.elvaco.mvp.web.dto.MapMarkerDto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.domainmodels.StatusType.OK;
import static com.elvaco.mvp.core.domainmodels.StatusType.WARNING;
import static com.elvaco.mvp.testing.fixture.UserTestData.DAILY_PLANET;
import static com.elvaco.mvp.testing.fixture.UserTestData.dailyPlanetUser;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("rawtypes")
public class GatewayControllerTest extends IntegrationTest {

  @Autowired
  private GatewayJpaRepository gatewayJpaRepository;

  @Autowired
  private GatewayStatusLogJpaRepository statusLogJpaRepository;

  @Autowired
  private LogicalMeterJpaRepository logicalMeterJpaRepository;

  @Autowired
  private Gateways gateways;

  @Autowired
  private LogicalMeters logicalMeters;

  @Autowired
  private Organisations organisations;

  private Organisation dailyPlanet;

  @Before
  public void setUp() {
    dailyPlanet = organisations.save(DAILY_PLANET);
  }

  @After
  public void tearDown() {
    statusLogJpaRepository.deleteAll();
    logicalMeterJpaRepository.deleteAll();
    gatewayJpaRepository.deleteAll();
    organisations.deleteById(dailyPlanet.id);
  }

  @Test
  public void fetchGatewayAndStatusForCurrentMonth() {
    Gateway gateway1 = saveGateway(dailyPlanet.id);
    Gateway gateway2 = saveGateway(dailyPlanet.id);

    ZonedDateTime date = ZonedDateTime.parse("2001-04-01T00:00:00.00Z");

    saveGatewayStatus(
      gateway1,
      OK,
      date.minusDays(90),
      date.minusDays(30)
    );

    saveGatewayStatus(
      gateway1,
      WARNING,
      date.minusDays(30),
      null
    );

    saveGatewayStatus(
      gateway2,
      OK,
      date.minusDays(90),
      null
    );

    Page<GatewayDto> response = asTestSuperAdmin()
      .getPage(
        "/gateways"
        + "?after=" + date.minusDays(30)
        + "&before=" + date,
        GatewayDto.class
      );

    assertThat(response.getTotalElements()).isEqualTo(2);
    assertThat(response.getNumberOfElements()).isEqualTo(2);
    assertThat(response.getTotalPages()).isEqualTo(1);

    assertGatewayStatus(response.getContent().get(0), gateway1, WARNING);
    assertGatewayStatus(response.getContent().get(1), gateway2, OK);
  }

  @Test
  public void fetchGatewayAndStatusForPreviousMonth() {
    Gateway gateway1 = saveGateway(dailyPlanet.id);
    Gateway gateway2 = saveGateway(dailyPlanet.id);

    ZonedDateTime date = ZonedDateTime.parse("2001-04-01T00:00:00.00Z");

    saveGatewayStatus(
      gateway1,
      OK,
      date.minusDays(90),
      date.minusDays(30)
    );

    saveGatewayStatus(
      gateway1,
      WARNING,
      date.minusDays(30),
      null
    );

    saveGatewayStatus(
      gateway2,
      OK,
      date.minusDays(90),
      null
    );

    Page<GatewayDto> response = asTestSuperAdmin()
      .getPage(
        "/gateways"
        + "?after=" + date.minusDays(60)
        + "&before=" + date.minusDays(30),
        GatewayDto.class
      );

    assertThat(response.getTotalElements()).isEqualTo(2);
    assertThat(response.getNumberOfElements()).isEqualTo(2);
    assertThat(response.getTotalPages()).isEqualTo(1);

    assertGatewayStatus(response.getContent().get(0), gateway1, OK);
    assertGatewayStatus(response.getContent().get(1), gateway2, OK);
  }

  @Test
  public void fetchAllGatewaysShouldBeEmptyWhenNoGatewaysExists() {
    Page<GatewayDto> response = asTestUser()
      .getPage("/gateways", GatewayDto.class);

    assertThat(response.getTotalElements()).isEqualTo(0);
    assertThat(response.getNumberOfElements()).isEqualTo(0);
    assertThat(response.getTotalPages()).isEqualTo(0);
  }

  @Test
  public void superAdminsCanListAllGateways() {
    saveGateway(dailyPlanet.id);
    saveGateway(dailyPlanet.id);
    saveGateway(context().getOrganisationId());

    Page<GatewayDto> response = asTestSuperAdmin()
      .getPage(
        "/gateways",
        GatewayDto.class
      );

    assertThat(response.getTotalElements()).isEqualTo(3);
    assertThat(response.getNumberOfElements()).isEqualTo(3);
    assertThat(response.getTotalPages()).isEqualTo(1);
  }

  @Test
  public void createNewGateway() {
    GatewayDto requestModel = new GatewayDto(
      null,
      "123",
      "2100",
      OK.name,
      "2018-04-09 07:45:12",
      new LocationDto(),
      emptyList()
    );

    ResponseEntity<GatewayDto> response = asSuperAdmin()
      .post("/gateways", requestModel, GatewayDto.class);

    GatewayDto created = response.getBody();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(created.id).isNotNull();
    assertThat(created.serial).isEqualTo("123");
    assertThat(created.productModel).isEqualTo("2100");
  }

  @Test
  public void otherUsersCannotFetchGatewaysFromOtherOrganisations() {
    saveGateway(context().getOrganisationId());

    Page<GatewayDto> gatewayResponse = restAsUser(dailyPlanetUser(dailyPlanet))
      .getPage("/gateways", GatewayDto.class);

    assertThat(gatewayResponse.getTotalElements()).isEqualTo(0L);
  }

  @Test
  public void userCanOnlyListGatewaysWithinSameOrganisation() {
    Gateway g1 = saveGateway(dailyPlanet.id);
    Gateway g2 = saveGateway(dailyPlanet.id);
    saveGateway(context().getOrganisationId());

    Page<GatewayDto> response = as(dailyPlanetUser(dailyPlanet))
      .getPage("/gateways", GatewayDto.class);

    List<UUID> gatewayIds = response.getContent()
      .stream()
      .map(g -> g.id)
      .collect(toList());

    assertThat(gatewayIds).containsOnly(g1.id, g2.id);
  }

  @Test
  public void superUserCanGetSingleGateway() {
    UUID gatewayId = saveGateway(dailyPlanet.id).id;

    ResponseEntity<GatewayDto> response = asTestSuperAdmin()
      .get("/gateways/" + gatewayId, GatewayDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().id).isEqualTo(gatewayId);
  }

  @Test
  public void mapDataIncludesGatewaysWithoutLocation() {
    UUID gatewayId = saveGateway(dailyPlanet.id).id;

    ResponseEntity<List<MapMarkerDto>> response = asTestSuperAdmin()
      .getList("/gateways/map-markers", MapMarkerDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(1);
    assertThat(response.getBody().get(0).id).isEqualTo(gatewayId);
  }

  @Test
  public void mapDataIncludesGatewaysWithCityAndAddressLocation() {
    Gateway gateway = saveGateway(dailyPlanet.id);

    LocationBuilder location = new LocationBuilder()
      .country("sweden")
      .city("kungsbacka")
      .streetAddress("super 1")
      .latitude(1.234)
      .longitude(2.3323);

    logicalMeters.save(new LogicalMeter(
      randomUUID(),
      "external-1234",
      dailyPlanet.id,
      location.build(),
      singletonList(gateway),
      ZonedDateTime.now()
    ));

    MapMarkerDto mapMarker = new MapMarkerDto(
      gateway.id,
      "unknown",
      1.234,
      2.3323,
      1.0
    );

    ResponseEntity<List<MapMarkerDto>> cityAddressResponse = asTestSuperAdmin()
      .getList("/gateways/map-markers?address=sweden,kungsbacka,super 1", MapMarkerDto.class);

    ResponseEntity<List<MapMarkerDto>> cityResponse = asTestSuperAdmin()
      .getList("/gateways/map-markers?city=sweden,kungsbacka", MapMarkerDto.class);

    assertSameMapMarker(cityAddressResponse, mapMarker);
    assertSameMapMarker(cityResponse, mapMarker);
  }

  private void assertGatewayStatus(
    GatewayDto actalGateway,
    Gateway expectedGateway,
    StatusType expectedStatus
  ) {
    assertThat(actalGateway.id)
      .as("Unexpected gateway id")
      .isEqualTo(expectedGateway.id);

    assertThat(actalGateway.status)
      .as("Unexpected status")
      .isEqualTo(expectedStatus.name);
  }

  private Gateway saveGateway(UUID organisationId) {
    return gateways.save(new Gateway(
      randomUUID(),
      organisationId,
      randomUUID().toString(),
      randomUUID().toString()
    ));
  }

  private void assertSameMapMarker(
    ResponseEntity<List<MapMarkerDto>> response,
    MapMarkerDto mapMarker
  ) {
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(1);
    assertThat(response.getBody().get(0)).isEqualTo(mapMarker);
  }

  private void saveGatewayStatus(
    Gateway gateway,
    StatusType status,
    ZonedDateTime start,
    @Nullable ZonedDateTime stop
  ) {
    statusLogJpaRepository.save(
      new GatewayStatusLogEntity(
        null,
        gateway.id,
        status,
        start,
        stop
      )
    );
  }
}
