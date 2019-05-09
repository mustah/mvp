package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PeriodBound;
import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.testdata.UrlTemplate;
import com.elvaco.mvp.web.dto.AlarmDto;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.PagedLogicalMeterDto;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.After;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DEFAULT_DISTRICT_HEATING;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DEFAULT_GAS;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DEFAULT_HOT_WATER;
import static com.elvaco.mvp.core.domainmodels.StatusType.ERROR;
import static com.elvaco.mvp.core.domainmodels.StatusType.OK;
import static com.elvaco.mvp.core.spi.data.RequestParameter.ALARM;
import static com.elvaco.mvp.core.spi.data.RequestParameter.GATEWAY_SERIAL;
import static com.elvaco.mvp.core.spi.data.RequestParameter.MEDIUM;
import static com.elvaco.mvp.core.spi.data.RequestParameter.SECONDARY_ADDRESS;
import static com.elvaco.mvp.testing.fixture.LocationTestData.kungsbacka;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class LogicalMeterControllerTest extends IntegrationTest {

  @After
  public void tearDown() {
    measurementJpaRepository.deleteAll();
    meterAlarmLogJpaRepository.deleteAll();
    gatewayStatusLogJpaRepository.deleteAll();
    gatewayJpaRepository.deleteAll();
  }

  @Test
  public void pagedMeter_Has_Location() {
    given(logicalMeter().location(kungsbacka().build()));

    PagedLogicalMeterDto logicalMeterDto = asUser()
      .getPage("/meters", PagedLogicalMeterDto.class)
      .getContent()
      .get(0);

    assertThat(logicalMeterDto.location.city).isEqualTo("kungsbacka");
    assertThat(logicalMeterDto.location.address).isEqualTo("kabelgatan 1");
  }

  @Test
  public void pagedMeter_Has_MeterDefinition() {
    given(logicalMeter().meterDefinition(DEFAULT_DISTRICT_HEATING));

    PagedLogicalMeterDto logicalMeterDto = asUser()
      .getPage("/meters", PagedLogicalMeterDto.class)
      .getContent()
      .get(0);

    assertThat(logicalMeterDto.medium).isEqualTo(DEFAULT_DISTRICT_HEATING.medium.name);
  }

  @Test
  public void pagedMeter_Has_ReadInterval() {
    given(physicalMeter().readIntervalMinutes(42L));

    var logicalMeterDto = asUser()
      .getPage("/meters", PagedLogicalMeterDto.class)
      .getContent();

    assertThat(logicalMeterDto)
      .extracting(m -> m.readIntervalMinutes)
      .containsExactly(42L);
  }

  @Test
  public void pagedMeter_Has_Manufacturer() {
    given(physicalMeter().manufacturer("KAKA"));

    var logicalMeterDto = asUser()
      .getPage("/meters", PagedLogicalMeterDto.class)
      .getContent();

    assertThat(logicalMeterDto)
      .extracting(m -> m.manufacturer)
      .containsExactly("KAKA");
  }

  @Test
  public void pagedMeter_Has_Gateway() {
    var meter = given(logicalMeter());

    given(gateway().serial("gateway-serial").meter(meter));

    PagedLogicalMeterDto logicalMeterDto = asUser()
      .getPage("/meters", PagedLogicalMeterDto.class)
      .getContent()
      .get(0);

    assertThat(logicalMeterDto.gatewaySerial).isEqualTo("gateway-serial");
  }

  @Test
  public void pagedMeter_Has_LatestActive_PhysicalMeter() {
    given(
      logicalMeter(),
      physicalMeter()
        .address("1")
        .activePeriod(PeriodRange.halfOpenFrom(context().yesterday(), context().now())),
      physicalMeter()
        .address("2")
        .activePeriod(PeriodRange.halfOpenFrom(context().now(), null))
    );

    List<PagedLogicalMeterDto> meters = asUser()
      .getPage(
        Url.builder()
          .path("/meters")
          // TODO this should not use report period or threshold period
          .reportPeriod(context().yesterday().minusDays(5), context().now())
          .build(),
        PagedLogicalMeterDto.class
      )
      .getContent();

    assertThat(meters)
      .extracting(m -> m.address)
      .containsExactly("2");
  }

  @Test
  public void pagedMeter_EnforcesPagination_Size() {
    given(logicalMeter());
    given(logicalMeter());
    given(logicalMeter());

    Page<PagedLogicalMeterDto> response = asUser()
      .getPage("/meters?size=1", PagedLogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(3);
    assertThat(response.getNumberOfElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(3);
  }

  @Test
  public void pagedMeter_EnforcesPagination_SizeAndPage() {
    given(logicalMeter());
    given(logicalMeter());
    given(logicalMeter());

    Page<PagedLogicalMeterDto> response = asUser()
      .getPage("/meters?page=0&size=2", PagedLogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(3);
    assertThat(response.getNumberOfElements()).isEqualTo(2);
    assertThat(response.getTotalPages()).isEqualTo(2);
  }

  @Test
  public void pagedMeter_By_Medium() {
    given(logicalMeter().meterDefinition(DEFAULT_HOT_WATER));
    given(logicalMeter().meterDefinition(DEFAULT_GAS));

    Page<PagedLogicalMeterDto> response = asUser()
      .getPage("/meters?medium=Hot+water", PagedLogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(1);
  }

  @Test
  public void pagedMeter_By_Organisation() {
    given(logicalMeter());

    Page<PagedLogicalMeterDto> response = asUser()
      .getPage("/meters?organisation=" + context().organisationId(), PagedLogicalMeterDto.class);

    assertThat(response.getTotalElements()).isGreaterThanOrEqualTo(1L);
  }

  @Test
  public void pagedMeter_By_Organisation_RequiresSameOrganisation() {
    UUID otherOrganisation = given(organisation()).getId();

    given(logicalMeter().organisationId(otherOrganisation));

    Page<PagedLogicalMeterDto> response = asUser()
      .getPage("/meters?organisation=" + otherOrganisation, PagedLogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(0L);
  }

  @Test
  public void pagedMeter_By_Medium_RequiresSameOrganisation() {
    var myMeter = given(logicalMeter().meterDefinition(DEFAULT_GAS));
    UUID organisationId = given(organisation()).getId();
    given(logicalMeter().meterDefinition(DEFAULT_GAS)
      .organisationId(organisationId));

    Page<PagedLogicalMeterDto> response = asUser()
      .getPage(
        Url.builder()
          .path("/meters")
          .parameter(MEDIUM, DEFAULT_GAS.medium.name)
          .build(),
        PagedLogicalMeterDto.class
      );

    assertThat(response.getContent())
      .extracting(m -> m.id)
      .containsExactly(myMeter.id);
  }

  @Test
  public void findAllMeters_WithGatewaySerial() {
    String serial = "666";

    var interestingMeter = given(logicalMeter().externalId(serial));
    var uninterestingMeter = given(logicalMeter().externalId("777"));

    given(gateway().serial(serial).meter(interestingMeter));
    given(gateway().serial("777").meter(uninterestingMeter));

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage(
        Url.builder()
          .path("/meters")
          .parameter(GATEWAY_SERIAL, serial)
          .build(),
        PagedLogicalMeterDto.class
      );

    assertThat(result.getContent())
      .extracting(m -> m.gatewaySerial, m -> m.facility)
      .containsExactly(tuple(serial, serial));
  }

  @Test
  public void findAllMeters_WithFacility() {
    String facility = "1";
    given(logicalMeter().externalId(facility));
    given(logicalMeter().externalId("2"));

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage("/meters?facility=" + facility, PagedLogicalMeterDto.class);

    assertThat(result.getContent())
      .extracting(m -> m.facility)
      .containsExactly(facility);
  }

  @Test
  public void findAllMeters_WithSecondaryAddress() {
    String address = "1";
    given(physicalMeter().address(address));
    given(physicalMeter().address("2"));

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage(
        Url.builder()
          .path("/meters")
          .parameter(SECONDARY_ADDRESS, address)
          .build(),
        PagedLogicalMeterDto.class
      );

    assertThat(result.getContent())
      .extracting(m -> m.address)
      .containsExactly(address);
  }

  @Test
  public void findAllMeters_WithUnknownCity() {
    given(logicalMeter().location(UNKNOWN_LOCATION));
    given(logicalMeter().location(kungsbacka().build()));

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage("/meters?city=unknown,unknown", PagedLogicalMeterDto.class);

    assertThat(result.getContent()).hasSize(1);
  }

  @Test
  public void findAllMeters_IncludeMetersWith_UnknownCity() {
    given(logicalMeter().location(UNKNOWN_LOCATION));
    given(logicalMeter().location(kungsbacka().build()));

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage("/meters?city=unknown,unknown&city=sverige,kungsbacka", PagedLogicalMeterDto.class);

    assertThat(result.getContent()).hasSize(2);
  }

  @Test
  public void findAllMeters_IncludeMetersWith_UnknownCity_AndLowConfidence() {
    given(logicalMeter().location(UNKNOWN_LOCATION));
    given(logicalMeter().location(kungsbacka().build()));
    given(logicalMeter().location(kungsbacka().confidence(0.74).build()));

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage("/meters?city=unknown,unknown&city=sverige,kungsbacka", PagedLogicalMeterDto.class);

    assertThat(result.getContent()).hasSize(3);
  }

  @Test
  public void findAllMeters_UnknownCity_NotLowConfidence() {
    given(logicalMeter().externalId("123").location(UNKNOWN_LOCATION));
    given(logicalMeter().externalId("123-123-123").location(kungsbacka().build()));
    given(logicalMeter().externalId("456").location(kungsbacka().confidence(0.74).build()));
    given(logicalMeter().externalId("789").location(
      kungsbacka()
        .longitude(null)
        .latitude(null)
        .confidence(null)
        .build()
    ));

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage("/meters?city=unknown,unknown", PagedLogicalMeterDto.class);

    assertThat(result.getContent())
      .extracting(m -> m.facility)
      .containsExactly("123");
  }

  @Test
  public void findAllMeters_WithManufacturer() {
    given(physicalMeter().manufacturer("KAM"));
    given(physicalMeter().manufacturer("ELV"));

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage("/meters?manufacturer=ELV", PagedLogicalMeterDto.class);

    assertThat(result.getContent())
      .extracting(m -> m.manufacturer)
      .containsExactly("ELV");
  }

  @Test
  public void findAllMeters_WithId() {
    UUID id1 = given(logicalMeter()).id;
    given(logicalMeter());

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage("/meters?id=" + id1, PagedLogicalMeterDto.class);

    assertThat(result.getContent())
      .extracting(m -> m.id)
      .containsExactly(id1);
  }

  @Test
  public void findAllMeters_WithLogicalMeterId() {
    UUID id1 = given(logicalMeter()).id;
    given(logicalMeter());

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage("/meters?logicalMeterId=" + id1, PagedLogicalMeterDto.class);

    assertThat(result.getContent())
      .extracting(m -> m.id)
      .containsExactly(id1);
  }

  @Test
  public void findAllMeters_WithUnknownAddress() {
    given(logicalMeter().externalId("abc").location(UNKNOWN_LOCATION));
    given(logicalMeter().externalId("123").location(kungsbacka().confidence(0.75).build()));
    given(logicalMeter().externalId("456").location(kungsbacka().confidence(0.80).build()));
    given(logicalMeter().externalId("789")
      .location(kungsbacka().confidence(null).longitude(null).latitude(null).build()));

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage("/meters?address=unknown,unknown,unknown", PagedLogicalMeterDto.class);

    assertThat(result.getContent())
      .extracting(m -> m.facility)
      .containsExactly("abc");
  }

  @Test
  public void findAllMetersPaged_WithOrganisationAsUser() {
    given(logicalMeter());

    UUID otherOrganisation = given(organisation()).getId();
    given(logicalMeter().organisationId(otherOrganisation));

    var allMeters = asUser().getPage("/meters", PagedLogicalMeterDto.class);

    assertThat(allMeters.getContent())
      .extracting(m -> m.organisationId)
      .containsExactly(context().organisationId());

    asUser()
      .getPage(
        "/meters?organisation={id}",
        PagedLogicalMeterDto.class,
        otherOrganisation
      );

    assertThat(allMeters.getContent())
      .as("The requested organisation was ignored, and implicitly replaced with the user's")
      .extracting(m -> m.organisationId)
      .containsExactly(context().organisationId());
  }

  @Test
  public void findAllMetersPaged_WithOrganisationAsSuperAdmin() {
    given(logicalMeter());

    UUID otherOrganisation = given(organisation()).getId();
    given(logicalMeter().organisationId(otherOrganisation));

    var allMeters = asSuperAdmin().getPage("/meters", PagedLogicalMeterDto.class);

    assertThat(allMeters.getContent()).hasSize(2);

    var oneOrganisation = asSuperAdmin()
      .getPage(
        "/meters?organisation={id}",
        PagedLogicalMeterDto.class,
        otherOrganisation
      );

    assertThat(oneOrganisation.getContent())
      .hasSize(1)
      .extracting(m -> m.organisationId)
      .containsExactly(otherOrganisation);
  }

  @Test
  public void findAllMeters_FindsActivePhysicalMeter() {
    given(
      logicalMeter(),
      physicalMeter()
        .address("1")
        .activePeriod(PeriodRange.halfOpenFrom(
          context().now().minusDays(5),
          context().now().minusDays(2)
        )),
      physicalMeter()
        .address("2")
        .activePeriod(PeriodRange.from(PeriodBound.inclusiveOf(context().now().minusDays(2))))
    );

    Page<PagedLogicalMeterDto> oldPeriod = asUser()
      .getPage(
        Url.builder().path("/meters")
          // TODO should not use period
          .thresholdPeriod(context().now().minusDays(5), context().now().minusDays(3))
          .build(),
        PagedLogicalMeterDto.class
      );

    Page<PagedLogicalMeterDto> currentPeriod = asUser()
      .getPage(
        Url.builder().path("/meters")
          // TODO should not use period
          .thresholdPeriod(context().now().minusDays(1), context().now())
          .build(),
        PagedLogicalMeterDto.class
      );

    assertSoftly(softly -> {
      softly.assertThat(oldPeriod.getContent()).extracting(m -> m.address).contains("1");
      softly.assertThat(currentPeriod.getContent()).extracting(m -> m.address).contains("2");
    });
  }

  @Test
  public void userCannotRemoveLogicalMeter() {
    var meter = given(logicalMeter());
    given(measurementSeries()
      .forMeter(meter)
      .withQuantity(Quantity.POWER)
      .startingAt(context().now())
      .withValues(1));

    ResponseEntity<ErrorMessageDto> response = asUser()
      .delete("/meters/" + meter.id, ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    assertNothingIsRemoved(meter);
  }

  @Test
  public void removingLogicalMeter_ShouldNotLeakInformation() {
    ResponseEntity<ErrorMessageDto> response = asUser()
      .delete("/meters/" + randomUUID(), ErrorMessageDto.class);

    assertThat(response.getStatusCode())
      .as("Test that we don't leak \"Meter not found\"")
      .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  public void adminCannotRemoveLogicalMeter() {
    var meter = given(logicalMeter());
    given(measurementSeries()
      .forMeter(meter)
      .withQuantity(Quantity.POWER)
      .startingAt(context().now())
      .withValues(1));

    ResponseEntity<ErrorMessageDto> response = asAdmin()
      .delete("/meters/" + meter.id, ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    assertNothingIsRemoved(meter);
  }

  @Test
  public void superAdminCanRemoveLogicalMeter() {
    var logicalMeter = given(
      logicalMeter(),
      physicalMeter().activePeriod(PeriodRange.from(context().now()))
    );

    given(measurementSeries()
      .forMeter(logicalMeter)
      .withQuantity(Quantity.POWER)
      .startingAt(context().now())
      .withValues(1));

    ResponseEntity<LogicalMeterDto> response = asSuperAdmin()
      .delete("/meters/" + logicalMeter.id, LogicalMeterDto.class);

    assertSoftly(softly -> {
      softly.assertThat(response.getStatusCode())
        .isEqualTo(HttpStatus.OK);

      softly.assertThat(logicalMeters.findById(logicalMeter.id))
        .isEmpty();

      softly.assertThat(physicalMeterJpaRepository.findById(logicalMeter
        .activePhysicalMeter()
        .get().id))
        .isEmpty();

      softly.assertThat(measurementJpaRepository.findAll())
        .isEmpty();
    });
  }

  @Test
  public void superAdminRemoveNonExistingLogicalMeter() {
    ResponseEntity<ErrorMessageDto> response = asSuperAdmin()
      .delete("/meters/" + randomUUID(), ErrorMessageDto.class);

    assertThatStatusIsNotFound(response);
  }

  @Test
  public void nullFieldsAreNotIncludedInDto() {
    var meterOk = given(physicalMeter().manufacturer("manu"));
    JsonNode jsonOk = asUser().getJson("/meters?id=" + meterOk.id);

    var meterNull = given(physicalMeter().manufacturer(null));
    JsonNode jsonNull = asUser().getJson("/meters?id=" + meterNull.id);

    assertSoftly(softly -> {
      softly.assertThat(jsonOk.get("content").get(0).has("manufacturer"))
        .as(jsonOk.toString())
        .isTrue();

      softly.assertThat(jsonNull.get("content").get(0).has("manufacturer"))
        .as(jsonNull.toString())
        .isFalse();
    });
  }

  @Test
  public void wildcardSearchMatchesFacilityStart() {
    given(logicalMeter().externalId("abcdef"));

    Page<PagedLogicalMeterDto> page = asUser().getPage(
      "/meters?w=abc",
      PagedLogicalMeterDto.class
    );

    assertThat(page.getContent())
      .extracting(m -> m.facility)
      .containsExactly("abcdef");
  }

  @Test
  public void wildcardSearchMatchesCityStart() {
    String city = "abcdef";
    given(logicalMeter().location(new LocationBuilder().city(city).build()));

    Page<PagedLogicalMeterDto> page = asUser().getPage(
      "/meters?w=abc",
      PagedLogicalMeterDto.class
    );

    assertThat(page)
      .extracting(m -> m.location.city)
      .containsExactly(city);
  }

  @Test
  public void wildcardSearchMatchesCityStart_caseInsensitive() {
    String city = "abcdef";
    given(logicalMeter().location(new LocationBuilder().city(city).build()));

    Page<PagedLogicalMeterDto> page = asUser().getPage(
      "/meters?w=ABC",
      PagedLogicalMeterDto.class
    );

    assertThat(page)
      .extracting(m -> m.location.city)
      .containsExactly(city);
  }

  @Test
  public void wildcardSearchMatchesAddressStart() {
    given(logicalMeter().location(kungsbacka().address("storgatan").build()));
    given(logicalMeter().location(kungsbacka().address("lillgatan").build()));

    Page<PagedLogicalMeterDto> page = asUser().getPage(
      "/meters?w=storgat",
      PagedLogicalMeterDto.class
    );

    assertThat(page)
      .extracting(m -> m.location.address)
      .containsExactly("storgatan");
  }

  @Test
  public void wildcardSearchMatchesAddressStart_caseInsensitive() {
    given(logicalMeter().location(kungsbacka().address("storgatan").build()));
    given(logicalMeter().location(kungsbacka().address("lillgatan").build()));

    Page<PagedLogicalMeterDto> page = asUser().getPage(
      "/meters?w=Storgat",
      PagedLogicalMeterDto.class
    );

    assertThat(page)
      .extracting(m -> m.location.address)
      .containsExactly("storgatan");
  }

  @Test
  public void wildcardSearchMatchesManufacturerStart() {
    given(physicalMeter().manufacturer("elvaco"));
    given(physicalMeter().manufacturer("kamstrup"));

    Page<PagedLogicalMeterDto> page = asUser().getPage(
      "/meters?w=elv",
      PagedLogicalMeterDto.class
    );

    assertThat(page.getContent())
      .extracting(m -> m.manufacturer)
      .containsExactly("elvaco");
  }

  @Test
  public void wildcardSearchMatchesMediumStart_IgnoresCase() {
    given(logicalMeter().meterDefinition(DEFAULT_HOT_WATER));
    given(logicalMeter().meterDefinition(DEFAULT_GAS));

    Page<PagedLogicalMeterDto> page = asUser().getPage(
      "/meters?w=hot",
      PagedLogicalMeterDto.class
    );

    assertThat(page.getContent())
      .extracting(m -> m.medium)
      .containsExactly(DEFAULT_HOT_WATER.medium.name);
  }

  @Test
  public void wildcardSearch_SecondaryAddress() {
    //Note: We don't want to accidentally match the external IDs, so we set them explicitly here
    given(
      logicalMeter().externalId("externalId1"),
      physicalMeter().externalId("externalId1").address("123")
    );
    given(
      logicalMeter().externalId("externalId2"),
      physicalMeter().externalId("externalId2").address("456")
    );

    Page<PagedLogicalMeterDto> contains = asUser()
      .getPage("/meters?w=23", PagedLogicalMeterDto.class);

    assertThat(contains.getContent())
      .extracting(m -> m.address)
      .containsExactly("123");

    Page<PagedLogicalMeterDto> startsWith = asUser()
      .getPage("/meters?w=12", PagedLogicalMeterDto.class);

    assertThat(startsWith.getContent())
      .extracting(m -> m.address)
      .containsExactly("123");
  }

  @Test
  public void wildcardSearchDoesNotReturnNonMatches() {
    given(logicalMeter().externalId("first"));
    given(logicalMeter().externalId("second"));

    Page<PagedLogicalMeterDto> page = asUser()
      .getPage("/meters?w=secon", PagedLogicalMeterDto.class);

    assertThat(page.getContent())
      .extracting(m -> m.facility)
      .containsExactly("second");
  }

  @Test
  public void wildcardSearchWithMultipleFieldsMatching() {
    UUID meterId = randomUUID();

    logicalMeters.save(LogicalMeter.builder()
      .id(meterId)
      .externalId("street facility")
      .organisationId(context().organisationId())
      .location(new LocationBuilder().city("city town").address("street road 1").build())
      .build());

    physicalMeters.save(PhysicalMeter.builder()
      .organisationId(context().organisationId())
      .address("12345")
      .externalId(randomUUID().toString())
      .manufacturer("stre")
      .logicalMeterId(meterId)
      .build());

    Page<PagedLogicalMeterDto> page = asUser().getPage(
      "/meters?w=str",
      PagedLogicalMeterDto.class
    );

    assertThat(page).hasSize(1);
  }

  @Test
  public void wildcardSearchReturnsAllMatches() {
    UUID meterIdOne = randomUUID();
    logicalMeters.save(LogicalMeter.builder()
      .id(meterIdOne)
      .externalId(meterIdOne.toString())
      .organisationId(context().organisationId())
      .location(new LocationBuilder().address("street 1").build())
      .build());

    physicalMeters.save(PhysicalMeter.builder()
      .organisationId(context().organisationId())
      .address("12345")
      .externalId(randomUUID().toString())
      .logicalMeterId(meterIdOne)
      .build());

    UUID meterIdTwo = randomUUID();
    logicalMeters.save(LogicalMeter.builder()
      .id(meterIdTwo)
      .externalId("street facility")
      .organisationId(context().organisationId())
      .build());

    physicalMeters.save(PhysicalMeter.builder()
      .organisationId(context().organisationId())
      .address("12345")
      .externalId(randomUUID().toString())
      .logicalMeterId(meterIdTwo)
      .build());

    Page<PagedLogicalMeterDto> page = asUser().getPage(
      "/meters?w=street",
      PagedLogicalMeterDto.class
    );

    assertThat(page).hasSize(2);
  }

  @Test
  public void meterShouldHaveNoAlarms() {
    given(logicalMeter());

    ZonedDateTime start = context().now();
    Page<PagedLogicalMeterDto> paginatedLogicalMeters = asUser()
      .getPage(
        metersUrl(start, start.plusHours(1)),
        PagedLogicalMeterDto.class
      );

    assertThat(paginatedLogicalMeters.getTotalElements()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getTotalPages()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getContent().get(0).alarm).isNull();
  }

  @Test
  public void onlyMetersWithAlarms() {
    var interestingMeter = given(physicalMeter());
    var uninterestingMeter = given(physicalMeter());

    given(statusLog(interestingMeter).status(OK).start(context().now()));
    given(statusLog(uninterestingMeter).status(OK).start(context().now()));

    given(alarm(interestingMeter).mask(12).start(context().now()));

    Page<PagedLogicalMeterDto> paginatedLogicalMeters = asUser()
      .getPage(
        Url.builder()
          .path("/meters")
          .parameter(ALARM, "yes")
          .build(),
        PagedLogicalMeterDto.class
      );

    assertThat(paginatedLogicalMeters.getTotalElements()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getTotalPages()).isEqualTo(1);

    PagedLogicalMeterDto result = paginatedLogicalMeters.getContent().get(0);
    assertThat(result.id).isEqualTo(interestingMeter.id);
    assertThat(result.alarm.mask).isEqualTo(12);
    assertThat(result.alarm.description).isNull();
  }

  @Test
  public void onlyMetersWithNoAlarms() {
    LogicalMeter logicalMeterWithoutAlarm = given(logicalMeter());
    LogicalMeter logicalMeterWithAlarm = given(logicalMeter());

    given(alarm(logicalMeterWithAlarm).start(context().now()).mask(12));

    Page<PagedLogicalMeterDto> paginatedLogicalMeters = asUser()
      .getPage(
        Url.builder()
          .path("/meters")
          .parameter(ALARM, "no")
          .build(),
        PagedLogicalMeterDto.class
      );

    assertThat(paginatedLogicalMeters.getTotalElements()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getTotalPages()).isEqualTo(1);

    PagedLogicalMeterDto result = paginatedLogicalMeters.getContent().get(0);
    assertThat(result.id).isEqualTo(logicalMeterWithoutAlarm.id);
    assertThat(result.alarm).isNull();
  }

  @Test
  public void meterShouldHaveOneActiveAlarm() {
    var meter = given(logicalMeter());
    var alarms = given(alarm(meter).mask(12).start(context().now()));
    AlarmLogEntry alarmLogEntry = alarms.stream().findFirst().get();

    Page<PagedLogicalMeterDto> paginatedLogicalMeters = asUser()
      .getPage(
        metersUrl(context().now(), context().now().plusHours(1)),
        PagedLogicalMeterDto.class
      );

    assertThat(paginatedLogicalMeters.getTotalElements()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getTotalPages()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getContent())
      .extracting(m -> m.alarm)
      .containsExactly(new AlarmDto(alarmLogEntry.id, alarmLogEntry.mask));
  }

  @Test
  public void meterShouldHaveNoAlarmWhenThereIsNoActive() {
    LogicalMeter logicalMeter = given(logicalMeter());

    given(
      alarm(logicalMeter).start(context().now())
        .stop(context().now().plusHours(3))
        .mask(112)
        .description("something is wrong"),
      alarm(logicalMeter).start(context().now())
        .stop(context().now().plusHours(4))
        .mask(122)
        .description("testing")
    );

    var response = asUser()
      .getPage(
        metersUrl(context().now().plusHours(3), context().now().plusHours(3).plusMinutes(30)),
        PagedLogicalMeterDto.class
      );

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent().get(0).alarm).isNull();
  }

  @Test
  public void meterShouldHaveLastActiveAlarm() {
    LogicalMeter logicalMeter = given(logicalMeter());

    AlarmLogEntry activeAlarm = given(
      alarm(logicalMeter)
        .start(context().now())
        .stop(context().now().plusHours(3))
        .mask(112)
        .description("something is wrong"),
      alarm(logicalMeter).start(context().now()).mask(122).description("testing")
    ).stream().filter(a -> a.stop == null).findAny().orElseThrow();

    Page<PagedLogicalMeterDto> paginatedLogicalMeters = asUser()
      .getPage(
        metersUrl(context().now(), context().now().plusHours(4)),
        PagedLogicalMeterDto.class
      );

    assertThat(paginatedLogicalMeters.getTotalElements()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getTotalPages()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getContent().get(0).alarm)
      .isEqualTo(new AlarmDto(activeAlarm.id, activeAlarm.mask));
  }

  @Test
  public void findMeterByCurrentlyActivePhysicalMetersAddress_MultipleActiveInSelectionPeriod() {
    given(
      logicalMeter(),
      physicalMeter().activePeriod(PeriodRange.halfOpenFrom(context().yesterday(), context().now()))
        .address("aaa"),
      physicalMeter().activePeriod(PeriodRange.halfOpenFrom(context().now(), null))
        .address("bbb")
    );

    Page<PagedLogicalMeterDto> paginatedLogicalMeters = asUser()
      .getPage(
        Url.builder()
          .path("/meters")
          .parameter(SECONDARY_ADDRESS, "bbb")
          .build(),
        PagedLogicalMeterDto.class
      );

    assertThat(paginatedLogicalMeters.getTotalElements()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getTotalPages()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getContent().get(0).address)
      .isEqualTo("bbb");
  }

  @Test
  public void findMeterByCurrentlyActivePhysicalMetersAddress() {
    given(
      logicalMeter(),
      physicalMeter().activePeriod(PeriodRange.halfOpenFrom(context().yesterday(), context().now()))
        .address("aaa"),
      physicalMeter().activePeriod(PeriodRange.halfOpenFrom(context().now(), null))
        .address("bbb")
    );

    Page<PagedLogicalMeterDto> paginatedLogicalMeters = asUser()
      .getPage(
        Url.builder()
          .path("/meters")
          .parameter(SECONDARY_ADDRESS, "bbb")
          .build(),
        PagedLogicalMeterDto.class
      );

    assertThat(paginatedLogicalMeters.getTotalElements()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getTotalPages()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getContent().get(0).address)
      .isEqualTo("bbb");
  }

  @Test
  public void findMeterByPreviouslyActivePhysicalMetersAddress() {
    given(
      logicalMeter(),
      physicalMeter().activePeriod(PeriodRange.halfOpenFrom(context().yesterday(), context().now()))
        .address("aaa"),
      physicalMeter().activePeriod(PeriodRange.halfOpenFrom(context().now(), null))
        .address("bbb")
    );

    Page<PagedLogicalMeterDto> paginatedLogicalMeters = asUser()
      .getPage(
        Url.builder()
          .path("/meters")
          .parameter(SECONDARY_ADDRESS, "aaa")
          .build(),
        PagedLogicalMeterDto.class
      );

    assertThat(paginatedLogicalMeters.getTotalElements()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getTotalPages()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getContent().get(0).address)
      .isEqualTo("aaa");
  }

  @Test
  public void alwaysShowLatestStatusEvenIfSelectionPeriodIsBefore() {
    var interestingMeter = given(physicalMeter());

    given(statusLog(interestingMeter).status(OK).start(context().now().minusDays(3))
      .stop(context().now().minusDays(1)));
    given(statusLog(interestingMeter).status(ERROR).start(context().now().minusDays(1)));

    Page<PagedLogicalMeterDto> paginatedLogicalMeters = asUser()
      .getPage(
        Url.builder()
          .path("/meters")
          .build(),
        PagedLogicalMeterDto.class
      );

    assertThat(paginatedLogicalMeters.getTotalElements()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getTotalPages()).isEqualTo(1);

    PagedLogicalMeterDto result = paginatedLogicalMeters.getContent().get(0);
    assertThat(result.id).isEqualTo(interestingMeter.id);
    assertThat(result.isReported).isEqualTo(true);

    given(statusLog(interestingMeter).status(OK).start(context().now().minusHours(6)));

    paginatedLogicalMeters = asUser()
      .getPage(
        Url.builder()
          .path("/meters")
          .build(),
        PagedLogicalMeterDto.class
      );

    assertThat(paginatedLogicalMeters.getTotalElements()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getTotalPages()).isEqualTo(1);

    result = paginatedLogicalMeters.getContent().get(0);
    assertThat(result.id).isEqualTo(interestingMeter.id);
    assertThat(result.isReported).isEqualTo(false);
  }

  private void assertNothingIsRemoved(LogicalMeter meter) {
    Optional<LogicalMeterEntity> logicalMeterEntity = logicalMeterJpaRepository
      .findById(meter.id);

    assertThat(logicalMeterEntity)
      .as("Logical meter should not be removed")
      .isPresent();

    assertThat(logicalMeterEntity.get().physicalMeters.size())
      .as("Physical meter should not be removed")
      .isEqualTo(1);

    List<MeasurementEntity> measurements = measurementJpaRepository.findAll()
      .stream()
      .filter(measurementEntity ->
        measurementEntity.id.physicalMeter.id.equals(meter.activePhysicalMeter().get().id))
      .collect(toList());

    assertThat(measurements.size())
      .as("Measurements should not be removed")
      .isEqualTo(1);
  }

  private static UrlTemplate metersUrl(ZonedDateTime after, ZonedDateTime before) {
    return Url.builder()
      .path("/meters")
      .build();
  }

  private static void assertThatStatusIsNotFound(ResponseEntity<ErrorMessageDto> response) {
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
