package com.elvaco.mvp.web;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter.PhysicalMeterBuilder;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.QMeasurementEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionEntityMapper;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.testdata.UrlTemplate;
import com.elvaco.mvp.testing.fixture.UserBuilder;
import com.elvaco.mvp.web.dto.AlarmDto;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.PagedLogicalMeterDto;
import com.fasterxml.jackson.databind.JsonNode;
import org.assertj.core.util.DoubleComparator;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DISTRICT_HEATING_METER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.HOT_WATER_METER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.UNKNOWN_METER;
import static com.elvaco.mvp.core.domainmodels.StatusType.OK;
import static com.elvaco.mvp.core.spi.data.RequestParameter.AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.ALARM;
import static com.elvaco.mvp.core.spi.data.RequestParameter.BEFORE;
import static com.elvaco.mvp.core.spi.data.RequestParameter.REPORTED;
import static com.elvaco.mvp.testing.fixture.LocationTestData.kungsbacka;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assume.assumeTrue;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class LogicalMeterControllerTest extends IntegrationTest {

  private static final ZonedDateTime YESTERDAY = ZonedDateTime.parse("2018-09-25T00:00:00Z")
    .minusDays(1)
    .truncatedTo(ChronoUnit.DAYS);

  private MeterDefinition hotWaterMeterDefinition;

  @Autowired
  private MeterDefinitions meterDefinitions;

  @Autowired
  private MeterStatusLogs meterStatusLogs;

  @Autowired
  private MeterAlarmLogs meterAlarmLogs;

  private ZonedDateTime start;

  @Before
  public void setUp() {
    assumeTrue(isPostgresDialect());
    start = ZonedDateTime.parse("2001-01-01T00:00:00.00Z");
    hotWaterMeterDefinition = meterDefinitions.save(HOT_WATER_METER);
  }

  @After
  public void tearDown() {
    if (isPostgresDialect()) {
      measurementJpaRepository.deleteAll();
    }
    meterAlarmLogJpaRepository.deleteAll();
    gatewayStatusLogJpaRepository.deleteAll();
    gatewayJpaRepository.deleteAll();
  }

  @Test
  public void collectionStatusIsNullWhenNoInterval() {
    LogicalMeter districtHeatingMeter = saveLogicalMeter(DISTRICT_HEATING_METER);

    PhysicalMeter physicalMeter = physicalMeters.save(
      physicalMeter()
        .logicalMeterId(districtHeatingMeter.id)
        .externalId(randomUUID().toString())
        .build()
    );

    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(physicalMeter.id)
        .status(OK)
        .start(start)
        .build()
    );

    addMeasurementsForMeterQuantities(
      physicalMeter,
      districtHeatingMeter.getQuantities(),
      start,
      1.0
    );

    Page<PagedLogicalMeterDto> paginatedLogicalMeters = asUser()
      .getPage(
        metersUrl(start, start.plusHours(1)),
        PagedLogicalMeterDto.class
      );

    assertThat(paginatedLogicalMeters.getTotalElements()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getTotalPages()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getContent()).hasSize(1);
    assertThat(paginatedLogicalMeters.getContent().get(0).collectionPercentage).isNull();
  }

  @Test
  public void locationIsAttachedToPagedMeter() {
    UUID meterId = randomUUID();
    logicalMeters.save(LogicalMeter.builder()
      .id(meterId)
      .externalId(meterId.toString())
      .organisationId(context().organisationId())
      .location(kungsbacka().build())
      .build());

    PagedLogicalMeterDto logicalMeterDto = asUser()
      .getPage("/meters", PagedLogicalMeterDto.class)
      .getContent()
      .get(0);

    assertThat(logicalMeterDto.location.city).isEqualTo("kungsbacka");
    assertThat(logicalMeterDto.location.address).isEqualTo("kabelgatan 1");
  }

  @Test
  public void meterDefinitionIsAttachedToPagedMeter() {
    LogicalMeter districtHeatingMeter = saveLogicalMeter(DISTRICT_HEATING_METER);

    physicalMeters.save(physicalMeter()
      .logicalMeterId(districtHeatingMeter.id)
      .externalId(randomUUID().toString())
      .readIntervalMinutes(30)
      .build());

    PagedLogicalMeterDto logicalMeterDto = asUser()
      .getPage("/meters", PagedLogicalMeterDto.class)
      .getContent()
      .get(0);

    assertThat(logicalMeterDto.medium).isEqualTo(DISTRICT_HEATING_METER.medium);
  }

  @Test
  public void collectionStatusZeroPercentWhenNoMeasurements() {
    assumeTrue(isPostgresDialect());

    LogicalMeter districtHeatingMeter = saveLogicalMeter(
      YESTERDAY.minusMinutes(15)
    );

    PhysicalMeter physicalMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(districtHeatingMeter.id)
      .externalId(randomUUID().toString())
      .readIntervalMinutes(30)
      .build());

    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(physicalMeter.id)
        .status(StatusType.OK)
        .start(YESTERDAY.minusMinutes(15))
        .build()
    );

    missingMeasurementJpaRepository.refreshLocked();

    PagedLogicalMeterDto logicalMeterDto = asUser()
      .getPage(metersUrl(YESTERDAY, YESTERDAY.plusHours(1)), PagedLogicalMeterDto.class)
      .getContent()
      .get(0);

    assertThat(logicalMeterDto.collectionPercentage).isEqualTo(0.0);
  }

  @Test
  public void collectionStatusFiftyPercent() {
    LogicalMeter districtHeatingMeter = saveLogicalMeter(
      YESTERDAY.minusMinutes(15)
    );

    PhysicalMeter physicalMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(districtHeatingMeter.id)
      .externalId(randomUUID().toString())
      .readIntervalMinutes(30)
      .build()
    );

    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(physicalMeter.id)
        .status(OK)
        .start(YESTERDAY.minusMinutes(15))
        .stop(YESTERDAY.plusHours(1))
        .build()
    );

    addMeasurementsForMeterQuantities(
      physicalMeter,
      districtHeatingMeter.getQuantities(),
      YESTERDAY,
      1.0
    );

    missingMeasurementJpaRepository.refreshLocked();

    Page<PagedLogicalMeterDto> response = asUser()
      .getPage(metersUrl(YESTERDAY, YESTERDAY.plusHours(1)), PagedLogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).extracting("collectionPercentage").containsExactly(50.0);
  }

  @Test
  public void collectionStatusFiftyPercentWhenMeterHasStatuses() {
    var districtHeatingMeter = saveLogicalMeter(YESTERDAY.minusMinutes(15));

    var physicalMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(districtHeatingMeter.id)
      .externalId(randomUUID().toString())
      .readIntervalMinutes(60)
      .build()
    );

    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(physicalMeter.id)
        .status(OK)
        .start(YESTERDAY.minusMinutes(15))
        .stop(YESTERDAY.plusHours(1))
        .build()
    );

    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(physicalMeter.id)
        .status(StatusType.WARNING)
        .start(YESTERDAY.plusHours(1))
        .build()
    );

    addMeasurementsForMeter(
      physicalMeter,
      districtHeatingMeter.getQuantities(),
      YESTERDAY,
      Duration.ofHours(2),
      60L,
      1.0
    );

    missingMeasurementJpaRepository.refreshLocked();

    var response = asUser()
      .getPage(metersUrl(YESTERDAY, YESTERDAY.plusHours(5)), PagedLogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).extracting("collectionPercentage").containsExactly(40.0);
  }

  @Test
  public void collectionStatusFiftyPercentWhenMeterHasMultipleActiveStatusesWithinPeriod() {
    var districtHeatingMeter = saveLogicalMeter(YESTERDAY.minusMinutes(15));

    var physicalMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(districtHeatingMeter.id)
      .externalId(randomUUID().toString())
      .readIntervalMinutes(60)
      .build()
    );

    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(physicalMeter.id)
        .status(OK)
        .start(YESTERDAY.minusMinutes(15))
        .build()
    );

    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(physicalMeter.id)
        .status(StatusType.WARNING)
        .start(YESTERDAY.plusHours(1))
        .build()
    );

    addMeasurementsForMeter(
      physicalMeter,
      districtHeatingMeter.getQuantities(),
      YESTERDAY,
      Duration.ofHours(2),
      60L,
      1.0
    );

    missingMeasurementJpaRepository.refreshLocked();

    var content = asUser()
      .getPage(metersUrl(YESTERDAY, YESTERDAY.plusHours(4)), PagedLogicalMeterDto.class)
      .getContent();

    assertThat(content).extracting("collectionPercentage").containsExactly(50.0);
  }

  @Test
  public void readIntervalIsSetOnPagedMeter() {
    LogicalMeter districtHeatingMeter = saveLogicalMeter(DISTRICT_HEATING_METER);

    physicalMeters.save(physicalMeter()
      .logicalMeterId(districtHeatingMeter.id)
      .externalId(randomUUID().toString())
      .readIntervalMinutes(42)
      .build()
    );

    PagedLogicalMeterDto logicalMeterDto = asUser()
      .getPage("/meters", PagedLogicalMeterDto.class)
      .getContent()
      .get(0);

    assertThat(logicalMeterDto.readIntervalMinutes).isEqualTo(42);
  }

  @Test
  public void manufacturerIsSetOnPagedMeter() {
    LogicalMeter districtHeatingMeter = saveLogicalMeter(DISTRICT_HEATING_METER);

    physicalMeters.save(physicalMeter()
      .logicalMeterId(districtHeatingMeter.id)
      .manufacturer("KAKA")
      .externalId(randomUUID().toString())
      .build()
    );

    PagedLogicalMeterDto logicalMeterDto = asUser()
      .getPage("/meters", PagedLogicalMeterDto.class)
      .getContent()
      .get(0);

    assertThat(logicalMeterDto.manufacturer).isEqualTo("KAKA");
  }

  @Test
  public void gatewayIsSetOnPagedMeter() {
    Gateway gateway = gateways.save(Gateway.builder()
      .organisationId(context().organisationId())
      .serial("gateway-serial")
      .productModel("gateway-product")
      .build());

    LogicalMeter districtHeatingMeter = logicalMeters.save(LogicalMeter.builder()
      .externalId("external-id")
      .organisationId(context().organisation().id)
      .meterDefinition(DISTRICT_HEATING_METER)
      .created(start)
      .gateway(gateway)
      .build()
    );

    physicalMeters.save(physicalMeter()
      .logicalMeterId(districtHeatingMeter.id)
      .manufacturer("KAKA")
      .externalId(randomUUID().toString())
      .build()
    );

    PagedLogicalMeterDto logicalMeterDto = asUser()
      .getPage("/meters", PagedLogicalMeterDto.class)
      .getContent()
      .get(0);

    assertThat(logicalMeterDto.gatewaySerial).isEqualTo("gateway-serial");
  }

  @Test
  @Ignore("Not supported, yet")
  public void collectionStatusRespectsChangingIntervalOnPhysicalMeter() {
    LogicalMeter districtHeatingMeter = saveLogicalMeter(DISTRICT_HEATING_METER);

    PhysicalMeter firstMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(districtHeatingMeter.id)
      .externalId(randomUUID().toString())
      .readIntervalMinutes(60)
      .build()
    );

    PhysicalMeter secondMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(districtHeatingMeter.id)
      .externalId(randomUUID().toString())
      .readIntervalMinutes(120)
      .build()
    );

    addMeasurementsForMeter(
      firstMeter,
      districtHeatingMeter.getQuantities(),
      start,
      Duration.ofHours(1),
      firstMeter.readIntervalMinutes
    );

    addMeasurementsForMeter(
      secondMeter,
      districtHeatingMeter.getQuantities(),
      start.plusHours(1),
      Duration.ofHours(2),
      secondMeter.readIntervalMinutes
    );

    PagedLogicalMeterDto logicalMeterDto = asUser()
      .getPage(
        metersUrl(start, start.plusHours(3)),
        PagedLogicalMeterDto.class
      )
      .getContent()
      .get(0);

    assertThat(logicalMeterDto.collectionPercentage).isEqualTo(100.0);
  }

  @Test
  public void pagedMeterPhysicalMeterChange() {
    LogicalMeter districtHeatingMeter = saveLogicalMeter(DISTRICT_HEATING_METER);
    physicalMeters.save(physicalMeter()
      .logicalMeterId(districtHeatingMeter.id)
      .externalId(districtHeatingMeter.externalId)
      .readIntervalMinutes(1)
      .manufacturer("1")
      .address("1")
      .build()
    );

    physicalMeters.save(physicalMeter()
      .logicalMeterId(districtHeatingMeter.id)
      .externalId(districtHeatingMeter.externalId)
      .readIntervalMinutes(2)
      .manufacturer("2")
      .address("2")
      .build()
    );

    List<PagedLogicalMeterDto> meters = asUser()
      .getPage("/meters", PagedLogicalMeterDto.class)
      .getContent();

    assertThat(meters).hasSize(2);

    PagedLogicalMeterDto first = meters.stream()
      .filter(logicalMeterDto -> logicalMeterDto.readIntervalMinutes.equals(1L))
      .findFirst().get();
    assertThat(first.manufacturer).isEqualTo("1");
    assertThat(first.address).isEqualTo("1");

    PagedLogicalMeterDto second = meters.stream()
      .filter(logicalMeterDto -> logicalMeterDto.readIntervalMinutes.equals(2L))
      .findFirst().get();
    assertThat(second.manufacturer).isEqualTo("2");
    assertThat(second.address).isEqualTo("2");
  }

  @Test
  public void collectionStatusTwoOutOfThreeMissing() {
    LogicalMeter districtHeatingMeter = saveLogicalMeter(
      YESTERDAY.minusMinutes(15)
    );

    PhysicalMeter physicalMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(districtHeatingMeter.id)
      .externalId(randomUUID().toString())
      .readIntervalMinutes(15)
      .build()
    );

    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(physicalMeter.id)
        .status(OK)
        .start(YESTERDAY.minusMinutes(15))
        .build()
    );

    addMeasurementsForMeterQuantities(
      physicalMeter,
      districtHeatingMeter.getQuantities(),
      YESTERDAY,
      1.0
    );

    missingMeasurementJpaRepository.refreshLocked();

    PagedLogicalMeterDto logicalMeterDto = asUser()
      .getPage(metersUrl(YESTERDAY, YESTERDAY.plusMinutes(45)), PagedLogicalMeterDto.class)
      .getContent()
      .get(0);

    assertThat(logicalMeterDto.collectionPercentage).isEqualTo(33.33333333333333);
  }

  @Test
  public void collectionStatusMeterChangeWithIntervalUpdate() {
    LogicalMeter districtHeatingMeter = saveLogicalMeter(
      YESTERDAY.minusMinutes(15)
    );

    PhysicalMeter physicalMeter1 = physicalMeters.save(physicalMeter()
      .logicalMeterId(districtHeatingMeter.id)
      .externalId(randomUUID().toString())
      .readIntervalMinutes(0)
      .build()
    );

    PhysicalMeter physicalMeter2 = physicalMeters.save(physicalMeter()
      .logicalMeterId(districtHeatingMeter.id)
      .externalId(randomUUID().toString())
      .readIntervalMinutes(60)
      .build()
    );

    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(physicalMeter1.id)
        .status(OK)
        .start(YESTERDAY.minusMinutes(15))
        .build()
    );

    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(physicalMeter2.id)
        .status(OK)
        .start(YESTERDAY.minusMinutes(15))
        .build()
    );

    addMeasurementsForMeter(
      physicalMeter2,
      districtHeatingMeter.getQuantities(),
      YESTERDAY.plusHours(1),
      Duration.ofDays(1),
      physicalMeter2.readIntervalMinutes
    );

    missingMeasurementJpaRepository.refreshLocked();

    List<PagedLogicalMeterDto> pagedMeters = asUser()
      .getPage(
        metersUrl(YESTERDAY, YESTERDAY.plusDays(1)),
        PagedLogicalMeterDto.class
      )
      .getContent();

    /* NOTE! We get _two_ entries for the same logical meter here, since we have two physical meters
    connected - one with no collection percentage (since it has no interval) and one with the
    expected percentage. */
    assertThat(pagedMeters).extracting("collectionPercentage")
      .usingComparatorForType(new DoubleComparator(0.1), Double.class)
      .containsExactlyInAnyOrder(null, 95.8333);
  }

  @Test
  public void collectionStatusOneHundredPercent() {
    LogicalMeter districtHeatingMeter = saveLogicalMeter(DISTRICT_HEATING_METER);

    ZonedDateTime start = ZonedDateTime.parse("2001-01-01T01:00:00.00Z");

    PhysicalMeter physicalMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(districtHeatingMeter.id)
      .externalId(randomUUID().toString())
      .readIntervalMinutes(60)
      .build()
    );

    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(physicalMeter.id)
        .status(StatusType.OK)
        .start(start)
        .build()
    );

    addMeasurementsForMeter(
      physicalMeter,
      districtHeatingMeter.getQuantities(),
      start,
      Duration.ofDays(1),
      60L
    );

    Page<PagedLogicalMeterDto> paginatedLogicalMeters = asUser()
      .getPage(
        metersUrl(start, start.plusDays(1)),
        PagedLogicalMeterDto.class
      );

    assertThat(paginatedLogicalMeters.getTotalElements()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getTotalPages()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getContent()).hasSize(1);
    assertThat(paginatedLogicalMeters.getContent().get(0).collectionPercentage).isEqualTo(100.0);
  }

  @Test
  public void findAllPaged() {
    saveLogicalMeter();
    saveLogicalMeter();
    saveLogicalMeter();

    Page<PagedLogicalMeterDto> response = asUser()
      .getPage("/meters?size=1", PagedLogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(3);
    assertThat(response.getNumberOfElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(3);
  }

  @Test
  public void findAllPagedSized() {
    saveLogicalMeter();
    saveLogicalMeter();
    saveLogicalMeter();

    Page<PagedLogicalMeterDto> response = asUser()
      .getPage("/meters?page=0&size=2", PagedLogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(3);
    assertThat(response.getNumberOfElements()).isEqualTo(2);
    assertThat(response.getTotalPages()).isEqualTo(2);
  }

  @Ignore
  @Test
  public void findAllPagedAndSorted() {
    // Address asc
    testSorting(
      "/meters?size=20&page=0&sort=address,asc",
      "Unexpected address, sorting failed",
      (PagedLogicalMeterDto meter) -> meter.location.address,
      "Drottninggatan 2"
    );

    // Address desc
    testSorting(
      "/meters?size=20&page=0&sort=address,desc",
      "Unexpected address, sorting failed",
      (PagedLogicalMeterDto meter) -> meter.location.address,
      "Kungsgatan 55"
    );

    // Manufacturer asc
    testSorting(
      "/meters?size=20&page=0&sort=manufacturer,asc",
      "Unexpected manufacturer, sorting failed",
      (PagedLogicalMeterDto meter) -> meter.manufacturer,
      "ELV1"
    );

    // Manufacturer desc
    testSorting(
      "/meters?size=20&page=0&sort=manufacturer,desc",
      "Unexpected manufacturer, sorting failed",
      (PagedLogicalMeterDto meter) -> meter.manufacturer,
      "ELV55"
    );

    testSorting(
      "/meters?size=20&page=0&sort=city,asc",
      "Unexpected city, sorting failed",
      (PagedLogicalMeterDto meter) -> meter.location.city,
      "Varberg"
    );

    testSorting(
      "/meters?size=20&page=0&sort=city,desc",
      "Unexpected city, sorting failed",
      (PagedLogicalMeterDto meter) -> meter.location.city,
      "Östersund"
    );
  }

  @Test
  public void statusesAreNotIncludedInStatusQueryForPeriod() {
    LogicalMeter firstLogicalMeter = saveLogicalMeter();
    LogicalMeter secondLogicalMeter = saveLogicalMeter();
    LogicalMeter thirdLogicalMeter = saveLogicalMeter();

    PhysicalMeter firstMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(firstLogicalMeter.id)
      .externalId("meter-one")
      .readIntervalMinutes(15)
      .build()
    );
    PhysicalMeter secondMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(secondLogicalMeter.id)
      .externalId("meter-two")
      .readIntervalMinutes(15)
      .build()
    );
    PhysicalMeter thirdMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(thirdLogicalMeter.id)
      .externalId("meter-three")
      .readIntervalMinutes(15)
      .build()
    );

    // status is active within period, should be included
    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(firstMeter.id)
        .status(StatusType.ERROR)
        .start(ZonedDateTime.parse("2004-12-25T10:14:00Z"))
        .build()
    );

    // status ended before period begun, should not be included
    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(secondMeter.id)
        .status(StatusType.ERROR)
        .start(ZonedDateTime.parse("2002-12-25T10:14:00Z"))
        .stop(ZonedDateTime.parse("2004-09-14T12:12:12Z"))
        .build()
    );

    //status started after period ended, should not be included
    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(thirdMeter.id)
        .status(StatusType.ERROR)
        .start(ZonedDateTime.parse("2015-04-29T10:14:20Z"))
        .build()
    );

    Url url = Url.builder()
      .path("/meters")
      .parameter(AFTER, "2005-01-10T01:00:00.00Z")
      .parameter(BEFORE, "2015-01-01T23:00:00.00Z")
      .parameter(REPORTED, StatusType.ERROR.name)
      .build();

    Page<PagedLogicalMeterDto> response = asUser()
      .getPage(url, PagedLogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getNumberOfElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);

    assertThat(response.getContent()).extracting("id").containsExactly(firstLogicalMeter.id);
  }

  @Test
  public void findAllWithPredicates() {
    saveLogicalMeter();
    saveLogicalMeter(HOT_WATER_METER);

    Page<PagedLogicalMeterDto> response = asUser()
      .getPage("/meters?medium=Hot+water", PagedLogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getNumberOfElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
  }

  @Test
  public void findsOwnOrganisationsMetersByFilter() {
    saveLogicalMeter();

    Page<PagedLogicalMeterDto> response = asUser()
      .getPage("/meters?organisation=" + context().organisationId(), PagedLogicalMeterDto.class);

    assertThat(response.getTotalElements()).isGreaterThanOrEqualTo(1L);
  }

  @Test
  public void cannotAccessOtherOrganisationsMetersByFilter() {
    User user = userBuilder().build();
    createUserIfNotPresent(user);
    saveLogicalMeter();

    Page<PagedLogicalMeterDto> response = restClient()
      .loginWith(user.email, user.password)
      .tokenAuthorization()
      .getPage("/meters?organisation=" + context().organisationId(), PagedLogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(0L);
  }

  @Test
  public void doesntFindOtherOrganisationsMetersUsingFilter() {
    createUserIfNotPresent(userBuilder().build());
    LogicalMeter myMeter = logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("my-own-meter")
      .organisationId(context().organisationId2())
      .meterDefinition(hotWaterMeterDefinition)
      .location(UNKNOWN_LOCATION)
      .build());
    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("not-my-meter")
      .organisationId(context().organisationId())
      .meterDefinition(hotWaterMeterDefinition)
      .location(UNKNOWN_LOCATION)
      .build());

    Page<PagedLogicalMeterDto> response = restClient()
      .loginWith("me@myorg.com", "secr3t")
      .tokenAuthorization()
      .getPage("/meters?medium=Hot+water", PagedLogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(1L);
    assertThat(response.getContent().get(0).id).isEqualTo(myMeter.id);
  }

  @Test
  public void findAllMeters_WithGatewaySerial() {
    String serial = "666";
    createMeterWithGateway("my-mapped-meter", serial);
    createMeterWithGateway("my-mapped-meter2", "777");

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage("/meters?gatewaySerial=" + serial, PagedLogicalMeterDto.class);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).gatewaySerial).isEqualTo(serial);
  }

  @Test
  public void findAllMeters_WithFacility() {
    String facility = "my-mapped-meter";

    logicalMeters.save(LogicalMeter.builder()
      .externalId(facility)
      .organisationId(context().organisationId())
      .build());

    logicalMeters.save(LogicalMeter.builder()
      .externalId("another-mapped-meter")
      .organisationId(context().organisationId())
      .build());

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage("/meters?facility=" + facility, PagedLogicalMeterDto.class);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).facility).isEqualTo(facility);
  }

  @Test
  public void findAllMeters_WithSecondaryAddress() {
    String secondaryAddress = "1";
    LogicalMeter logicalMeter = saveLogicalMeter(DISTRICT_HEATING_METER);

    physicalMeters.save(PhysicalMeter.builder()
      .organisation(context().organisation())
      .medium("Heat")
      .manufacturer("ELV1")
      .logicalMeterId(logicalMeter.id)
      .externalId(randomUUID().toString())
      .readIntervalMinutes(0)
      .address(secondaryAddress)
      .build()
    );

    String anotherSecondaryAddress = "2";
    LogicalMeter anotherLogicalMeter = saveLogicalMeter(DISTRICT_HEATING_METER);

    physicalMeters.save(PhysicalMeter.builder()
      .organisation(context().organisation())
      .medium("Heat")
      .manufacturer("ELV1")
      .logicalMeterId(anotherLogicalMeter.id)
      .externalId(randomUUID().toString())
      .readIntervalMinutes(0)
      .address(anotherSecondaryAddress)
      .build()
    );

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage("/meters?secondaryAddress=" + secondaryAddress, PagedLogicalMeterDto.class);

    assertThat(result.getContent())
      .as("There should be one meter with secondary address: \"" + secondaryAddress + "\"")
      .hasSize(1);
    assertThat(result.getContent().get(0).address).isEqualTo(secondaryAddress);
  }

  @Test
  public void findAllMeters_WithUnknownCity() {
    logicalMeters.save(LogicalMeter.builder()
      .externalId("my-mapped-meter")
      .organisationId(context().organisationId())
      .build());

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage("/meters?city=unknown,unknown", PagedLogicalMeterDto.class);

    assertThat(result.getContent()).hasSize(1);
  }

  @Test
  public void findAllMeters_IncludeMetersWith_UnknownCity() {
    logicalMeters.save(LogicalMeter.builder()
      .externalId("my-mapped-meter")
      .organisationId(context().organisationId())
      .build());
    logicalMeters.save(LogicalMeter.builder()
      .externalId("123-123-123")
      .organisationId(context().organisationId())
      .location(kungsbacka().build())
      .build());

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage("/meters?city=unknown,unknown&city=sverige,kungsbacka", PagedLogicalMeterDto.class);

    assertThat(result.getContent()).hasSize(2);
  }

  @Test
  public void findAllMeters_IncludeMetersWith_UnknownCity_AndLowConfidence() {
    logicalMeters.save(LogicalMeter.builder()
      .externalId("my-mapped-meter")
      .organisationId(context().organisationId())
      .build());

    logicalMeters.save(LogicalMeter.builder()
      .externalId("123-123-123")
      .organisationId(context().organisationId())
      .location(kungsbacka().build())
      .build());

    logicalMeters.save(LogicalMeter.builder()
      .externalId("123-456")
      .organisationId(context().organisationId())
      .location(kungsbacka().confidence(0.74).build())
      .build());

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage("/meters?city=unknown,unknown&city=sverige,kungsbacka", PagedLogicalMeterDto.class);

    assertThat(result.getContent()).hasSize(3);
  }

  @Test
  public void findAllMetersWithUnknownCity() {
    logicalMeters.save(LogicalMeter.builder()
      .externalId("123")
      .organisationId(context().organisationId())
      .build());

    logicalMeters.save(LogicalMeter.builder()
      .externalId("123-123-123")
      .organisationId(context().organisationId())
      .location(kungsbacka().build())
      .build());

    logicalMeters.save(LogicalMeter.builder()
      .externalId("456")
      .organisationId(context().organisationId())
      .location(kungsbacka().confidence(0.74).build())
      .build());

    logicalMeters.save(LogicalMeter.builder()
      .externalId("789")
      .organisationId(context().organisationId())
      .location(kungsbacka()
        .longitude(null)
        .latitude(null)
        .confidence(null).build())
      .build());

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage("/meters?city=unknown,unknown", PagedLogicalMeterDto.class);

    assertThat(result.getContent()).extracting("facility")
      .containsExactlyInAnyOrder("123");
  }

  @Test
  public void findAllMeters_WithManufacturer() {
    physicalMeters.save(physicalMeter().manufacturer("KAM")
      .logicalMeterId(saveLogicalMeter().id)
      .build());

    physicalMeters.save(physicalMeter().manufacturer("ELV")
      .logicalMeterId(saveLogicalMeter().id)
      .build());

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage("/meters?manufacturer=ELV", PagedLogicalMeterDto.class);

    assertThat(result.getContent()).extracting("manufacturer")
      .containsExactly("ELV");
  }

  @Test
  public void findAllMeters_WithId() {
    UUID id1 = saveLogicalMeter().id;
    UUID id2 = saveLogicalMeter().id;
    physicalMeters.save(physicalMeter()
      .logicalMeterId(id1)
      .build());

    physicalMeters.save(physicalMeter()
      .logicalMeterId(id2)
      .build());

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage("/meters?id=" + id1, PagedLogicalMeterDto.class);

    assertThat(result.getContent()).extracting("id")
      .containsExactly(id1);
  }

  @Test
  public void findAllMeters_WithLogicalMeterId() {
    UUID id1 = saveLogicalMeter().id;
    UUID id2 = saveLogicalMeter().id;
    physicalMeters.save(physicalMeter()
      .logicalMeterId(id1)
      .build());

    physicalMeters.save(physicalMeter()
      .logicalMeterId(id2)
      .build());

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage("/meters?logicalMeterId=" + id1, PagedLogicalMeterDto.class);

    assertThat(result.getContent()).extracting("id")
      .containsExactly(id1);
  }

  @Test
  public void findAllMeters_WithUnknownAddress() {
    LogicalMeter.LogicalMeterBuilder logicalMeter = LogicalMeter.builder()
      .organisationId(context().organisationId());

    logicalMeters.save(logicalMeter.externalId("abc").build());

    logicalMeters.save(logicalMeter.externalId("123")
      .location(kungsbacka().confidence(0.75).build())
      .build());

    logicalMeters.save(logicalMeter.externalId("456")
      .location(kungsbacka().confidence(0.80).build())
      .build());

    logicalMeters.save(logicalMeter.externalId("789")
      .location(kungsbacka()
        .longitude(null)
        .latitude(null)
        .confidence(null).build())
      .build());

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage("/meters?address=unknown,unknown,unknown", PagedLogicalMeterDto.class);

    assertThat(result.getContent()).extracting("facility")
      .containsExactly("abc");
  }

  @Test
  public void findAllMetersPaged_WithOrganisationAsUser() {
    LogicalMeter anotherOrganisationsMeter = logicalMeters.save(
      logicalMeterBuilder(UNKNOWN_METER)
        .organisationId(context().organisationId2())
        .externalId("someone-elses-meter")
        .build()
    );

    physicalMeters.save(physicalMeter()
      .logicalMeterId(anotherOrganisationsMeter.id)
      .externalId("someone-elses-meter")
      .organisation(context().organisation2())
      .build()
    );

    LogicalMeter usersMeter = logicalMeters.save(
      LogicalMeter.builder()
        .organisationId(context().organisationId())
        .externalId("users-meter")
        .build()
    );

    physicalMeters.save(physicalMeter()
      .logicalMeterId(usersMeter.id)
      .externalId("users-meter")
      .organisation(context().organisation())
      .build()
    );

    var allMeters = asUser().getPage("/meters", PagedLogicalMeterDto.class);

    assertThat(allMeters.getContent())
      .extracting("organisationId")
      .containsExactly(context().organisationId());

    asUser()
      .getPage(
        "/meters?organisation={id}",
        PagedLogicalMeterDto.class,
        context().organisationId2()
      );

    assertThat(allMeters.getContent())
      .as("The requested organisation was ignored, and implicitly replaced with the user's")
      .extracting("organisationId")
      .containsExactly(context().organisationId());
  }

  @Test
  public void findAllMetersPaged_WithOrganisationAsSuperAdmin() {
    LogicalMeter anotherOrganisationsMeter = logicalMeters.save(
      logicalMeterBuilder(UNKNOWN_METER)
        .organisationId(context().organisationId2())
        .build()
    );

    physicalMeters.save(physicalMeter()
      .logicalMeterId(anotherOrganisationsMeter.id)
      .externalId("someone-elses-meter")
      .organisation(context().organisation2())
      .build()
    );

    physicalMeters.save(physicalMeter()
      .logicalMeterId(saveLogicalMeter().id)
      .externalId("super-admins-meter")
      .organisation(context().superAdmin.organisation)
      .build()
    );

    var allMeters = asSuperAdmin().getPage("/meters", PagedLogicalMeterDto.class);

    assertThat(allMeters.getContent()).hasSize(2);

    var oneOrganisation = asSuperAdmin()
      .getPage(
        "/meters?organisation={id}",
        PagedLogicalMeterDto.class,
        context().organisationId2()
      );

    assertThat(oneOrganisation.getContent())
      .hasSize(1)
      .extracting("id")
      .containsExactly(anotherOrganisationsMeter.id);
  }

  @Test
  public void userCanNotRemoveLogicalMeter() {
    LogicalMeter districtHeatingMeter = saveLogicalMeter(DISTRICT_HEATING_METER);
    PhysicalMeter physicalMeter = physicalMeters.save(
      physicalMeter()
        .logicalMeterId(districtHeatingMeter.id)
        .externalId("bowie")
        .readIntervalMinutes(15)
        .build()
    );

    ZonedDateTime date = ZonedDateTime.now();

    addMeasurementsForMeter(
      physicalMeter,
      new HashSet<>(singletonList(Quantity.VOLUME)),
      date,
      Duration.ofHours(1),
      60L
    );

    ResponseEntity<ErrorMessageDto> response = asUser()
      .delete("/meters/" + districtHeatingMeter.id, ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    assertNothingIsRemoved(districtHeatingMeter, physicalMeter);
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
  public void adminCanNotRemoveLogicalMeter() {
    LogicalMeter districtHeatingMeter = saveLogicalMeter(DISTRICT_HEATING_METER);
    PhysicalMeter physicalMeter = physicalMeters.save(
      physicalMeter()
        .logicalMeterId(districtHeatingMeter.id)
        .externalId("bowie")
        .readIntervalMinutes(15)
        .build()
    );

    ZonedDateTime date = ZonedDateTime.now();

    addMeasurementsForMeter(
      physicalMeter,
      singleton(Quantity.VOLUME),
      date,
      Duration.ofHours(1),
      60L
    );

    ResponseEntity<Unauthorized> response = asAdmin()
      .delete("/meters/" + districtHeatingMeter.id, Unauthorized.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    assertNothingIsRemoved(districtHeatingMeter, physicalMeter);
  }

  @Test
  public void superAdminCanRemoveLogicalMeter() {
    LogicalMeter districtHeatingMeter = saveLogicalMeter(DISTRICT_HEATING_METER);
    PhysicalMeter physicalMeter = physicalMeters.save(
      physicalMeter()
        .logicalMeterId(districtHeatingMeter.id)
        .externalId("bowie")
        .readIntervalMinutes(15)
        .build()
    );

    ZonedDateTime date = ZonedDateTime.now();

    addMeasurementsForMeter(
      physicalMeter,
      singleton(Quantity.VOLUME),
      date,
      Duration.ofDays(1),
      60L
    );

    ResponseEntity<LogicalMeterDto> response = asSuperAdmin()
      .delete("/meters/" + districtHeatingMeter.id, LogicalMeterDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(logicalMeters.findById(districtHeatingMeter.id)).isEmpty();
    assertThat(physicalMeterJpaRepository.findById(physicalMeter.id)).isEmpty();
    assertThat(measurements.findBy(physicalMeter.id, date, Quantity.VOLUME.name)).isEmpty();
  }

  @Test
  public void superAdminRemoveNonExistingLogicalMeter() {
    ResponseEntity<ErrorMessageDto> response = asSuperAdmin()
      .delete("/meters/" + randomUUID(), ErrorMessageDto.class);

    assertThatStatusIsNotFound(response);
  }

  @Test
  public void nullFieldsAreNotIncludedInDto() {
    UUID meterId = randomUUID();
    logicalMeters.save(LogicalMeter.builder()
      .id(meterId)
      .externalId(meterId.toString())
      .organisationId(context().organisation().id)
      .meterDefinition(DISTRICT_HEATING_METER)
      .created(start)
      .build());

    JsonNode logicalMeterJson = asUser()
      .getJson("/meters/" + meterId);

    assertThat(logicalMeterJson.has("collectionPercentage")).isFalse();
  }

  @Test
  public void wildcardSearchMatchesFacilityStart() {
    UUID meterId = randomUUID();
    logicalMeters.save(LogicalMeter.builder()
      .id(meterId)
      .externalId("abcdef")
      .organisationId(context().organisationId())
      .build());

    Page<PagedLogicalMeterDto> page = asUser().getPage(
      "/meters?w=abc",
      PagedLogicalMeterDto.class
    );

    assertThat(page).hasSize(1);
    PagedLogicalMeterDto logicalMeterDto = page.getContent().get(0);
    assertThat(logicalMeterDto.facility).isEqualTo("abcdef");
  }

  @Test
  public void wildcardSearchMatchesCityStart() {
    UUID meterId = randomUUID();
    logicalMeters.save(LogicalMeter.builder()
      .id(meterId)
      .externalId(meterId.toString())
      .organisationId(context().organisationId())
      .location(new LocationBuilder().city("ringhals").build())
      .build());

    Page<PagedLogicalMeterDto> page = asUser().getPage(
      "/meters?w=ring",
      PagedLogicalMeterDto.class
    );

    assertThat(page)
      .extracting("location.city")
      .containsExactly("ringhals");
  }

  @Test
  public void wildcardSearchMatchesCityStart_caseInsensitive() {
    UUID meterId = randomUUID();
    logicalMeters.save(LogicalMeter.builder()
      .id(meterId)
      .externalId(meterId.toString())
      .organisationId(context().organisationId())
      .location(new LocationBuilder().city("ringhals").build())
      .build());

    Page<PagedLogicalMeterDto> page = asUser().getPage(
      "/meters?w=Ring",
      PagedLogicalMeterDto.class
    );

    assertThat(page)
      .extracting("location.city")
      .containsExactly("ringhals");
  }

  @Test
  public void wildcardSearchMatchesAddressStart() {
    UUID meterId = randomUUID();
    logicalMeters.save(LogicalMeter.builder()
      .id(meterId)
      .externalId(meterId.toString())
      .organisationId(context().organisationId())
      .location(new LocationBuilder().city("ringhals").address("storgatan 34").build())
      .build());

    Page<PagedLogicalMeterDto> page = asUser().getPage(
      "/meters?w=storgat",
      PagedLogicalMeterDto.class
    );

    assertThat(page)
      .extracting("location.address", "location.city")
      .containsExactly(tuple("storgatan 34", "ringhals"));
  }

  @Test
  public void wildcardSearchMatchesAddressStart_caseInsensitive() {
    UUID meterId = randomUUID();
    logicalMeters.save(LogicalMeter.builder()
      .id(meterId)
      .externalId(meterId.toString())
      .organisationId(context().organisationId())
      .location(new LocationBuilder().city("ringhals").address("storgatan 34").build())
      .build());

    Page<PagedLogicalMeterDto> page = asUser().getPage(
      "/meters?w=Storgat",
      PagedLogicalMeterDto.class
    );

    assertThat(page)
      .extracting("location.address", "location.city")
      .containsExactly(tuple("storgatan 34", "ringhals"));
  }

  @Test
  public void wildcardSearchMatchesManufacturerStart() {
    UUID meterId = randomUUID();

    logicalMeters.save(LogicalMeter.builder()
      .id(meterId)
      .externalId(meterId.toString())
      .organisationId(context().organisationId())
      .location(UNKNOWN_LOCATION)
      .build());

    physicalMeters.save(PhysicalMeter.builder()
      .organisation(context().organisation())
      .externalId(randomUUID().toString())
      .manufacturer("ELV")
      .address("1234")
      .logicalMeterId(meterId)
      .build());

    Page<PagedLogicalMeterDto> page = asUser().getPage(
      "/meters?w=EL",
      PagedLogicalMeterDto.class
    );

    assertThat(page).hasSize(1);
    PagedLogicalMeterDto logicalMeterDto = page.getContent().get(0);
    assertThat(logicalMeterDto.manufacturer).isEqualTo("ELV");
  }

  @Test
  public void wildcardSearchMatchesMediumStart_IgnoresCase() {
    UUID meterId = randomUUID();

    logicalMeters.save(LogicalMeter.builder()
      .id(meterId)
      .organisationId(context().organisationId())
      .externalId(meterId.toString())
      .meterDefinition(HOT_WATER_METER)
      .build());

    Page<PagedLogicalMeterDto> page = asUser().getPage(
      "/meters?w=hot",
      PagedLogicalMeterDto.class
    );

    assertThat(page).hasSize(1);
    PagedLogicalMeterDto logicalMeterDto = page.getContent().get(0);
    assertThat(logicalMeterDto.medium).isEqualTo("Hot water");
  }

  @Test
  public void wildcardSearch_StartsWithSecondaryAddress() {
    UUID logicalMeterId = randomUUID();

    logicalMeters.save(LogicalMeter.builder()
      .id(logicalMeterId)
      .externalId("external")
      .organisationId(context().organisationId())
      .build());

    physicalMeters.save(PhysicalMeter.builder()
      .organisation(context().organisation())
      .externalId(randomUUID().toString())
      .manufacturer("ELV")
      .address("032123")
      .logicalMeterId(logicalMeterId)
      .build());

    Page<PagedLogicalMeterDto> page = asUser()
      .getPage("/meters?w=03", PagedLogicalMeterDto.class);

    assertThat(page).hasSize(1);
    assertThat(page.getContent()).extracting("address").containsExactly("032123");
  }

  @Test
  public void wildcardSearchDoesNotReturnNonMatches() {
    UUID meterId = randomUUID();
    LogicalMeter.LogicalMeterBuilder builder = LogicalMeter.builder()
      .id(meterId)
      .organisationId(context().organisationId());

    logicalMeters.save(builder.externalId("first facility").build());
    logicalMeters.save(builder.externalId("second facility").build());

    Page<PagedLogicalMeterDto> page = asUser()
      .getPage("/meters?w=secon", PagedLogicalMeterDto.class);

    assertThat(page).hasSize(1);
    assertThat(page.getContent()).extracting("facility").containsExactly("second facility");
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
      .organisation(context().organisation())
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
      .organisation(context().organisation())
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
      .organisation(context().organisation())
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
  public void findsMeterWithinPeriodWithNoActiveStatus() {
    UUID meterId = randomUUID();

    logicalMeters.save(LogicalMeter.builder()
      .id(meterId)
      .externalId(meterId.toString())
      .organisationId(context().organisationId())
      .location(new LocationBuilder().address("street 1").build())
      .build());

    physicalMeters.save(PhysicalMeter.builder()
      .organisation(context().organisation())
      .address("12345")
      .externalId(randomUUID().toString())
      .logicalMeterId(meterId)
      .build());

    List<PagedLogicalMeterDto> content = asUser().getPage(
      metersUrl(start.minusHours(1), start.plusHours(1)),
      PagedLogicalMeterDto.class
    ).getContent();
    assertThat(content).extracting("id").containsExactly(meterId);
  }

  @Test
  public void meterShouldHaveNoAlarms() {
    LogicalMeter logicalMeter = saveLogicalMeter(DISTRICT_HEATING_METER);

    physicalMeters.save(
      physicalMeter()
        .logicalMeterId(logicalMeter.id)
        .externalId("123123")
        .build()
    );

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
    LogicalMeter logicalMeter1 = saveLogicalMeter();
    LogicalMeter logicalMeter2 = saveLogicalMeter();
    StatusLogEntry.StatusLogEntryBuilder<UUID> statusBuilder = StatusLogEntry.<UUID>builder()
      .status(StatusType.OK)
      .start(start);

    PhysicalMeter physicalMeter = physicalMeters.save(
      physicalMeter().logicalMeterId(logicalMeter1.id).build()
    );

    saveStatusLogForMeter(statusBuilder.entityId(physicalMeter.id).build());

    PhysicalMeter physicalMeterWithAlarm = physicalMeters.save(
      physicalMeter().logicalMeterId(logicalMeter2.id).build()
    );

    saveStatusLogForMeter(statusBuilder.entityId(physicalMeterWithAlarm.id).build());

    meterAlarmLogs.save(AlarmLogEntry.builder()
      .entityId(physicalMeterWithAlarm.id)
      .mask(12)
      .start(start)
      .build());

    Page<PagedLogicalMeterDto> paginatedLogicalMeters = asUser()
      .getPage(
        Url.builder()
          .path("/meters")
          .parameter(AFTER, start)
          .parameter(BEFORE, start.plusHours(9))
          .parameter(ALARM, "yes")
          .build(),
        PagedLogicalMeterDto.class
      );

    assertThat(paginatedLogicalMeters.getTotalElements()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getTotalPages()).isEqualTo(1);

    PagedLogicalMeterDto result = paginatedLogicalMeters.getContent().get(0);
    assertThat(result.id).isEqualTo(logicalMeter2.id);
    assertThat(result.alarm.mask).isEqualTo(12);
    assertThat(result.alarm.description).isNull();
  }

  @Test
  public void onlyMetersWithNoAlarms() {
    LogicalMeter logicalMeter1 = saveLogicalMeter();
    LogicalMeter logicalMeter2 = saveLogicalMeter();
    StatusLogEntry.StatusLogEntryBuilder<UUID> statusBuilder = StatusLogEntry.<UUID>builder()
      .status(StatusType.OK)
      .start(start);

    PhysicalMeter physicalMeter = physicalMeters.save(
      physicalMeter().logicalMeterId(logicalMeter1.id).build()
    );

    saveStatusLogForMeter(statusBuilder.entityId(physicalMeter.id).build());

    PhysicalMeter physicalMeterWithAlarm = physicalMeters.save(
      physicalMeter().logicalMeterId(logicalMeter2.id).build()
    );

    saveStatusLogForMeter(statusBuilder.entityId(physicalMeterWithAlarm.id).build());

    meterAlarmLogs.save(AlarmLogEntry.builder()
      .entityId(physicalMeterWithAlarm.id)
      .mask(12)
      .start(start)
      .build());

    Page<PagedLogicalMeterDto> paginatedLogicalMeters = asUser()
      .getPage(
        Url.builder()
          .path("/meters")
          .parameter(AFTER, start)
          .parameter(BEFORE, start.plusHours(9))
          .parameter(ALARM, "no")
          .build(),
        PagedLogicalMeterDto.class
      );

    assertThat(paginatedLogicalMeters.getTotalElements()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getTotalPages()).isEqualTo(1);

    PagedLogicalMeterDto result = paginatedLogicalMeters.getContent().get(0);
    assertThat(result.id).isEqualTo(logicalMeter1.id);
    assertThat(result.alarm).isNull();
  }

  @Test
  public void meterShouldHaveOneActiveAlarm() {
    LogicalMeter logicalMeter = saveLogicalMeter(DISTRICT_HEATING_METER);

    PhysicalMeter physicalMeter = physicalMeters.save(
      physicalMeter()
        .logicalMeterId(logicalMeter.id)
        .externalId("123123")
        .build()
    );

    AlarmLogEntry alarm = meterAlarmLogs.save(AlarmLogEntry.builder()
      .entityId(physicalMeter.id)
      .mask(12)
      .start(start)
      .description("something is wrong")
      .build());

    Page<PagedLogicalMeterDto> paginatedLogicalMeters = asUser()
      .getPage(
        metersUrl(start, start.plusHours(1)),
        PagedLogicalMeterDto.class
      );

    assertThat(paginatedLogicalMeters.getTotalElements()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getTotalPages()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getContent().get(0).alarm)
      .isEqualTo(new AlarmDto(alarm.id, alarm.mask));
  }

  @Test
  public void meterShouldHaveNoAlarmWhenThereIsNoActive() {
    LogicalMeter logicalMeter = saveLogicalMeter(DISTRICT_HEATING_METER);

    PhysicalMeter physicalMeter = physicalMeters.save(
      physicalMeter()
        .logicalMeterId(logicalMeter.id)
        .externalId("123123")
        .build()
    );

    meterAlarmLogs.save(AlarmLogEntry.builder()
      .entityId(physicalMeter.id)
      .mask(112)
      .start(start)
      .stop(start.plusHours(3))
      .description("something is wrong")
      .build());

    meterAlarmLogs.save(AlarmLogEntry.builder()
      .entityId(physicalMeter.id)
      .mask(122)
      .start(start)
      .stop(start.plusHours(4))
      .description("testing")
      .build());

    var response = asUser()
      .getPage(
        metersUrl(start.plusHours(4), start.plusHours(5)),
        PagedLogicalMeterDto.class
      );

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent().get(0).alarm).isNull();
  }

  @Test
  public void meterShouldHaveLastActiveAlarm() {
    LogicalMeter logicalMeter = saveLogicalMeter(DISTRICT_HEATING_METER);

    PhysicalMeter physicalMeter = physicalMeters.save(
      physicalMeter()
        .logicalMeterId(logicalMeter.id)
        .externalId("123123")
        .build()
    );

    meterAlarmLogs.save(AlarmLogEntry.builder()
      .entityId(physicalMeter.id)
      .mask(112)
      .start(start)
      .stop(start.plusHours(3))
      .description("something is wrong")
      .build());

    AlarmLogEntry activeAlarm = meterAlarmLogs.save(AlarmLogEntry.builder()
      .entityId(physicalMeter.id)
      .mask(122)
      .start(start.plusHours(2))
      .description("testing")
      .build());

    Page<PagedLogicalMeterDto> paginatedLogicalMeters = asUser()
      .getPage(
        metersUrl(start, start.plusHours(4)),
        PagedLogicalMeterDto.class
      );

    assertThat(paginatedLogicalMeters.getTotalElements()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getTotalPages()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getContent().get(0).alarm)
      .isEqualTo(new AlarmDto(activeAlarm.id, activeAlarm.mask));
  }

  private void createMeterWithGateway(String meterExternalId, String gatewaySerial) {
    LogicalMeterEntity logicalMeterEntity = new LogicalMeterEntity(
      randomUUID(),
      meterExternalId,
      context().organisationId(),
      ZonedDateTime.now(),
      MeterDefinitionEntityMapper.toEntity(UNKNOWN_METER)
    );

    logicalMeterJpaRepository.save(logicalMeterEntity);

    GatewayEntity gatewayEntity = newGatewayEntity(context().organisationEntity.id, gatewaySerial);
    gatewayEntity.meters = new HashSet<>();
    gatewayEntity.meters.add(logicalMeterEntity);
    gatewayJpaRepository.save(gatewayEntity);

    logicalMeterEntity.gateways = new HashSet<>();
    logicalMeterEntity.gateways.add(gatewayEntity);
    logicalMeterJpaRepository.save(logicalMeterEntity);
  }

  private void assertNothingIsRemoved(
    LogicalMeter districtHeatingMeter,
    PhysicalMeter physicalMeter
  ) {
    Optional<LogicalMeterEntity> logicalMeterEntity = logicalMeterJpaRepository
      .findById(districtHeatingMeter.id);

    assertThat(logicalMeterEntity)
      .as("Logical meter should not be removed")
      .isPresent();

    assertThat(logicalMeterEntity.get().physicalMeters.size())
      .as("Physical meter should not be removed")
      .isEqualTo(1);

    List<MeasurementEntity> measurements = measurementJpaRepository.findAll(
      QMeasurementEntity.measurementEntity.id.physicalMeter.id.eq(physicalMeter.id)
    );

    assertThat(measurements.size())
      .as("Measurements should not be removed")
      .isEqualTo(1);
  }

  private void addMeasurementsForMeter(
    PhysicalMeter physicalMeter,
    Set<Quantity> quantities,
    ZonedDateTime start,
    Duration periodDuration,
    Long minuteInterval
  ) {
    addMeasurementsForMeter(
      physicalMeter,
      quantities,
      start,
      periodDuration,
      minuteInterval,
      0
    );
  }

  private void addMeasurementsForMeter(
    PhysicalMeter physicalMeter,
    Set<Quantity> quantities,
    ZonedDateTime start,
    Duration periodDuration,
    Long minuteInterval,
    double valueIncrementation
  ) {
    ZonedDateTime now = start;
    double incrementedValue = 1.0;
    while (now.isBefore(start.plus(periodDuration))) {
      addMeasurementsForMeterQuantities(physicalMeter, quantities, now, incrementedValue);
      now = now.plusMinutes(minuteInterval);
      incrementedValue += valueIncrementation;
    }
  }

  private void addMeasurementsForMeterQuantities(
    PhysicalMeter physicalMeter,
    Set<Quantity> quantities,
    ZonedDateTime when,
    double value
  ) {
    for (Quantity quantity : quantities) {
      measurements.save(Measurement.builder()
        .created(when)
        .quantity(quantity.name)
        .value(value)
        .unit(quantity.presentationUnit())
        .physicalMeter(physicalMeter)
        .build()
      );
    }
  }

  private LogicalMeter saveLogicalMeter(ZonedDateTime dateTime) {
    return logicalMeters.save(logicalMeterBuilder(DISTRICT_HEATING_METER)
      .created(dateTime)
      .build());
  }

  private LogicalMeter saveLogicalMeter() {
    return saveLogicalMeter(UNKNOWN_METER);
  }

  private LogicalMeter saveLogicalMeter(MeterDefinition meterDefinition) {
    return logicalMeters.save(buildLogicalMeter(meterDefinition));
  }

  private LogicalMeter buildLogicalMeter(MeterDefinition meterDefinition) {
    return logicalMeterBuilder(meterDefinition).build();
  }

  private LogicalMeter.LogicalMeterBuilder logicalMeterBuilder(MeterDefinition meterDefinition) {
    UUID meterId = randomUUID();
    return LogicalMeter.builder()
      .id(meterId)
      .externalId(meterId.toString())
      .organisationId(context().organisationId())
      .meterDefinition(meterDefinition);
  }

  private StatusLogEntry<UUID> saveStatusLogForMeter(StatusLogEntry<UUID> statusLog) {
    return meterStatusLogs.save(statusLog);
  }

  private UserBuilder userBuilder() {
    return new UserBuilder()
      .name("Me")
      .email("me@myorg.com")
      .password("secr3t")
      .organisation(context().organisation2())
      .asUser();
  }

  private void testSorting(
    String url,
    String errorMessage,
    Function<PagedLogicalMeterDto, String> actual,
    String expected
  ) {
    Page<PagedLogicalMeterDto> response = asUser()
      .getPage(url, PagedLogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(55);

    assertThat(actual.apply(response.getContent().get(0)))
      .as(errorMessage)
      .isEqualTo(expected);
  }

  private PhysicalMeterBuilder physicalMeter() {
    return PhysicalMeter.builder()
      .organisation(context().organisation())
      .address("111-222-333-444-1")
      .externalId(randomUUID().toString())
      .medium("Heat")
      .manufacturer("ELV1");
  }

  private static UrlTemplate metersUrl(ZonedDateTime after, ZonedDateTime before) {
    return Url.builder()
      .path("/meters")
      .parameter(AFTER, after)
      .parameter(BEFORE, before)
      .build();
  }

  private static GatewayEntity newGatewayEntity(UUID organisationId, String serial) {
    return new GatewayEntity(
      randomUUID(),
      organisationId,
      serial,
      "",
      emptySet()
    );
  }

  private static void assertThatStatusIsNotFound(ResponseEntity<ErrorMessageDto> response) {
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
