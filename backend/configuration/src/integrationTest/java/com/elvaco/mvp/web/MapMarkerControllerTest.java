package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.database.repository.jpa.GatewayJpaRepository;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.MapMarkerDto;
import com.elvaco.mvp.web.dto.MapMarkerWithStatusDto;
import com.elvaco.mvp.web.dto.MapMarkersDto;
import com.google.common.collect.ImmutableMultimap;
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
  public void cannotFindMapMarkerWithNoLocation_HasEmptyBody() {
    UUID logicalMeterId = saveLogicalMeterWith(UNKNOWN_LOCATION).id;

    ResponseEntity<ErrorMessageDto> response = asSuperAdmin()
      .get("/map-markers/meters/" + logicalMeterId, ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNull();
  }

  @Test
  public void findLogicalMeterWithLocation() {
    UUID logicalMeterId = saveLogicalMeterWith(newLocation()).id;

    ResponseEntity<MapMarkerWithStatusDto> response = asSuperAdmin()
      .get("/map-markers/meters/" + logicalMeterId, MapMarkerWithStatusDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MapMarkerWithStatusDto(
      logicalMeterId,
      "unknown",
      2.1222,
      1.2212
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
    LogicalMeter logicalMeter = saveLogicalMeterWith(newLocation());

    ResponseEntity<MapMarkersDto> response = asTestUser()
      .get("/map-markers/meters", MapMarkersDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().markers).isEqualTo(ImmutableMultimap.builder()
      .put(StatusType.UNKNOWN.name, new MapMarkerDto(logicalMeter.id, 2.1222, 1.2212))
      .build()
      .asMap());
  }

  @Test
  public void findAllMapMarkersForLogicalMetersWithParameters() {
    saveLogicalMeterWith(UNKNOWN_LOCATION);
    LogicalMeter meter2 = saveLogicalMeterWith(newLocation());
    LogicalMeter meter3 = saveLogicalMeterWith(newLocation());

    ResponseEntity<MapMarkersDto> response = asTestUser()
      .get("/map-markers/meters?city=sweden,kungsbacka", MapMarkersDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().markers).hasSize(1);
    assertThat(response.getBody().markers.get(StatusType.UNKNOWN.name))
      .containsExactlyInAnyOrder(
        new MapMarkerDto(meter2.id, 2.1222, 1.2212),
        new MapMarkerDto(meter3.id, 2.1222, 1.2212)
      );
  }

  @Test
  public void doNotIncludeMeterMapMarkerWithLowConfidence() {
    saveLogicalMeterWith(lowConfidenceLocation());

    ResponseEntity<MapMarkersDto> response = asTestUser()
      .get("/map-markers/meters", MapMarkersDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().markers).isEmpty();
  }

  @Test
  public void returnsOk_EmptyBody_WhenMeterLocationHas_LowConfidence() {
    LogicalMeter logicalMeter = saveLogicalMeterWith(lowConfidenceLocation());

    ResponseEntity<MapMarkerWithStatusDto> response = asTestUser()
      .get("/map-markers/meters/" + logicalMeter.id, MapMarkerWithStatusDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNull();
  }

  @Test
  public void mapDataDoesNotIncludeGatewaysWithoutLocation() {
    saveGateway(context().getOrganisationId2());

    ResponseEntity<MapMarkersDto> response = asTestSuperAdmin()
      .get("/map-markers/gateways", MapMarkersDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().markers).isEmpty();
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

    ResponseEntity<MapMarkersDto> cityAddressResponse = asTestSuperAdmin()
      .get("/map-markers/gateways?address=sweden,kungsbacka,super 1", MapMarkersDto.class);

    ResponseEntity<MapMarkersDto> cityResponse = asTestSuperAdmin()
      .get("/map-markers/gateways?city=sweden,kungsbacka", MapMarkersDto.class);

    assertThat(cityResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(cityAddressResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    StatusType status = StatusType.UNKNOWN;

    assertThat(cityResponse.getBody().markers).isEqualTo(ImmutableMultimap.builder()
      .put(status.name, new MapMarkerDto(gateway.id, 1.234, 2.3323))
      .build()
      .asMap());
    assertThat(cityAddressResponse.getBody().markers).isEqualTo(ImmutableMultimap.builder()
      .put(status.name, new MapMarkerDto(gateway.id, 1.234, 2.3323))
      .build()
      .asMap());
  }

  @Test
  public void cannotFindGatewayMapMarkers_WithUnknownCity() {
    Gateway gateway = saveGateway(context().getOrganisationId2());

    logicalMeters.save(new LogicalMeter(
      randomUUID(),
      "external-1234",
      context().getOrganisationId2(),
      new LocationBuilder().build(),
      singletonList(gateway),
      ZonedDateTime.now()
    ));

    ResponseEntity<MapMarkersDto> response = asTestSuperAdmin()
      .get("/map-markers/gateways?city=unknown,unknown", MapMarkersDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().markers).isEmpty();
  }

  @Test
  public void doNotIncludeGatewayMapMarkerWithLowConfidence() {
    Gateway gateway = saveGateway(context().getOrganisationId2());

    logicalMeters.save(new LogicalMeter(
      randomUUID(),
      "external-1234",
      context().getOrganisationId2(),
      lowConfidenceLocation(),
      singletonList(gateway),
      ZonedDateTime.now()
    ));

    ResponseEntity<MapMarkersDto> response = asTestSuperAdmin()
      .get("/map-markers/gateways", MapMarkersDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().markers).isEmpty();
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
    return gateways.save(Gateway.builder()
      .organisationId(organisationId)
      .productModel(randomUUID().toString())
      .serial(randomUUID().toString())
      .build()
    );
  }

  private static Location lowConfidenceLocation() {
    return withGeoPosition()
      .confidence(0.5)
      .build();
  }

  private static Location newLocation() {
    return withGeoPosition().build();
  }

  private static LocationBuilder withGeoPosition() {
    return kungsbacka()
      .longitude(1.2212)
      .latitude(2.1222);
  }

  private static LocationBuilder kungsbacka() {
    return new LocationBuilder()
      .country("sweden")
      .city("kungsbacka")
      .address("kabelgatan 2t");
  }
}
