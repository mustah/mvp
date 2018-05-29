package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
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
import com.elvaco.mvp.web.dto.GeoPositionDto;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.LocationDto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.domainmodels.StatusType.OK;
import static com.elvaco.mvp.core.domainmodels.StatusType.WARNING;
import static com.elvaco.mvp.core.util.Dates.formatUtc;
import static com.elvaco.mvp.testing.fixture.UserTestData.dailyPlanetUser;
import static java.util.Arrays.asList;
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
    dailyPlanet = context().organisation2();
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
  public void superAdminCanGetSingleGateway() {
    UUID gatewayId = saveGateway(dailyPlanet.id).id;

    ResponseEntity<GatewayDto> response = asTestSuperAdmin()
      .get("/gateways/" + gatewayId, GatewayDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().id).isEqualTo(gatewayId);
  }

  @Test
  public void findGateways_WithUnknownCity() {
    Gateway gateway1 = saveGateway(dailyPlanet.id);
    Gateway gateway2 = saveGateway(dailyPlanet.id);

    LocationBuilder unknownLocation = unknownLocationBuilder();

    logicalMeters.save(newLogicalMeter(
      randomUUID(),
      "external-1234",
      asList(gateway1, gateway2),
      unknownLocation.build()
    ));

    Page<GatewayDto> content = asTestSuperAdmin()
      .getPage("/gateways?city=unknown,unknown", GatewayDto.class);

    assertThat(content.getContent()).hasSize(2);
  }

  @Test
  public void findGateways_WithUnknownAndKnownCity() {
    Gateway gateway1 = saveGateway(dailyPlanet.id);
    Gateway gateway2 = saveGateway(dailyPlanet.id);
    Gateway gateway3 = saveGateway(dailyPlanet.id);

    LocationBuilder unknownLocation = unknownLocationBuilder();

    logicalMeters.save(newLogicalMeter(
      randomUUID(),
      "external-1234",
      asList(gateway1, gateway2),
      unknownLocation.build()
    ));

    logicalMeters.save(newLogicalMeter(
      randomUUID(),
      "external-1235",
      singletonList(gateway3),
      new LocationBuilder()
        .country("sweden")
        .city("kungsbacka")
        .address("kabelgatan 1")
        .longitude(1.3333)
        .latitude(1.12345)
        .build()
    ));

    Page<GatewayDto> content = asTestSuperAdmin()
      .getPage("/gateways?city=unknown,unknown&city=sweden,kungsbacka", GatewayDto.class);

    assertThat(content.getContent()).hasSize(3);
  }

  @Test
  public void findGateways_WithCompleteAddressInfoButLowConfidence() {
    Gateway gateway1 = saveGateway(dailyPlanet.id);
    Gateway gateway2 = saveGateway(dailyPlanet.id);

    LocationBuilder unknownLocation = unknownLocationBuilder();

    UUID meterId1 = randomUUID();
    logicalMeters.save(newLogicalMeter(
      meterId1,
      "external-1234",
      singletonList(gateway1),
      unknownLocation.build()
    ));

    Location locationWithLowConfidence = new LocationBuilder()
      .country("sweden")
      .city("kungsbacka")
      .address("kabelgatan 1")
      .longitude(1.3333)
      .latitude(1.12345)
      .confidence(0.6)
      .build();

    UUID meterId2 = randomUUID();
    logicalMeters.save(newLogicalMeter(
      meterId2,
      "external-1235",
      singletonList(gateway2),
      locationWithLowConfidence
    ));

    Page<GatewayDto> content = asTestSuperAdmin()
      .getPage("/gateways?city=unknown,unknown&city=sweden,kungsbacka", GatewayDto.class);

    StatusLogEntry<UUID> status1 = gateway1.currentStatus();
    StatusLogEntry<UUID> status2 = gateway2.currentStatus();

    assertThat(content.getContent())
      .containsExactlyInAnyOrder(
        new GatewayDto(
          gateway1.id,
          gateway1.serial,
          gateway1.productModel,
          status1.status.name,
          formatUtc(status1.start),
          new LocationDto(
            new IdNamedDto("unknown"),
            new IdNamedDto("unknown"),
            new GeoPositionDto(1.234, 2.3323, 1.0)
          ),
          singletonList(meterId1),
          gateway1.organisationId
        ),
        new GatewayDto(
          gateway2.id,
          gateway2.serial,
          gateway2.productModel,
          status2.status.name,
          formatUtc(status2.start),
          new LocationDto(
            new IdNamedDto("kungsbacka"),
            new IdNamedDto("kabelgatan 1"),
            new GeoPositionDto(1.12345, 1.3333, 0.6)
          ),
          singletonList(meterId2),
          gateway2.organisationId
        )
      );
  }

  @Test
  public void findGateways_WithUnknownAddress() {
    Gateway gateway1 = saveGateway(dailyPlanet.id);
    Gateway gateway2 = saveGateway(dailyPlanet.id);
    Location unknownAddress = new LocationBuilder()
      .country("sweden")
      .city("kungsbacka")
      .longitude(1.3333)
      .latitude(1.12345)
      .build();

    logicalMeters.save(newLogicalMeter(
      randomUUID(),
      "external-1234",
      singletonList(gateway1),
      unknownAddress
    ));

    logicalMeters.save(newLogicalMeter(
      randomUUID(),
      "external-1235",
      singletonList(gateway2),
      new LocationBuilder()
        .country("sweden")
        .city("kungsbacka")
        .address("kabelgatan 1")
        .longitude(1.3333)
        .latitude(1.12345)
        .build()
    ));

    Page<GatewayDto> content = asTestSuperAdmin()
      .getPage("/gateways?address=unknown,unknown,unknown", GatewayDto.class);

    assertThat(content.getContent()).hasSize(1);
  }

  private LogicalMeter newLogicalMeter(
    UUID meterId,
    String externalId,
    List<Gateway> gateway,
    Location location
  ) {
    return new LogicalMeter(
      meterId,
      externalId,
      dailyPlanet.id,
      location,
      ZonedDateTime.now(),
      emptyList(),
      MeterDefinition.UNKNOWN_METER,
      gateway,
      null,
      emptyList(),
      null
    );
  }

  private void assertGatewayStatus(
    GatewayDto actualGateway,
    Gateway expectedGateway,
    StatusType expectedStatus
  ) {
    assertThat(actualGateway.id)
      .as("Unexpected gateway id")
      .isEqualTo(expectedGateway.id);

    assertThat(actualGateway.status)
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

  private static LocationBuilder unknownLocationBuilder() {
    return new LocationBuilder()
      .latitude(1.234)
      .longitude(2.3323);
  }
}
