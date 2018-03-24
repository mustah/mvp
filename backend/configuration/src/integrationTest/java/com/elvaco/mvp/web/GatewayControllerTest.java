package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.dto.MapMarkerType;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.database.repository.jpa.GatewayJpaRepository;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.GatewayDto;
import com.elvaco.mvp.web.dto.LocationDto;
import com.elvaco.mvp.web.dto.MapMarkerDto;
import com.elvaco.mvp.web.util.Dates;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
    logicalMeterJpaRepository.deleteAll();
    gatewayJpaRepository.deleteAll();
    organisations.deleteById(dailyPlanet.id);
  }

  @Test
  public void fetchAllGatewaysShouldBeEmptyWhenNoGatewaysExists() {
    Page<GatewayDto> response = as(context().user)
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

    Page<GatewayDto> response = as(context().superAdmin)
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
      StatusType.OK.name,
      Dates.formatTime(ZonedDateTime.now(), TimeZone.getTimeZone("UTC")),
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

    ResponseEntity<GatewayDto> response = as(context().superAdmin)
      .get("/gateways/" + gatewayId, GatewayDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().id).isEqualTo(gatewayId);
  }

  @Test
  public void mapDataIncludesGatewaysWithoutLocation() {
    UUID gatewayId = saveGateway(dailyPlanet.id).id;

    ResponseEntity<List<MapMarkerDto>> response = as(context().superAdmin)
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
      MapMarkerType.Gateway,
      "unknown",
      1.234,
      2.3323,
      1.0
    );

    ResponseEntity<List<MapMarkerDto>> cityAddressResponse = as(context().superAdmin)
      .getList("/gateways/map-markers?address=sweden,kungsbacka,super 1", MapMarkerDto.class);

    ResponseEntity<List<MapMarkerDto>> cityResponse = as(context().superAdmin)
      .getList("/gateways/map-markers?city=sweden,kungsbacka", MapMarkerDto.class);

    assertSameMapMarker(cityAddressResponse, mapMarker);
    assertSameMapMarker(cityResponse, mapMarker);
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
}
