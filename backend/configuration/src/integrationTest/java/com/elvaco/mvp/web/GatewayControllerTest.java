package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.database.entity.gateway.GatewayStatusLogEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import com.elvaco.mvp.testdata.IdStatus;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.web.dto.GatewayDto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static com.elvaco.mvp.core.domainmodels.StatusType.ERROR;
import static com.elvaco.mvp.core.domainmodels.StatusType.OK;
import static com.elvaco.mvp.core.domainmodels.StatusType.WARNING;
import static com.elvaco.mvp.testing.fixture.LocationTestData.kungsbacka;
import static com.elvaco.mvp.testing.fixture.UserTestData.dailyPlanetUser;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SuppressWarnings("rawtypes")
public class GatewayControllerTest extends IntegrationTest {

  private Organisation dailyPlanet;

  @Before
  public void setUp() {
    dailyPlanet = context().organisation2();
  }

  @After
  public void tearDown() {
    physicalMeterJpaRepository.deleteAll();
    gatewayStatusLogJpaRepository.deleteAll();
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
      gateway1.id,
      OK,
      date.minusDays(90),
      date.minusDays(30)
    );

    saveGatewayStatus(
      gateway1.id,
      WARNING,
      date.minusDays(30),
      null
    );

    saveGatewayStatus(
      gateway2.id,
      OK,
      date.minusDays(90),
      null
    );

    Page<GatewayDto> response = asSuperAdmin()
      .getPage("/gateways" + "?after=" + date.minusDays(30) + "&before=" + date, GatewayDto.class);

    assertThat(response.getTotalElements()).isEqualTo(2);
    assertThat(response.getNumberOfElements()).isEqualTo(2);
    assertThat(response.getTotalPages()).isEqualTo(1);

    List<IdStatus> gatewayIds = response.getContent().stream()
      .map(gateway -> new IdStatus(gateway.id, gateway.status))
      .collect(toList());

    assertThat(gatewayIds).containsExactlyInAnyOrder(
      new IdStatus(gateway1.id, WARNING.name),
      new IdStatus(gateway2.id, OK.name)
    );
  }

  @Test
  public void fetchGatewayAndStatusForPreviousMonth() {
    Gateway gateway1 = saveGateway(dailyPlanet.id);
    Gateway gateway2 = saveGateway(dailyPlanet.id);

    ZonedDateTime date = ZonedDateTime.parse("2001-04-01T00:00:00.00Z");

    saveGatewayStatus(
      gateway1.id,
      OK,
      date.minusDays(90),
      date.minusDays(30)
    );

    saveGatewayStatus(
      gateway1.id,
      WARNING,
      date.minusDays(30),
      null
    );

    saveGatewayStatus(
      gateway2.id,
      OK,
      date.minusDays(90),
      null
    );

    Page<GatewayDto> response = asSuperAdmin()
      .getPage(
        "/gateways"
          + "?after=" + date.minusDays(60)
          + "&before=" + date.minusDays(30),
        GatewayDto.class
      );

    assertThat(response.getTotalElements()).isEqualTo(2);
    assertThat(response.getNumberOfElements()).isEqualTo(2);
    assertThat(response.getTotalPages()).isEqualTo(1);

    List<IdStatus> gatewayIds = response.getContent().stream()
      .map(gw -> new IdStatus(gw.id, gw.status))
      .collect(toList());

    assertThat(gatewayIds).containsExactlyInAnyOrder(
      new IdStatus(gateway1.id, OK.name),
      new IdStatus(gateway2.id, OK.name)
    );
  }

  @Test
  public void fetchAllGatewaysShouldBeEmptyWhenNoGatewaysExists() {
    Page<GatewayDto> response = asUser()
      .getPage("/gateways", GatewayDto.class);

    assertThat(response.getTotalElements()).isEqualTo(0);
    assertThat(response.getNumberOfElements()).isEqualTo(0);
    assertThat(response.getTotalPages()).isEqualTo(0);
  }

  @Test
  public void superAdminsCanListAllGateways() {
    saveGateway(dailyPlanet.id);
    saveGateway(dailyPlanet.id);
    saveGateway(context().organisationId());

    Page<GatewayDto> response = asSuperAdmin()
      .getPage("/gateways", GatewayDto.class);

    assertThat(response.getTotalElements()).isEqualTo(3);
    assertThat(response.getNumberOfElements()).isEqualTo(3);
    assertThat(response.getTotalPages()).isEqualTo(1);
  }

  @Test
  public void otherUsersCannotFetchGatewaysFromOtherOrganisations() {
    saveGateway(context().organisationId());

    Page<GatewayDto> response = restAsUser(dailyPlanetUser(dailyPlanet))
      .getPage("/gateways", GatewayDto.class);

    assertThat(response.getTotalElements()).isEqualTo(0L);
    assertThat(response.getNumberOfElements()).isEqualTo(0);
    assertThat(response.getTotalPages()).isEqualTo(0);
  }

  @Test
  public void userCanOnlyListGatewaysWithinSameOrganisation() {
    Gateway g1 = saveGateway(dailyPlanet.id);
    Gateway g2 = saveGateway(dailyPlanet.id);
    saveGateway(context().organisationId());

    Page<GatewayDto> response = as(dailyPlanetUser(dailyPlanet))
      .getPage("/gateways", GatewayDto.class);

    List<UUID> gatewayIds = response.getContent().stream()
      .map(g -> g.id)
      .collect(toList());

    assertThat(gatewayIds).containsExactlyInAnyOrder(g1.id, g2.id);
  }

  @Test
  public void superAdminCanGetSingleGateway() {
    UUID gatewayId = saveGateway(dailyPlanet.id).id;

    ResponseEntity<GatewayDto> response = asSuperAdmin()
      .get("/gateways/" + gatewayId, GatewayDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().id).isEqualTo(gatewayId);
  }

  @Test
  public void findGateways_WithSerial() {
    gateways.save(Gateway.builder()
      .organisationId(dailyPlanet.id)
      .serial("1")
      .productModel("elv").build());

    gateways.save(Gateway.builder()
      .organisationId(dailyPlanet.id)
      .serial("2")
      .productModel("elv").build());

    Page<GatewayDto> content = asSuperAdmin()
      .getPage("/gateways?gatewaySerial=1", GatewayDto.class);

    assertThat(content.getContent()).hasSize(1);
    assertThat(content.getContent().get(0).serial).isEqualTo("1");
  }

  @Test
  public void findGateways_WithId() {
    Gateway gateway1 = saveGateway(dailyPlanet.id);
    Gateway gateway2 = saveGateway(dailyPlanet.id);

    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("external-1234")
      .organisationId(dailyPlanet.id)
      .gateways(asList(gateway1, gateway2))
      .build());

    Page<GatewayDto> content = asSuperAdmin()
      .getPage("/gateways?id=" + gateway1.id.toString(), GatewayDto.class);

    assertThat(content.getContent())
      .extracting("id")
      .containsExactly(gateway1.id);
  }

  @Test
  public void findGateways_WithGatewayId() {
    Gateway gateway1 = saveGateway(dailyPlanet.id);
    Gateway gateway2 = saveGateway(dailyPlanet.id);

    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("external-1234")
      .organisationId(dailyPlanet.id)
      .gateways(asList(gateway1, gateway2))
      .build());

    Page<GatewayDto> content = asSuperAdmin()
      .getPage("/gateways?gatewayId=" + gateway1.id.toString(), GatewayDto.class);

    assertThat(content.getContent())
      .extracting("id")
      .containsExactly(gateway1.id);
  }

  @Test
  public void findGateways_WithUnknownCity() {
    Gateway gateway1 = saveGateway(dailyPlanet.id);
    Gateway gateway2 = saveGateway(dailyPlanet.id);

    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("external-1234")
      .organisationId(dailyPlanet.id)
      .gateways(asList(gateway1, gateway2))
      .location(UNKNOWN_LOCATION)
      .build());

    Page<GatewayDto> content = asSuperAdmin()
      .getPage("/gateways?city=unknown,unknown", GatewayDto.class);

    assertThat(content.getContent()).hasSize(2);
  }

  @Test
  public void findGateways_WithUnknownAndKnownCity() {
    Gateway gateway1 = saveGateway(dailyPlanet.id);
    Gateway gateway2 = saveGateway(dailyPlanet.id);
    Gateway gateway3 = saveGateway(dailyPlanet.id);

    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("external-1234")
      .organisationId(dailyPlanet.id)
      .gateways(asList(gateway1, gateway2))
      .location(UNKNOWN_LOCATION)
      .build());

    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("external-1235")
      .organisationId(dailyPlanet.id)
      .gateways(singletonList(gateway3))
      .location(kungsbacka().build())
      .build());

    Page<GatewayDto> content = asSuperAdmin()
      .getPage("/gateways?city=unknown,unknown&city=sverige,kungsbacka", GatewayDto.class);

    assertThat(content.getContent()).hasSize(3);
  }

  @Test
  public void findGateways_WithCompleteAddressInfoButLowConfidence() {
    double lowConfidence = 0.6;

    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("external-1235")
      .organisationId(dailyPlanet.id)
      .gateways(singletonList(saveGateway(dailyPlanet.id)))
      .location(kungsbacka().confidence(lowConfidence).build())
      .build());

    Page<GatewayDto> content = asSuperAdmin()
      .getPage("/gateways?city=sverige,kungsbacka", GatewayDto.class);

    assertThat(content.getContent()).hasSize(1);
  }

  @Test
  public void findGateways_WithErrorReportedMetersWithinPeriod() {
    Gateway gateway1 = saveGateway(dailyPlanet.id);
    LogicalMeter logicalMeter = logicalMeters.save(
      LogicalMeter.builder()
        .id(randomUUID())
        .externalId("external-1234")
        .organisationId(dailyPlanet.id)
        .gateway(gateway1)
        .build()
    );
    PhysicalMeter physicalMeter = physicalMeters.save(
      physicalMeterBuilder()
        .organisation(dailyPlanet)
        .logicalMeterId(logicalMeter.id)
        .build()
    );

    ZonedDateTime time = ZonedDateTime.parse("2017-01-01T00:00:00Z");
    savePhysicalMeterStatus(physicalMeter.id, ERROR, time.minusDays(1), null);

    Page<GatewayDto> page = asSuperAdmin()
      .getPage(
        Url.builder()
          .path("/gateways")
          .parameter(RequestParameter.BEFORE, time)
          .parameter(RequestParameter.AFTER, time.minusDays(2))
          .parameter(RequestParameter.REPORTED, ERROR)
          .build(),
        GatewayDto.class
      );

    List<GatewayDto> content = page.getContent();
    assertThat(content).hasSize(1);
  }

  @Test
  public void findGateways_WithoutErrorReportedMetersWithinPeriod() {
    Gateway gateway1 = saveGateway(dailyPlanet.id);
    LogicalMeter logicalMeter = logicalMeters.save(
      LogicalMeter.builder()
        .id(randomUUID())
        .externalId("external-1234")
        .organisationId(dailyPlanet.id)
        .gateway(gateway1)
        .build()
    );
    PhysicalMeter physicalMeter = physicalMeters.save(
      physicalMeterBuilder()
        .organisation(dailyPlanet)
        .logicalMeterId(logicalMeter.id)
        .build()
    );

    ZonedDateTime time = ZonedDateTime.parse("2017-01-01T00:00:00Z");
    savePhysicalMeterStatus(physicalMeter.id, OK, time.minusDays(1), null);

    Page<GatewayDto> page = asSuperAdmin()
      .getPage(
        Url.builder()
          .path("/gateways")
          .parameter(RequestParameter.BEFORE, time)
          .parameter(RequestParameter.AFTER, time.minusDays(2))
          .parameter(RequestParameter.REPORTED, ERROR)
          .build(),
        GatewayDto.class
      );

    List<GatewayDto> content = page.getContent();
    assertThat(content).hasSize(0);
  }

  @Test
  public void findGateways_WithUnknownAddress() {
    Gateway gateway1 = saveGateway(dailyPlanet.id);
    Gateway gateway2 = saveGateway(dailyPlanet.id);
    Location unknownAddress = kungsbacka().address(null).build();

    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("external-1234")
      .organisationId(dailyPlanet.id)
      .gateway(gateway1)
      .location(unknownAddress)
      .build());

    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("external-1235")
      .organisationId(dailyPlanet.id)
      .gateway(gateway2)
      .location(kungsbacka().build())
      .build());

    Page<GatewayDto> content = asSuperAdmin()
      .getPage("/gateways?address=unknown,unknown,unknown", GatewayDto.class);

    assertThat(content.getContent()).hasSize(1);
  }

  @Test
  public void wildcardSearchMatchesSerialStart() {
    gateways.save(Gateway.builder()
      .organisationId(context().organisationId())
      .serial("123456")
      .productModel("product-model")
      .build());

    Page<GatewayDto> page = asUser()
      .getPage("/gateways?w=1234", GatewayDto.class);

    assertThat(page).hasSize(1);
    GatewayDto gateway = page.getContent().get(0);
    assertThat(gateway.serial).isEqualTo("123456");
  }

  @Test
  public void wildcardSearchMatchesCityStart() {
    Gateway gateway = gateways.save(Gateway.builder()
      .organisationId(context().organisationId())
      .serial("123456")
      .productModel("product-model")
      .build());

    logicalMeters.save(LogicalMeter.builder()
      .externalId("external-id")
      .organisationId(dailyPlanet.id)
      .gateway(gateway)
      .location(kungsbacka().build())
      .build());
    Page<GatewayDto> page = asUser()
      .getPage("/gateways?w=kungsb", GatewayDto.class);

    assertThat(page).extracting("location.city").containsExactly("kungsbacka");
  }

  @Test
  public void wildcardSearchMatchesCityStart_caseInsensitive() {
    Gateway gateway = gateways.save(Gateway.builder()
      .organisationId(context().organisationId())
      .serial("123456")
      .productModel("product-model")
      .build());

    logicalMeters.save(LogicalMeter.builder()
      .externalId("external-id")
      .organisationId(dailyPlanet.id)
      .gateway(gateway)
      .location(kungsbacka().build())
      .build());
    Page<GatewayDto> page = asUser()
      .getPage("/gateways?w=Kungsb", GatewayDto.class);

    assertThat(page)
      .extracting("location.city")
      .containsExactly("kungsbacka");
  }

  @Test
  public void wildcardSearchMatchesAddressStart() {
    Gateway gateway = gateways.save(Gateway.builder()
      .organisationId(context().organisationId())
      .serial("123456")
      .productModel("product-model")
      .build());

    logicalMeters.save(LogicalMeter.builder()
      .externalId("external-id")
      .organisationId(dailyPlanet.id)
      .gateway(gateway)
      .location(kungsbacka().address("teknikgatan 2t").build())
      .build());
    Page<GatewayDto> page = asUser()
      .getPage("/gateways?w=tekni", GatewayDto.class);

    assertThat(page)
      .extracting("location.address", "location.city")
      .containsExactly(tuple("teknikgatan 2t", "kungsbacka"));
  }

  @Test
  public void wildcardSearchMatchesAddressStart_caseInsensitive() {
    Gateway gateway = gateways.save(Gateway.builder()
      .organisationId(context().organisationId())
      .serial("123456")
      .productModel("product-model")
      .build());

    logicalMeters.save(LogicalMeter.builder()
      .externalId("external-id")
      .organisationId(dailyPlanet.id)
      .gateway(gateway)
      .location(kungsbacka().build())
      .build());
    Page<GatewayDto> page = asUser()
      .getPage("/gateways?w=Kabel", GatewayDto.class);

    assertThat(page)
      .extracting("location.address", "location.city")
      .containsExactly(tuple("kabelgatan 1", "kungsbacka"));
  }

  @Test
  public void wildcardSearchMatchesProductTypeStart() {
    gateways.save(Gateway.builder()
      .organisationId(context().organisationId())
      .serial("123456")
      .productModel("CMe3100")
      .build());

    Page<GatewayDto> page = asUser()
      .getPage("/gateways?w=CMe3", GatewayDto.class);

    assertThat(page).hasSize(1);
  }

  @Test
  public void wildcardSearchDoesNotReturnNonMatches() {
    gateways.save(Gateway.builder()
      .organisationId(context().organisationId())
      .serial("123456")
      .productModel("product-model")
      .build());

    gateways.save(Gateway.builder()
      .organisationId(context().organisationId())
      .serial("789")
      .productModel("product-model")
      .build());

    Page<GatewayDto> page = asUser()
      .getPage("/gateways?w=1234", GatewayDto.class);

    assertThat(page).hasSize(1);
    GatewayDto gateway = page.getContent().get(0);
    assertThat(gateway.serial).isEqualTo("123456");
  }

  private PhysicalMeter.PhysicalMeterBuilder physicalMeterBuilder() {
    return PhysicalMeter.builder()
      .address(randomUUID().toString())
      .externalId(randomUUID().toString());
  }

  private Gateway saveGateway(UUID organisationId) {
    return gateways.save(Gateway.builder()
      .organisationId(organisationId)
      .serial(randomUUID().toString())
      .productModel(randomUUID().toString())
      .build());
  }

  private void saveGatewayStatus(
    UUID gatewayId,
    StatusType status,
    ZonedDateTime start,
    @Nullable ZonedDateTime stop
  ) {
    gatewayStatusLogJpaRepository.save(
      new GatewayStatusLogEntity(
        null,
        gatewayId,
        status,
        start,
        stop
      )
    );
  }

  private void savePhysicalMeterStatus(
    UUID physicalMeterId,
    StatusType status,
    ZonedDateTime start,
    @Nullable ZonedDateTime stop
  ) {
    physicalMeterStatusLogJpaRepository.save(
      new PhysicalMeterStatusLogEntity(
        null,
        physicalMeterId,
        status,
        start,
        stop
      )
    );
  }
}
