package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.GatewayStatusLogs;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.database.repository.jpa.GatewayJpaRepository;
import com.elvaco.mvp.database.repository.jpa.GatewayStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeterAlarmLogJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
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
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class MapMarkerControllerTest extends IntegrationTest {

  private static final ZonedDateTime NOW = ZonedDateTime.parse("2018-02-01T00:11:22Z");

  @Autowired
  private LogicalMeterJpaRepository logicalMeterJpaRepository;

  @Autowired
  private PhysicalMeterJpaRepository physicalMeterJpaRepository;

  @Autowired
  private PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository;

  @Autowired
  private GatewayStatusLogJpaRepository gatewayStatusLogJpaRepository;

  @Autowired
  private GatewayJpaRepository gatewayJpaRepository;

  @Autowired
  private MeterAlarmLogJpaRepository meterAlarmLogJpaRepository;

  @Autowired
  private Gateways gateways;

  @Autowired
  private LogicalMeters logicalMeters;

  @Autowired
  private PhysicalMeters physicalMeters;

  @Autowired
  private MeterStatusLogs meterStatusLogs;

  @Autowired
  private GatewayStatusLogs gatewayStatusLogs;

  @Autowired
  private MeterAlarmLogs meterAlarmLogs;

  @After
  public void tearDown() {
    logicalMeterJpaRepository.deleteAll();
    physicalMeterJpaRepository.deleteAll();
    physicalMeterStatusLogJpaRepository.deleteAll();
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
      2.1222,
      1.2212
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

    ResponseEntity<MapMarkerWithStatusDto> found = asTestUser()
      .get("/map-markers/meters/" + logicalMeterId, MapMarkerWithStatusDto.class);

    assertThat(found.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(found.getBody().id)
      .isEqualTo(logicalMeterId);
  }

  @Test
  public void meterMapMarkers_ChecksOrganisation() {
    StatusType status = StatusType.OK;
    saveLogicalAndPhysicalMeters(newLocation(), context().user, status);
    saveLogicalAndPhysicalMeters(newLocation(), context().user, status);

    ZonedDateTime before = NOW.plusDays(2);
    ZonedDateTime after = NOW.minusDays(2);

    String url = "/map-markers/meters/?before=" + before + "&after=" + after;

    ResponseEntity<MapMarkersDto> differentOrganisation = restAsUser(context().user2)
      .get(url, MapMarkersDto.class);

    assertThat(differentOrganisation.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(differentOrganisation.getBody().markers.size()).isEqualTo(0);

    ResponseEntity<MapMarkersDto> sameOrganisation = asTestUser()
      .get(url, MapMarkersDto.class);

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
      .put(StatusType.OK, new MapMarkerDto(logicalMeter.id, 2.1222, 1.2212))
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

    ResponseEntity<MapMarkersDto> response = asTestUser()
      .get("/map-markers/meters?city=sweden,kungsbacka", MapMarkersDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().markers).hasSize(1);
    assertThat(response.getBody().markers.get(StatusType.WARNING))
      .containsExactlyInAnyOrder(
        new MapMarkerDto(meter2.id, 2.1222, 1.2212),
        new MapMarkerDto(meter3.id, 2.1222, 1.2212)
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

    ResponseEntity<MapMarkersDto> response = asTestUser()
      .get(mapMarkerAlarmUrl("yes"), MapMarkersDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().markers).hasSize(2);
    assertThat(response.getBody().markers.get(StatusType.OK))
      .containsExactlyInAnyOrder(new MapMarkerDto(meter2.id, 2.1222, 1.2212, 55));
    assertThat(response.getBody().markers.get(StatusType.ERROR))
      .containsExactlyInAnyOrder(new MapMarkerDto(meter3.id, 2.1222, 1.2212, 99));
  }

  @Test
  public void gatewayMapMarkers_WithAlarms() {
    Gateway gateway1 = saveGatewayWith(context().organisationId(), StatusType.WARNING);
    Gateway gateway2 = saveGatewayWith(context().organisationId(), StatusType.OK);
    Gateway gateway3 = saveGatewayWith(context().organisationId(), StatusType.ERROR);

    LogicalMeter meter1 = saveLogicalMeterWith(newLocation(), gateway1);
    LogicalMeter meter2 = saveLogicalMeterWith(newLocation(), gateway2);
    LogicalMeter meter3 = saveLogicalMeterWith(newLocation(), gateway3);

    savePhysicalMeterWith(meter1, StatusType.WARNING);
    PhysicalMeter physicalMeter2 = savePhysicalMeterWith(meter2, StatusType.OK);
    PhysicalMeter physicalMeter3 = savePhysicalMeterWith(meter3, StatusType.ERROR);

    AlarmLogEntry.AlarmLogEntryBuilder alarmBuilder = AlarmLogEntry.builder()
      .start(ZonedDateTime.now());

    meterAlarmLogs.save(alarmBuilder.entityId(physicalMeter2.id).mask(55).build());
    meterAlarmLogs.save(alarmBuilder.entityId(physicalMeter3.id).mask(99).build());

    ResponseEntity<MapMarkersDto> response = asTestUser()
      .get(gatewayMapMarkerAlarmUrl("yes"), MapMarkersDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().markers).hasSize(2);
    assertThat(response.getBody().markers.get(StatusType.OK))
      .containsExactlyInAnyOrder(new MapMarkerDto(gateway2.id, 2.1222, 1.2212, 55));
    assertThat(response.getBody().markers.get(StatusType.ERROR))
      .containsExactlyInAnyOrder(new MapMarkerDto(gateway3.id, 2.1222, 1.2212, 99));
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

    ResponseEntity<MapMarkersDto> response = asTestUser()
      .get(mapMarkerAlarmUrl("no"), MapMarkersDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().markers).hasSize(1);
    assertThat(response.getBody().markers.get(StatusType.WARNING))
      .containsExactlyInAnyOrder(new MapMarkerDto(meter1.id, 2.1222, 1.2212));
  }

  @Test
  public void gatewayMapMarkers_ChecksOrganisation() {
    Gateway gateway1 = saveGatewayWith(context().organisationId(), StatusType.WARNING);
    Gateway gateway2 = saveGatewayWith(context().organisationId(), StatusType.WARNING);
    Gateway gateway3 = saveGatewayWith(context().organisationId(), StatusType.WARNING);

    saveLogicalMeterWith(UNKNOWN_LOCATION, gateway1);
    saveLogicalMeterWith(newLocation(), gateway2);
    saveLogicalMeterWith(newLocation(), gateway3);

    String url = "/map-markers/gateways?city=sweden,kungsbacka";
    ResponseEntity<MapMarkersDto> foundByCorrectUser = asTestUser()
      .get(url, MapMarkersDto.class);

    assertThat(foundByCorrectUser.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(foundByCorrectUser.getBody().markers).hasSize(1);
    assertThat(foundByCorrectUser.getBody().markers.get(StatusType.WARNING))
      .containsExactlyInAnyOrder(
        new MapMarkerDto(gateway2.id, 2.1222, 1.2212),
        new MapMarkerDto(gateway3.id, 2.1222, 1.2212)
      );

    ResponseEntity<MapMarkersDto> notFoundByIncorrectUser = restAsUser(context().user2)
      .get(url, MapMarkersDto.class);

    assertThat(notFoundByIncorrectUser.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(notFoundByIncorrectUser.getBody().markers).hasSize(0);
  }

  @Test
  public void doNotIncludeMeterMapMarkerWithLowConfidence() {
    saveLogicalMeterWith(lowConfidenceLocation(), context().user);

    ResponseEntity<MapMarkersDto> response = asTestUser()
      .get("/map-markers/meters", MapMarkersDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().markers).isEmpty();
  }

  @Test
  public void findMeterMapMarker_EmptyBodyForLowConfidence() {
    LogicalMeter logicalMeter = saveLogicalMeterWith(lowConfidenceLocation(), context().user);

    ResponseEntity<MapMarkerWithStatusDto> response = asTestUser()
      .get("/map-markers/meters/" + logicalMeter.id, MapMarkerWithStatusDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(response.getBody()).isNull();
  }

  @Test
  public void mapDataDoesNotIncludeGatewaysWithoutLocation() {
    saveGatewayWith(context().organisationId2(), StatusType.OK);

    ResponseEntity<MapMarkersDto> response = asTestSuperAdmin()
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
      .location(new LocationBuilder()
        .country("sweden")
        .city("kungsbacka")
        .address("super 1")
        .latitude(1.234)
        .longitude(2.3323)
        .build())
      .build());

    ResponseEntity<MapMarkersDto> cityAddressResponse = asTestSuperAdmin()
      .get("/map-markers/gateways?address=sweden,kungsbacka,super 1", MapMarkersDto.class);

    ResponseEntity<MapMarkersDto> cityResponse = asTestSuperAdmin()
      .get("/map-markers/gateways?city=sweden,kungsbacka", MapMarkersDto.class);

    assertThat(cityResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(cityAddressResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(cityResponse.getBody().markers).isEqualTo(ImmutableMultimap.builder()
      .put(StatusType.UNKNOWN, new MapMarkerDto(gateway.id, 1.234, 2.3323))
      .build()
      .asMap());
    assertThat(cityAddressResponse.getBody().markers).isEqualTo(ImmutableMultimap.builder()
      .put(StatusType.UNKNOWN, new MapMarkerDto(gateway.id, 1.234, 2.3323))
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
      .location(new LocationBuilder().build())
      .build());

    ResponseEntity<MapMarkersDto> response = asTestSuperAdmin()
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
      .location(lowConfidenceLocation())
      .build());

    ResponseEntity<MapMarkersDto> response = asTestSuperAdmin()
      .get("/map-markers/gateways", MapMarkersDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().markers).isEmpty();
  }

  private LogicalMeter saveLogicalMeter() {
    return saveLogicalMeterWith(newLocation(), context().user);
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

  private static String mapMarkerAlarmUrl(String alarm) {
    return urlOf("meters", alarm);
  }

  private static String gatewayMapMarkerAlarmUrl(String alarm) {
    return urlOf("gateways", alarm);
  }

  private static String urlOf(String entity, String alarm) {
    ZonedDateTime now = ZonedDateTime.now();
    return String.format(
      "/map-markers/%s?alarm=%s&after=%s&before=%s",
      entity,
      alarm,
      now.minusDays(1),
      now.plusDays(1)
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
      .latitude(2.1222)
      .confidence(1.0);
  }

  private static LocationBuilder kungsbacka() {
    return new LocationBuilder()
      .country("sweden")
      .city("kungsbacka")
      .address("kabelgatan 2t");
  }
}
