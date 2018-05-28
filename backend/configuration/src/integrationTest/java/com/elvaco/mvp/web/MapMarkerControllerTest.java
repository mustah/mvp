package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.database.repository.jpa.GatewayJpaRepository;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.MapMarkerDto;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class MapMarkerControllerTest extends IntegrationTest {

  @Autowired
  private LogicalMeterJpaRepository logicalMeterJpaRepository;

  @Autowired
  private GatewayJpaRepository gatewayJpaRepository;

  @Autowired
  private Gateways gateways;

  @Autowired
  private LogicalMeters logicalMeters;

  @After
  public void tearDown() {
    gatewayJpaRepository.deleteAll();
    logicalMeterJpaRepository.deleteAll();
  }

  @Test
  public void locationForMeterNotFound() {
    UUID logicalMeterId = randomUUID();

    ResponseEntity<ErrorMessageDto> response = asSuperAdmin()
      .get("/map-markers/meters/" + logicalMeterId, ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody().message)
      .isEqualTo("Unable to find meter with ID '" + logicalMeterId + "'");
  }

  @Test
  public void findMapMarkerWithNoLocation() {
    UUID logicalMeterId = saveLogicalMeterWith(UNKNOWN_LOCATION).id;

    ResponseEntity<MapMarkerDto> response = asSuperAdmin()
      .get("/map-markers/meters/" + logicalMeterId, MapMarkerDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MapMarkerDto(
      logicalMeterId,
      "unknown",
      null,
      null,
      null
    ));
  }

  @Test
  public void findLogicalMeterWithLocation() {
    UUID logicalMeterId = saveLogicalMeterWith(newLocation()).id;

    ResponseEntity<MapMarkerDto> response = asSuperAdmin()
      .get("/map-markers/meters/" + logicalMeterId, MapMarkerDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MapMarkerDto(
      logicalMeterId,
      "unknown",
      2.1222,
      1.2212,
      1.0
    ));
  }

  @Test
  public void userShouldNotBeAbleToFindLocationForAMeterFromAnotherOrganisation() {
    UUID logicalMeterId = saveLogicalMeterWith(newLocation()).id;

    ResponseEntity<ErrorMessageDto> response = asOtherUser()
      .get("/map-markers/meters/" + logicalMeterId, ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody().message)
      .isEqualTo("Unable to find meter with ID '" + logicalMeterId + "'");
  }

  @Test
  public void findAllMapMarkersForLogicalMeters() {
    saveLogicalMeterWith(newLocation());

    ResponseEntity<List<MapMarkerDto>> response = asTestUser()
      .getList("/map-markers/meters", MapMarkerDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().size()).isEqualTo(1);
  }

  @Test
  public void findAllMapMarkersForLogicalMetersWithParameters() {
    saveLogicalMeterWith(UNKNOWN_LOCATION);
    saveLogicalMeterWith(newLocation());
    saveLogicalMeterWith(newLocation());

    ResponseEntity<List<MapMarkerDto>> response = asTestUser()
      .getList("/map-markers/meters?city=sweden,kungsbacka", MapMarkerDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().size()).isEqualTo(2);
  }

  @Test
  public void mapDataIncludesGatewaysWithoutLocation() {
    UUID gatewayId = saveGateway(context().getOrganisationId2()).id;

    ResponseEntity<List<MapMarkerDto>> response = asTestSuperAdmin()
      .getList("/map-markers/gateways", MapMarkerDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(1);
    assertThat(response.getBody().get(0).id).isEqualTo(gatewayId);
  }

  @Test
  public void mapMarkersIncludesGatewaysWithCityAndAddressLocation() {
    Gateway gateway = saveGateway(context().getOrganisationId2());

    LocationBuilder location = new LocationBuilder()
      .country("sweden")
      .city("kungsbacka")
      .address("super 1")
      .latitude(1.234)
      .longitude(2.3323);

    logicalMeters.save(new LogicalMeter(
      randomUUID(),
      "external-1234",
      context().getOrganisationId2(),
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
      .getList("/map-markers/gateways?address=sweden,kungsbacka,super 1", MapMarkerDto.class);

    ResponseEntity<List<MapMarkerDto>> cityResponse = asTestSuperAdmin()
      .getList("/map-markers/gateways?city=sweden,kungsbacka", MapMarkerDto.class);

    assertSameMapMarker(cityAddressResponse, mapMarker);
    assertSameMapMarker(cityResponse, mapMarker);
  }

  @Test
  public void findGatewayMapMarkers_WithUnknownCity() {
    Gateway gateway = saveGateway(context().getOrganisationId2());

    logicalMeters.save(new LogicalMeter(
      randomUUID(),
      "external-1234",
      context().getOrganisationId2(),
      new LocationBuilder().build(),
      singletonList(gateway),
      ZonedDateTime.now()
    ));

    MapMarkerDto mapMarker = new MapMarkerDto();
    mapMarker.id = gateway.id;
    mapMarker.status = "unknown";

    ResponseEntity<List<MapMarkerDto>> response = asTestSuperAdmin()
      .getList("/map-markers/gateways?city=unknown,unknown", MapMarkerDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).containsExactly(mapMarker);
  }

  private void assertSameMapMarker(
    ResponseEntity<List<MapMarkerDto>> response,
    MapMarkerDto mapMarker
  ) {
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(1);
    assertThat(response.getBody().get(0)).isEqualTo(mapMarker);
  }

  private LogicalMeter saveLogicalMeterWith(Location location) {
    return logicalMeters.save(logicalMeterWith(location));
  }

  private LogicalMeter logicalMeterWith(Location location) {
    return new LogicalMeter(
      randomUUID(),
      randomUUID().toString(),
      context().getOrganisationId(),
      location,
      ZonedDateTime.now()
    );
  }

  private Gateway saveGateway(UUID organisationId) {
    return gateways.save(new Gateway(
      randomUUID(),
      organisationId,
      randomUUID().toString(),
      randomUUID().toString()
    ));
  }

  private static Location newLocation() {
    return new LocationBuilder()
      .country("sweden")
      .city("kungsbacka")
      .address("kabelgatan 2t")
      .longitude(1.2212)
      .latitude(2.1222)
      .build();
  }
}
