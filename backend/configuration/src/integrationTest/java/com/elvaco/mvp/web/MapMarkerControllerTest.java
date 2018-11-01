package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.GatewayStatusLogs;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.testdata.UrlTemplate;
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
import static com.elvaco.mvp.core.spi.data.RequestParameter.AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.ALARM;
import static com.elvaco.mvp.core.spi.data.RequestParameter.BEFORE;
import static com.elvaco.mvp.testing.fixture.LocationTestData.kungsbacka;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class MapMarkerControllerTest extends IntegrationTest {

  private static final ZonedDateTime NOW = ZonedDateTime.parse("2018-02-01T00:11:22Z");

  @Autowired
  private MeterStatusLogs meterStatusLogs;

  @Autowired
  private GatewayStatusLogs gatewayStatusLogs;

  @Autowired
  private MeterAlarmLogs meterAlarmLogs;

  @After
  public void tearDown() {
    meterAlarmLogJpaRepository.deleteAll();
    gatewayStatusLogJpaRepository.deleteAll();
    gatewayJpaRepository.deleteAll();
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
    UUID logicalMeterId = saveLogicalMeterWith(UNKNOWN_LOCATION, context().user).id;

    ResponseEntity<ErrorMessageDto> response = asSuperAdmin()
      .get("/map-markers/meters/" + logicalMeterId, ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(response.getBody()).isNull();
  }

  @Test
  public void findLogicalMeterWithLocation() {
    UUID logicalMeterId = saveLogicalMeter().id;

    ResponseEntity<MapMarkerWithStatusDto> response = asSuperAdmin()
      .get("/map-markers/meters/" + logicalMeterId, MapMarkerWithStatusDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MapMarkerWithStatusDto(
      logicalMeterId,
      "unknown",
      12.345,
      11.123
    ));
  }

  @Test
  public void findMeterMapMarker_ChecksOrganisation() {
    UUID logicalMeterId = saveLogicalMeter().id;

    ResponseEntity<ErrorMessageDto> missing = restAsUser(context().user2)
      .get("/map-markers/meters/" + logicalMeterId, ErrorMessageDto.class);

    assertThat(missing.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(missing.getBody().message)
      .isEqualTo("Unable to find meter with ID '" + logicalMeterId + "'");

    ResponseEntity<MapMarkerWithStatusDto> found = asUser()
      .get("/map-markers/meters/" + logicalMeterId, MapMarkerWithStatusDto.class);

    assertThat(found.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(found.getBody().id)
      .isEqualTo(logicalMeterId);
  }

  @Test
  public void meterMapMarkers_ChecksOrganisation() {
    StatusType status = StatusType.OK;
    saveLogicalAndPhysicalMeters(kungsbacka().build(), context().user, status);
    saveLogicalAndPhysicalMeters(kungsbacka().build(), context().user, status);

    ZonedDateTime before = NOW.plusDays(2);
    ZonedDateTime after = NOW.minusDays(2);

    Url urlDefinition =
      Url.builder().path("/map-markers/meters")
        .parameter(BEFORE, before)
        .parameter(AFTER, after)
        .build();

    ResponseEntity<MapMarkersDto> differentOrganisation = restAsUser(context().user2)
      .get(urlDefinition, MapMarkersDto.class);

    assertThat(differentOrganisation.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(differentOrganisation.getBody().markers.size()).isEqualTo(0);

    ResponseEntity<MapMarkersDto> sameOrganisation = asUser()
      .get(urlDefinition, MapMarkersDto.class);

    assertThat(sameOrganisation.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(sameOrganisation.getBody().markers.get(status).size()).isEqualTo(2);
  }

  @Test
  public void meterMapMarkers_FindsMapMarkers() {
    LogicalMeter logicalMeter = saveLogicalMeter();

    savePhysicalMeterWith(logicalMeter, StatusType.OK);

    ResponseEntity<MapMarkersDto> response = asSuperAdmin()
      .get("/map-markers/meters", MapMarkersDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().markers).isEqualTo(ImmutableMultimap.builder()
      .put(StatusType.OK, new MapMarkerDto(logicalMeter.id, 12.345, 11.123))
      .build()
      .asMap());
  }

  @Test
  public void meterMapMarkers_FindsMapMarkersWithParameters() {
    LogicalMeter meter1 = saveLogicalMeterWith(UNKNOWN_LOCATION, context().user);
    LogicalMeter meter2 = saveLogicalMeter();
    LogicalMeter meter3 = saveLogicalMeter();

    savePhysicalMeterWith(meter1, StatusType.WARNING);
    savePhysicalMeterWith(meter2, StatusType.WARNING);
    savePhysicalMeterWith(meter3, StatusType.WARNING);

    ResponseEntity<MapMarkersDto> response = asUser()
      .get("/map-markers/meters?city=sverige,kungsbacka", MapMarkersDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().markers).hasSize(1);
    assertThat(response.getBody().markers.get(StatusType.WARNING))
      .containsExactlyInAnyOrder(
        new MapMarkerDto(meter2.id, 12.345, 11.123),
        new MapMarkerDto(meter3.id, 12.345, 11.123)
      );
  }

  @Test
  public void meterMapMarkers_WithAlarms() {
    LogicalMeter meter1 = saveLogicalMeter();
    LogicalMeter meter2 = saveLogicalMeter();
    LogicalMeter meter3 = saveLogicalMeter();

    savePhysicalMeterWith(meter1, StatusType.WARNING);
    PhysicalMeter physicalMeter2 = savePhysicalMeterWith(meter2, StatusType.OK);
    PhysicalMeter physicalMeter3 = savePhysicalMeterWith(meter3, StatusType.ERROR);

    AlarmLogEntry.AlarmLogEntryBuilder alarmBuilder = AlarmLogEntry.builder()
      .start(ZonedDateTime.now());

    meterAlarmLogs.save(alarmBuilder.entityId(physicalMeter2.id).mask(55).build());
    meterAlarmLogs.save(alarmBuilder.entityId(physicalMeter3.id).mask(99).build());

    ResponseEntity<MapMarkersDto> response = asUser()
      .get(mapMarkerAlarmUrl("yes"), MapMarkersDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().markers).hasSize(2);
    assertThat(response.getBody().markers.get(StatusType.OK))
      .containsExactlyInAnyOrder(new MapMarkerDto(meter2.id, 12.345, 11.123, 55));
    assertThat(response.getBody().markers.get(StatusType.ERROR))
      .containsExactlyInAnyOrder(new MapMarkerDto(meter3.id, 12.345, 11.123, 99));
  }

  @Test
  public void gatewayMapMarkers_WithAlarms() {
    Gateway gateway1 = saveGatewayWith(context().organisationId(), StatusType.WARNING);
    Gateway gateway2 = saveGatewayWith(context().organisationId(), StatusType.OK);
    Gateway gateway3 = saveGatewayWith(context().organisationId(), StatusType.ERROR);

    LogicalMeter meter1 = saveLogicalMeterWith(kungsbacka().build(), gateway1);
    LogicalMeter meter2 = saveLogicalMeterWith(kungsbacka().build(), gateway2);
    LogicalMeter meter3 = saveLogicalMeterWith(kungsbacka().build(), gateway3);

    savePhysicalMeterWith(meter1, StatusType.WARNING);
    PhysicalMeter physicalMeter2 = savePhysicalMeterWith(meter2, StatusType.OK);
    PhysicalMeter physicalMeter3 = savePhysicalMeterWith(meter3, StatusType.ERROR);

    AlarmLogEntry.AlarmLogEntryBuilder alarmBuilder = AlarmLogEntry.builder()
      .start(ZonedDateTime.now());

    meterAlarmLogs.save(alarmBuilder.entityId(physicalMeter2.id).mask(55).build());
    meterAlarmLogs.save(alarmBuilder.entityId(physicalMeter3.id).mask(99).build());

    ResponseEntity<MapMarkersDto> response = asUser()
      .get(gatewayMapMarkerAlarmUrl("yes"), MapMarkersDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().markers).hasSize(2);
    assertThat(response.getBody().markers.get(StatusType.OK))
      .containsExactlyInAnyOrder(new MapMarkerDto(gateway2.id, 12.345, 11.123, 55));
    assertThat(response.getBody().markers.get(StatusType.ERROR))
      .containsExactlyInAnyOrder(new MapMarkerDto(gateway3.id, 12.345, 11.123, 99));
  }

  @Test
  public void gatewayMapMarkers_doNotIncludeMarkersWithAlarmsOutsidePeriod() {
    Gateway gateway = saveGatewayWith(context().organisationId(), StatusType.OK);

    LogicalMeter meter = saveLogicalMeterWith(kungsbacka().build(), gateway);

    PhysicalMeter physicalMeter = savePhysicalMeterWith(meter, StatusType.OK);

    ZonedDateTime now = ZonedDateTime.now();
    AlarmLogEntry.AlarmLogEntryBuilder alarmBuilder = AlarmLogEntry.builder()
      .start(now.minusDays(2)).stop(now.minusDays(1)).mask(1);

    meterAlarmLogs.save(alarmBuilder.entityId(physicalMeter.id).build());
    Url url = Url.builder().path("/map-markers/gateways")
      .parameter(RequestParameter.ALARM, "yes")
      .parameter(RequestParameter.AFTER, now.minusDays(1).plusHours(1))
      .parameter(RequestParameter.BEFORE, now)
      .build();

    ResponseEntity<MapMarkersDto> response = asUser()
      .get(url, MapMarkersDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().markers).isEmpty();
  }

  @Test
  public void meterMapMarkers_WithoutAlarms() {
    LogicalMeter meter1 = saveLogicalMeter();
    LogicalMeter meter2 = saveLogicalMeter();
    LogicalMeter meter3 = saveLogicalMeter();

    savePhysicalMeterWith(meter1, StatusType.WARNING);
    PhysicalMeter physicalMeter2 = savePhysicalMeterWith(meter2, StatusType.OK);
    PhysicalMeter physicalMeter3 = savePhysicalMeterWith(meter3, StatusType.ERROR);

    AlarmLogEntry.AlarmLogEntryBuilder alarmBuilder = AlarmLogEntry.builder()
      .start(ZonedDateTime.now());

    meterAlarmLogs.save(alarmBuilder.entityId(physicalMeter2.id).mask(55).build());
    meterAlarmLogs.save(alarmBuilder.entityId(physicalMeter3.id).mask(99).build());

    ResponseEntity<MapMarkersDto> response = asUser()
      .get(mapMarkerAlarmUrl("no"), MapMarkersDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().markers).hasSize(1);
    assertThat(response.getBody().markers.get(StatusType.WARNING))
      .containsExactlyInAnyOrder(new MapMarkerDto(meter1.id, 12.345, 11.123));
  }

  @Test
  public void gatewayMapMarkers_ChecksOrganisation() {
    Gateway gateway1 = saveGatewayWith(context().organisationId(), StatusType.WARNING);
    Gateway gateway2 = saveGatewayWith(context().organisationId(), StatusType.WARNING);
    Gateway gateway3 = saveGatewayWith(context().organisationId(), StatusType.WARNING);

    saveLogicalMeterWith(UNKNOWN_LOCATION, gateway1);
    saveLogicalMeterWith(kungsbacka().build(), gateway2);
    saveLogicalMeterWith(kungsbacka().build(), gateway3);

    String url = "/map-markers/gateways?city=sverige,kungsbacka";
    ResponseEntity<MapMarkersDto> foundByCorrectUser = asUser()
      .get(url, MapMarkersDto.class);

    assertThat(foundByCorrectUser.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(foundByCorrectUser.getBody().markers).hasSize(1);
    assertThat(foundByCorrectUser.getBody().markers.get(StatusType.WARNING))
      .containsExactlyInAnyOrder(
        new MapMarkerDto(gateway2.id, 12.345, 11.123),
        new MapMarkerDto(gateway3.id, 12.345, 11.123)
      );

    ResponseEntity<MapMarkersDto> notFoundByIncorrectUser = restAsUser(context().user2)
      .get(url, MapMarkersDto.class);

    assertThat(notFoundByIncorrectUser.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(notFoundByIncorrectUser.getBody().markers).hasSize(0);
  }

  @Test
  public void doNotIncludeMeterMapMarkerWithLowConfidence() {
    saveLogicalMeterWith(kungsbacka()
      .confidence(0.5)
      .build(), context().user);

    ResponseEntity<MapMarkersDto> response = asUser()
      .get("/map-markers/meters", MapMarkersDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().markers).isEmpty();
  }

  @Test
  public void findMeterMapMarker_EmptyBodyForLowConfidence() {
    LogicalMeter logicalMeter = saveLogicalMeterWith(kungsbacka()
      .confidence(0.5)
      .build(), context().user);

    ResponseEntity<MapMarkerWithStatusDto> response = asUser()
      .get("/map-markers/meters/" + logicalMeter.id, MapMarkerWithStatusDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(response.getBody()).isNull();
  }

  @Test
  public void mapDataDoesNotIncludeGatewaysWithoutLocation() {
    saveGatewayWith(context().organisationId2(), StatusType.OK);

    ResponseEntity<MapMarkersDto> response = asSuperAdmin()
      .get("/map-markers/gateways", MapMarkersDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().markers).isEmpty();
  }

  @Test
  public void mapMarkersIncludesGatewaysWithCityAndAddressLocation() {
    Gateway gateway = saveGatewayWith(context().organisationId2(), StatusType.UNKNOWN);

    logicalMeters.save(LogicalMeter.builder()
      .externalId("external-1234")
      .organisationId(context().organisationId2())
      .created(NOW)
      .gateway(gateway)
      .location(kungsbacka().address("super 1").build())
      .build());

    ResponseEntity<MapMarkersDto> cityAddressResponse = asSuperAdmin()
      .get("/map-markers/gateways?address=sverige,kungsbacka,super+1", MapMarkersDto.class);

    ResponseEntity<MapMarkersDto> cityResponse = asSuperAdmin()
      .get("/map-markers/gateways?city=sverige,kungsbacka", MapMarkersDto.class);

    assertThat(cityResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(cityAddressResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(cityResponse.getBody().markers).isEqualTo(ImmutableMultimap.builder()
      .put(StatusType.UNKNOWN, new MapMarkerDto(gateway.id, 12.345, 11.123))
      .build()
      .asMap());
    assertThat(cityAddressResponse.getBody().markers).isEqualTo(ImmutableMultimap.builder()
      .put(StatusType.UNKNOWN, new MapMarkerDto(gateway.id, 12.345, 11.123))
      .build()
      .asMap());
  }

  @Test
  public void cannotFindGatewayMapMarkers_WithUnknownCity() {
    Gateway gateway = saveGatewayWith(context().organisationId2(), StatusType.OK);

    logicalMeters.save(LogicalMeter.builder()
      .externalId("external-1234")
      .organisationId(context().organisationId2())
      .created(NOW)
      .gateway(gateway)
      .build());

    ResponseEntity<MapMarkersDto> response = asSuperAdmin()
      .get("/map-markers/gateways?city=unknown,unknown", MapMarkersDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().markers).isEmpty();
  }

  @Test
  public void doNotIncludeGatewayMapMarkerWithLowConfidence() {
    Gateway gateway = saveGatewayWith(context().organisationId2(), StatusType.OK);

    logicalMeters.save(LogicalMeter.builder()
      .externalId("external-1234")
      .organisationId(context().organisationId2())
      .created(NOW)
      .gateway(gateway)
      .location(kungsbacka()
        .confidence(0.5)
        .build())
      .build());

    ResponseEntity<MapMarkersDto> response = asSuperAdmin()
      .get("/map-markers/gateways", MapMarkersDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().markers).isEmpty();
  }

  private LogicalMeter saveLogicalMeter() {
    return saveLogicalMeterWith(kungsbacka().build(), context().user);
  }

  private PhysicalMeter savePhysicalMeterWith(LogicalMeter logicalMeter, StatusType status) {
    PhysicalMeter physicalMeter = physicalMeters.save(
      PhysicalMeter.builder()
        .logicalMeterId(logicalMeter.id)
        .externalId(logicalMeter.externalId)
        .address("v1")
        .manufacturer("ELV")
        .organisation(context().organisation())
        .build()
    );

    meterStatusLogs.save(
      StatusLogEntry.<UUID>builder()
        .entityId(physicalMeter.id)
        .status(status)
        .start(NOW)
        .build()
    );
    return physicalMeter;
  }

  private LogicalMeter saveLogicalMeterWith(Location location, Gateway gateway) {
    return logicalMeters.save(LogicalMeter.builder()
      .externalId(randomUUID().toString())
      .organisationId(context().organisationId())
      .created(NOW)
      .gateway(gateway)
      .location(location)
      .build());
  }

  private LogicalMeter saveLogicalMeterWith(Location location, User user) {
    return logicalMeters.save(LogicalMeter.builder()
      .externalId(randomUUID().toString())
      .organisationId(user.organisation.id)
      .created(NOW)
      .location(location)
      .build());
  }

  private LogicalMeter saveLogicalAndPhysicalMeters(
    Location location,
    User user,
    StatusType status
  ) {
    LogicalMeter logicalMeter = logicalMeters.save(LogicalMeter.builder()
      .externalId(randomUUID().toString())
      .organisationId(user.organisation.id)
      .created(NOW)
      .location(location)
      .build());
    savePhysicalMeterWith(logicalMeter, status);
    return logicalMeter;
  }

  private Gateway saveGatewayWith(UUID organisationId, StatusType status) {
    Gateway gateway = gateways.save(Gateway.builder()
      .organisationId(organisationId)
      .productModel(randomUUID().toString())
      .serial(randomUUID().toString())
      .build()
    );
    gatewayStatusLogs.save(
      StatusLogEntry.<UUID>builder()
        .entityId(gateway.id)
        .status(status)
        .start(NOW)
        .build()
    );
    return gateway;
  }

  private static UrlTemplate mapMarkerAlarmUrl(String alarm) {
    ZonedDateTime now = ZonedDateTime.now();
    return Url.builder().path("/map-markers/meters")
      .parameter(ALARM, alarm)
      .parameter(AFTER, now.minusDays(1))
      .parameter(BEFORE, now.plusDays(1))
      .build();
  }

  private static UrlTemplate gatewayMapMarkerAlarmUrl(String alarm) {
    ZonedDateTime now = ZonedDateTime.now();
    return Url.builder().path("/map-markers/gateways")
      .parameter(ALARM, alarm)
      .parameter(AFTER, now.minusDays(1))
      .parameter(BEFORE, now.plusDays(1))
      .build();
  }
}
