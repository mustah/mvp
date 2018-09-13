package com.elvaco.mvp.web;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Language;
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
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.usecase.MeasurementUseCases;
import com.elvaco.mvp.core.util.Dates;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.QMeasurementEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.repository.jpa.GatewayJpaRepository;
import com.elvaco.mvp.database.repository.jpa.GatewayStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepositoryImpl;
import com.elvaco.mvp.database.repository.jpa.MeterAlarmLogJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MissingMeasurementJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionEntityMapper;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testing.fixture.UserBuilder;
import com.elvaco.mvp.web.dto.AlarmDto;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.MeasurementDto;
import com.elvaco.mvp.web.dto.MeterStatusLogDto;
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
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.GAS_METER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.HOT_WATER_METER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.UNKNOWN_METER;
import static com.elvaco.mvp.core.domainmodels.StatusType.ERROR;
import static com.elvaco.mvp.core.domainmodels.StatusType.OK;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

public class LogicalMeterControllerTest extends IntegrationTest {

  private static final ZonedDateTime NOW = ZonedDateTime.now();
  private static final ZonedDateTime YESTERDAY = ZonedDateTime.now()
    .minusDays(1)
    .truncatedTo(ChronoUnit.DAYS);

  private MeterDefinition hotWaterMeterDefinition;

  @Autowired
  private LogicalMeters logicalMeters;

  @Autowired
  private LogicalMeterJpaRepository logicalMeterJpaRepository;

  @Autowired
  private PhysicalMeterJpaRepository physicalMeterJpaRepository;

  @Autowired
  private MeasurementJpaRepositoryImpl measurementJpaRepository;

  @Autowired
  private MeasurementUseCases measurementUseCases;

  @Autowired
  private MeterDefinitions meterDefinitions;

  @Autowired
  private PhysicalMeters physicalMeters;

  @Autowired
  private Gateways gateways;

  @Autowired
  private GatewayJpaRepository gatewayJpaRepository;

  @Autowired
  private GatewayStatusLogJpaRepository gatewayStatusLogJpaRepository;

  @Autowired
  private MeterStatusLogs meterStatusLogs;

  @Autowired
  private MeterAlarmLogs meterAlarmLogs;

  @Autowired
  private MeterAlarmLogJpaRepository meterAlarmLogJpaRepository;

  @Autowired
  private PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository;

  @Autowired
  private MissingMeasurementJpaRepository missingMeasurementJpaRepository;

  private ZonedDateTime start;

  @Before
  public void setUp() {
    start = ZonedDateTime.parse("2001-01-01T00:00:00.00Z");

    hotWaterMeterDefinition = meterDefinitions.save(MeterDefinition.HOT_WATER_METER);
  }

  @After
  public void tearDown() {
    measurementJpaRepository.deleteAll();
    physicalMeterStatusLogJpaRepository.deleteAll();
    physicalMeterJpaRepository.deleteAll();
    meterAlarmLogJpaRepository.deleteAll();
    gatewayStatusLogJpaRepository.deleteAll();
    gatewayJpaRepository.deleteAll();
    logicalMeterJpaRepository.deleteAll();
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

    Page<PagedLogicalMeterDto> paginatedLogicalMeters = asTestUser()
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
  public void shouldNotHaveStatusChangedSetWhenMeterCreatedAfterPeriodEnd() {
    ZonedDateTime start = ZonedDateTime.now();

    LogicalMeter logicalMeter = logicalMeters.save(LogicalMeter.builder()
      .externalId("externalId")
      .organisationId(context().organisationId())
      .meterDefinition(GAS_METER)
      .created(start)
      .build()
    );

    PhysicalMeter firstMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(logicalMeter.id)
      .externalId("meter-one")
      .readIntervalMinutes(15)
      .build()
    );

    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(firstMeter.id)
        .status(OK)
        .start(start)
        .build()
    );

    List<PagedLogicalMeterDto> content = asTestUser()
      .getPage(metersUrl(start, start.minusDays(7)), PagedLogicalMeterDto.class)
      .getContent();

    assertThat(content).extracting("statusChanged")
      .containsExactlyElementsOf(singletonList(null));
  }

  @Test
  public void locationIsAttachedToPagedMeter() {
    UUID meterId = UUID.randomUUID();
    logicalMeters.save(LogicalMeter.builder()
      .id(meterId)
      .externalId(meterId.toString())
      .organisationId(context().organisationId())
      .meterDefinition(UNKNOWN_METER)
      .location(new LocationBuilder().city("kungsbacka")
        .country("sweden")
        .address("kabelgatan 2t")
        .build())
      .build());

    PagedLogicalMeterDto logicalMeterDto = asTestUser()
      .getPage("/meters", PagedLogicalMeterDto.class)
      .getContent().get(0);

    assertThat(logicalMeterDto.location.city.name).isEqualTo("kungsbacka");
    assertThat(logicalMeterDto.location.address.name).isEqualTo("kabelgatan 2t");
  }

  @Test
  public void meterDefinitionIsAttachedToPagedMeter() {
    LogicalMeter districtHeatingMeter = saveLogicalMeter(DISTRICT_HEATING_METER);

    physicalMeters.save(physicalMeter()
      .logicalMeterId(districtHeatingMeter.id)
      .externalId(randomUUID().toString())
      .readIntervalMinutes(30)
      .build());

    PagedLogicalMeterDto logicalMeterDto = asTestUser()
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

    PagedLogicalMeterDto logicalMeterDto = asTestUser()
      .getPage(metersUrl(YESTERDAY, YESTERDAY.plusHours(1)), PagedLogicalMeterDto.class)
      .getContent()
      .get(0);

    assertThat(logicalMeterDto.collectionPercentage).isEqualTo(0.0);
  }

  @Test
  public void collectionStatusFiftyPercent() {
    assumeTrue(isPostgresDialect());

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

    Page<PagedLogicalMeterDto> response = asTestUser()
      .getPage(metersUrl(YESTERDAY, YESTERDAY.plusHours(1)), PagedLogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).hasSize(1);
    assertThat(response.getContent().get(0).collectionPercentage).isEqualTo(50.0);
  }

  @Test
  public void collectionStatusFiftyPercentWhenMeterHasStatuses() {
    assumeTrue(isPostgresDialect());

    LogicalMeter districtHeatingMeter = saveLogicalMeter(
      YESTERDAY.minusMinutes(15)
    );

    PhysicalMeter physicalMeter = physicalMeters.save(physicalMeter()
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
        .stop(YESTERDAY.plusHours(5))
        .build()
    );

    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(physicalMeter.id)
        .status(StatusType.WARNING)
        .start(YESTERDAY.plusHours(5))
        .build()
    );

    addMeasurementsForMeter(
      physicalMeter,
      districtHeatingMeter.getQuantities(),
      YESTERDAY,
      Duration.ofHours(2),
      60L,
      1.0,
      1.0
    );

    missingMeasurementJpaRepository.refreshLocked();

    PagedLogicalMeterDto logicalMeterDto = asTestUser()
      .getPage(metersUrl(YESTERDAY, YESTERDAY.plusHours(4)), PagedLogicalMeterDto.class)
      .getContent()
      .get(0);

    assertThat(logicalMeterDto.collectionPercentage).isEqualTo(50.0);
  }

  @Test
  public void collectionStatusFiftyPercentWhenMeterHasMultipleActiveStatusesWithinPeriod() {
    assumeTrue(isPostgresDialect());

    LogicalMeter districtHeatingMeter = saveLogicalMeter(
      YESTERDAY.minusMinutes(15)
    );

    PhysicalMeter physicalMeter = physicalMeters.save(physicalMeter()
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
      1.0,
      1.0
    );

    missingMeasurementJpaRepository.refreshLocked();

    PagedLogicalMeterDto logicalMeterDto = asTestUser()
      .getPage(metersUrl(YESTERDAY, YESTERDAY.plusHours(4)), PagedLogicalMeterDto.class)
      .getContent()
      .get(0);

    assertThat(logicalMeterDto.collectionPercentage).isEqualTo(50.0);
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

    PagedLogicalMeterDto logicalMeterDto = asTestUser()
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

    PagedLogicalMeterDto logicalMeterDto = asTestUser()
      .getPage("/meters", PagedLogicalMeterDto.class)
      .getContent()
      .get(0);

    assertThat(logicalMeterDto.manufacturer).isEqualTo("KAKA");
  }

  @Test
  public void gatewayIsSetOnPagedMeter() {
    Gateway gateway = gateways.save(new Gateway(
      randomUUID(),
      context().organisationId(),
      "gateway-serial",
      "gateway-product"
    ));

    LogicalMeter districtHeatingMeter = logicalMeters.save(
      LogicalMeter.builder()
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

    PagedLogicalMeterDto logicalMeterDto = asTestUser()
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
      firstMeter.readIntervalMinutes,
      1.0
    );

    addMeasurementsForMeter(
      secondMeter,
      districtHeatingMeter.getQuantities(),
      start.plusHours(1),
      Duration.ofHours(2),
      secondMeter.readIntervalMinutes,
      1.0
    );

    PagedLogicalMeterDto logicalMeterDto = asTestUser()
      .getPage(metersUrl(start, start.plusHours(3)), PagedLogicalMeterDto.class)
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

    List<PagedLogicalMeterDto> meters = asTestUser()
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
    assumeTrue(isPostgresDialect());

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

    PagedLogicalMeterDto logicalMeterDto = asTestUser()
      .getPage(metersUrl(YESTERDAY, YESTERDAY.plusMinutes(45)), PagedLogicalMeterDto.class)
      .getContent()
      .get(0);

    assertThat(logicalMeterDto.collectionPercentage).isEqualTo(33.33333333333333);
  }

  @Test
  public void collectionStatusMeterChangeWithIntervalUpdate() {
    assumeTrue(isPostgresDialect());

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
      physicalMeter2.readIntervalMinutes,
      1.0
    );

    missingMeasurementJpaRepository.refreshLocked();

    List<PagedLogicalMeterDto> pagedMeters = asTestUser()
      .getPage(metersUrl(YESTERDAY, YESTERDAY.plusDays(1)), PagedLogicalMeterDto.class)
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
      60L,
      1.0
    );

    Page<PagedLogicalMeterDto> paginatedLogicalMeters = asTestUser()
      .getPage(metersUrl(start, start.plusDays(1)), PagedLogicalMeterDto.class);

    assertThat(paginatedLogicalMeters.getTotalElements()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getTotalPages()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getContent()).hasSize(1);
    assertThat(paginatedLogicalMeters.getContent().get(0).collectionPercentage).isEqualTo(100.0);
  }

  @Test
  public void findById_WithinDefaultPeriod_WithUnknownStatus() {
    LogicalMeter logicalMeter = saveLogicalMeter();

    ZonedDateTime start = ZonedDateTime.parse("2001-01-01T01:00:00.00Z");

    PhysicalMeter physicalMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(logicalMeter.id)
      .externalId(randomUUID().toString())
      .readIntervalMinutes(60)
      .build()
    );

    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(physicalMeter.id)
        .status(StatusType.UNKNOWN)
        .start(start)
        .build()
    );

    LogicalMeterDto logicalMeterDto = asTestUser()
      .getList(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody()
      .get(0);

    assertThat(logicalMeterDto.isReported).isTrue();
  }

  @Test
  public void findById_WithinExplicitPeriod_WithUnknownStatus() {
    LogicalMeter logicalMeter = saveLogicalMeter();

    ZonedDateTime start = ZonedDateTime.parse("2001-01-01T01:00:00.00Z");

    PhysicalMeter physicalMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(logicalMeter.id)
      .externalId(randomUUID().toString())
      .readIntervalMinutes(60)
      .build()
    );

    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(physicalMeter.id)
        .status(StatusType.UNKNOWN)
        .start(start)
        .build()
    );

    String url = meterDetailsUrl(logicalMeter.id) + "&before=" + NOW + "&after=" + YESTERDAY;
    LogicalMeterDto logicalMeterDto = asTestUser()
      .getList(url, LogicalMeterDto.class)
      .getBody()
      .get(0);

    assertThat(logicalMeterDto.isReported).isTrue();
  }

  @Test
  public void findById_WithinPeriod_ShouldBeNotReportedWhenOkStatus() {
    LogicalMeter logicalMeter = saveLogicalMeter();
    UUID physicalMeterId = randomUUID();

    physicalMeters.save(PhysicalMeter.builder()
      .id(physicalMeterId)
      .organisation(context().organisation())
      .address("address")
      .externalId("external-id")
      .medium("medium")
      .manufacturer("manufacturer")
      .logicalMeterId(logicalMeter.id)
      .build());

    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(physicalMeterId)
        .status(OK)
        .start(YESTERDAY)
        .build()
    );
    LogicalMeterDto logicalMeterDto = asTestUser()
      .getList(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody()
      .get(0);

    assertThat(logicalMeterDto.isReported).isFalse();
  }

  @Test
  public void findById_MeterIncludesStatusChangeLog() {
    LogicalMeter logicalMeter = saveLogicalMeter();

    UUID physicalMeterId = randomUUID();

    physicalMeters.save(PhysicalMeter.builder()
      .id(physicalMeterId)
      .organisation(context().organisation())
      .address("address")
      .externalId("external-id")
      .medium("medium")
      .manufacturer("manufacturer")
      .logicalMeterId(logicalMeter.id)
      .build());

    StatusLogEntry<UUID> logEntry = saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(physicalMeterId)
        .status(OK)
        .start(ZonedDateTime.parse("2001-01-01T10:14:00Z"))
        .stop(ZonedDateTime.parse("2001-01-06T10:14:00Z"))
        .build()
    );

    LogicalMeterDto logicalMeterDto = asTestUser()
      .getList("/meters/details?id=" + logicalMeter.id, LogicalMeterDto.class)
      .getBody()
      .get(0);

    assertThat(logicalMeterDto.statusChangelog).containsExactly(
      new MeterStatusLogDto(
        logEntry.id,
        "ok",
        "2001-01-01T10:14:00Z",
        "2001-01-06T10:14:00Z"
      )
    );
  }

  @Test
  public void findAllPaged() {
    saveLogicalMeter();
    saveLogicalMeter();
    saveLogicalMeter();

    Page<PagedLogicalMeterDto> response = asTestUser()
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

    Page<PagedLogicalMeterDto> response = asTestUser()
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
      (PagedLogicalMeterDto meter) -> meter.location.address.name,
      "Drottninggatan 2"
    );

    // Address desc
    testSorting(
      "/meters?size=20&page=0&sort=address,desc",
      "Unexpected address, sorting failed",
      (PagedLogicalMeterDto meter) -> meter.location.address.name,
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
      (PagedLogicalMeterDto meter) -> meter.location.city.name,
      "Varberg"
    );

    testSorting(
      "/meters?size=20&page=0&sort=city,desc",
      "Unexpected city, sorting failed",
      (PagedLogicalMeterDto meter) -> meter.location.city.name,
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

    Page<PagedLogicalMeterDto> response = asTestUser()
      .getPage(
        "/meters?after=2005-01-10T01:00:00.00Z&before=2015-01-01T23:00:00.00Z&reported=error",
        PagedLogicalMeterDto.class
      );

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getNumberOfElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);

    assertThat(response.getContent().get(0).id).isEqualTo(firstLogicalMeter.id);
  }

  @Test
  public void pagedMeterDetailsIsNotReported() {
    LogicalMeter logicalMeter = saveLogicalMeter();

    PhysicalMeter physicalMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(logicalMeter.id)
      .externalId("meter-one")
      .readIntervalMinutes(15)
      .build()
    );

    ZonedDateTime start = ZonedDateTime.parse("2018-04-03T08:00:00Z");

    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(physicalMeter.id)
        .status(OK)
        .start(start)
        .build()
    );

    List<LogicalMeterDto> response = asTestUser()
      .getList("/meters/details/?id=" + logicalMeter.id, LogicalMeterDto.class)
      .getBody();

    LogicalMeterDto logicalMeterDto = response.get(0);
    assertThat(logicalMeterDto.isReported).isFalse();
    assertThat(logicalMeterDto.statusChanged).isEqualTo(Dates.formatUtc(start));
  }

  @Test
  public void twoPagedMeterDetailsHaveStatuses() {
    LogicalMeter logicalMeter1 = saveLogicalMeter();
    LogicalMeter logicalMeter2 = saveLogicalMeter();

    PhysicalMeter physicalMeter1 = physicalMeters.save(physicalMeter()
      .logicalMeterId(logicalMeter1.id)
      .externalId("meter-one")
      .readIntervalMinutes(15)
      .build()
    );
    PhysicalMeter physicalMeter2 = physicalMeters.save(physicalMeter()
      .logicalMeterId(logicalMeter2.id)
      .externalId("meter-two")
      .readIntervalMinutes(15)
      .build()
    );

    ZonedDateTime start = ZonedDateTime.parse("2018-04-03T08:00:00Z");

    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(physicalMeter1.id)
        .status(OK)
        .start(start)
        .build()
    );
    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(physicalMeter2.id)
        .status(ERROR)
        .start(start)
        .build()
    );

    String url = "/meters/details/?id=" + logicalMeter1.id + "&id=" + logicalMeter2.id;
    List<LogicalMeterDto> logicalMetersResponse = asTestUser()
      .getList(url, LogicalMeterDto.class)
      .getBody();

    String statusChanged = Dates.formatUtc(start);

    assertThat(logicalMetersResponse)
      .extracting("isReported")
      .containsExactlyInAnyOrder(false, true);
    assertThat(logicalMetersResponse)
      .extracting("statusChanged")
      .containsExactlyInAnyOrder(statusChanged, statusChanged);
  }

  @Test
  public void meterIsNotReported_WhenNoReportedStatusExists() {
    LogicalMeter logicalMeter = saveLogicalMeter();

    physicalMeters.save(physicalMeter()
      .logicalMeterId(logicalMeter.id)
      .externalId("meter-one")
      .readIntervalMinutes(15)
      .build()
    );

    LogicalMeterDto logicalMeterDto = asTestUser()
      .getList(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody()
      .get(0);

    assertThat(logicalMeterDto.isReported).isFalse();
  }

  @Test
  public void metersIsNotReportedWhenStatusOk() {
    LogicalMeter logicalMeter = saveLogicalMeter();
    PhysicalMeter physicalMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(logicalMeter.id)
      .externalId("meter-one")
      .readIntervalMinutes(15)
      .build()
    );

    ZonedDateTime start = ZonedDateTime.parse("2018-04-03T08:00:00Z");
    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(physicalMeter.id)
        .status(OK)
        .start(start)
        .build()
    );

    LogicalMeterDto logicalMeterDto = asTestUser()
      .getList(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody()
      .get(0);

    assertThat(logicalMeterDto.isReported).isFalse();
    assertThat(logicalMeterDto.statusChanged).isEqualTo(Dates.formatUtc(start));
  }

  @Test
  public void latestStartedStatusIsSetWhenMeterHasMultipleActiveStatuses() {
    LogicalMeter logicalMeter = saveLogicalMeter();

    PhysicalMeter firstMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(logicalMeter.id)
      .externalId("meter-one")
      .readIntervalMinutes(15)
      .build()
    );

    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(firstMeter.id)
        .status(OK)
        .start(start)
        .build()
    );

    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(firstMeter.id)
        .status(ERROR)
        .start(start.plusMinutes(1))
        .build()
    );

    LogicalMeterDto logicalMeterDto = asTestUser()
      .getList(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody()
      .get(0);

    assertThat(logicalMeterDto.isReported).isTrue();
  }

  @Test
  public void latestStartedStatusIsSetWhenMeterHasMultipleActiveStatusesOnMultiplePhysicalMeters() {
    LogicalMeter logicalMeter = saveLogicalMeter();

    PhysicalMeter firstMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(logicalMeter.id)
      .externalId("meter-one")
      .readIntervalMinutes(15)
      .build()
    );

    PhysicalMeter secondMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(logicalMeter.id)
      .externalId("meter-two")
      .readIntervalMinutes(15)
      .build()
    );

    ZonedDateTime start = ZonedDateTime.parse("2018-04-03T08:00:00Z");
    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(firstMeter.id)
        .status(OK)
        .start(start.plusSeconds(1))
        .build()
    );

    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(secondMeter.id)
        .status(ERROR)
        .start(start)
        .build()
    );

    LogicalMeterDto logicalMeterDto = asTestUser()
      .getList(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody()
      .get(0);

    assertThat(logicalMeterDto.isReported).isFalse();
  }

  @Test
  public void malformedDateParameter() {
    ResponseEntity<ErrorMessageDto> response = asTestUser()
      .get("/meters?after=NotAValidTimestamp&before=AndNeitherIsThis", ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).isEqualTo(
      "Failed to construct filter 'after' for value 'NotAValidTimestamp'");
  }

  @Test
  public void findAllWithPredicates() {
    saveLogicalMeter();
    saveLogicalMeter(MeterDefinition.HOT_WATER_METER);

    Page<PagedLogicalMeterDto> response = asTestUser()
      .getPage("/meters?medium=Hot water", PagedLogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getNumberOfElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
  }

  @Test
  public void findsOwnOrganisationsMetersByFilter() {
    saveLogicalMeter();

    Page<PagedLogicalMeterDto> response = asTestUser()
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
      .getPage("/meters?medium=Hot water", PagedLogicalMeterDto.class);

    assertThat(response.getTotalElements()).isEqualTo(1L);
    assertThat(response.getContent().get(0).id).isEqualTo(myMeter.id);
  }

  @Test
  public void meterNotFound() {
    ResponseEntity<List<LogicalMeterDto>> response = asTestUser()
      .getList(meterDetailsUrl(randomUUID()), LogicalMeterDto.class);

    assertThatStatusIsOk(response);
    assertThat(response.getBody()).isEmpty();
  }

  @Test
  public void findAllMeters_WithGatewaySerial() {
    String serial = "666";
    createMeterWithGateway("my-mapped-meter", serial);
    createMeterWithGateway("my-mapped-meter2", "777");

    Page<PagedLogicalMeterDto> result = asTestUser()
      .getPage("/meters?gatewaySerial=" + serial, PagedLogicalMeterDto.class);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).gatewaySerial).isEqualTo(serial);
  }

  @Test
  public void findAllMeters_WithFacility() {
    String facility = "my-mapped-meter";

    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId(facility)
      .organisationId(context().organisationId())
      .meterDefinition(MeterDefinition.UNKNOWN_METER)
      .location(UNKNOWN_LOCATION)
      .build());

    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("another-mapped-meter")
      .organisationId(context().organisationId())
      .meterDefinition(MeterDefinition.UNKNOWN_METER)
      .location(UNKNOWN_LOCATION)
      .build());

    Page<PagedLogicalMeterDto> result = asTestUser()
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

    Page<PagedLogicalMeterDto> result = asTestUser()
      .getPage("/meters?secondaryAddress=" + secondaryAddress, PagedLogicalMeterDto.class);

    assertThat(result.getContent())
      .as("There should be one meter with secondary address: \"" + secondaryAddress + "\"")
      .hasSize(1);
    assertThat(result.getContent().get(0).address).isEqualTo(secondaryAddress);
  }

  @Test
  public void findAllMeters_WithUnknownCity() {
    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("my-mapped-meter")
      .organisationId(context().organisationId())
      .meterDefinition(MeterDefinition.UNKNOWN_METER)
      .location(UNKNOWN_LOCATION)
      .build());

    Page<PagedLogicalMeterDto> result = asTestUser()
      .getPage("/meters?city=unknown,unknown", PagedLogicalMeterDto.class);

    assertThat(result.getContent()).hasSize(1);
  }

  @Test
  public void findAllMeters_IncludeMetersWith_UnknownCity() {
    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("my-mapped-meter")
      .organisationId(context().organisationId())
      .meterDefinition(MeterDefinition.UNKNOWN_METER)
      .location(UNKNOWN_LOCATION)
      .build());
    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("123-123-123")
      .organisationId(context().organisationId())
      .meterDefinition(MeterDefinition.UNKNOWN_METER)
      .location(kungsbacka().build())
      .build());

    Page<PagedLogicalMeterDto> result = asTestUser()
      .getPage("/meters?city=unknown,unknown&city=sweden,kungsbacka", PagedLogicalMeterDto.class);

    assertThat(result.getContent()).hasSize(2);
  }

  @Test
  public void findAllMeters_IncludeMetersWith_UnknownCity_AndLowConfidence() {
    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("my-mapped-meter")
      .organisationId(context().organisationId())
      .meterDefinition(MeterDefinition.UNKNOWN_METER)
      .location(UNKNOWN_LOCATION)
      .build());

    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("123-123-123")
      .organisationId(context().organisationId())
      .meterDefinition(MeterDefinition.UNKNOWN_METER)
      .location(kungsbacka().build())
      .build());

    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("123-456")
      .organisationId(context().organisationId())
      .meterDefinition(MeterDefinition.UNKNOWN_METER)
      .location(kungsbacka().confidence(0.74).build())
      .build());

    Page<PagedLogicalMeterDto> result = asTestUser()
      .getPage("/meters?city=unknown,unknown&city=sweden,kungsbacka", PagedLogicalMeterDto.class);

    assertThat(result.getContent()).hasSize(3);
  }

  @Test
  public void findAllMetersWithUnknownCity() {
    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("123")
      .organisationId(context().organisationId())
      .meterDefinition(MeterDefinition.UNKNOWN_METER)
      .location(UNKNOWN_LOCATION)
      .build());

    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("123-123-123")
      .organisationId(context().organisationId())
      .meterDefinition(MeterDefinition.UNKNOWN_METER)
      .location(kungsbacka().build())
      .build());

    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("456")
      .organisationId(context().organisationId())
      .meterDefinition(MeterDefinition.UNKNOWN_METER)
      .location(kungsbacka().confidence(0.74).build())
      .build());

    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("789")
      .organisationId(context().organisationId())
      .meterDefinition(MeterDefinition.UNKNOWN_METER)
      .location(kungsbacka()
        .longitude(null)
        .latitude(null)
        .confidence(null).build())
      .build());

    Page<PagedLogicalMeterDto> result = asTestUser()
      .getPage("/meters?city=unknown,unknown", PagedLogicalMeterDto.class);

    assertThat(result.getContent()).extracting("facility")
      .containsExactlyInAnyOrder("123", "456", "789");
  }

  @Test
  public void findAllMeters_WithUnknownAddress() {
    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("abc")
      .organisationId(context().organisationId())
      .meterDefinition(MeterDefinition.UNKNOWN_METER)
      .location(UNKNOWN_LOCATION)
      .build());

    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("123")
      .organisationId(context().organisationId())
      .meterDefinition(MeterDefinition.UNKNOWN_METER)
      .location(kungsbacka().confidence(0.75).build())
      .build());

    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("456")
      .organisationId(context().organisationId())
      .meterDefinition(MeterDefinition.UNKNOWN_METER)
      .location(kungsbacka().confidence(0.80).build())
      .build());

    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("789")
      .organisationId(context().organisationId())
      .meterDefinition(MeterDefinition.UNKNOWN_METER)
      .location(kungsbacka()
        .longitude(null)
        .latitude(null)
        .confidence(null).build())
      .build());

    Page<PagedLogicalMeterDto> result = asTestUser()
      .getPage("/meters?address=unknown,unknown,unknown", PagedLogicalMeterDto.class);

    assertThat(result.getContent()).extracting("facility")
      .containsExactlyInAnyOrder("abc", "789");
  }

  @Test
  public void findAllMetersPaged_WithMeasurementAboveMax() {
    LogicalMeter firstLogicalMeter = saveLogicalMeter();
    PhysicalMeter firstMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(firstLogicalMeter.id)
      .externalId("meter-one")
      .readIntervalMinutes(15)
      .build()
    );

    addMeasurementsForMeter(
      firstMeter,
      Collections.singleton(Quantity.POWER),
      ZonedDateTime.now().minusHours(2),
      Duration.ofHours(3),
      60L,
      2.0,
      1.0
    );

    LogicalMeter secondLogicalMeter = saveLogicalMeter();
    PhysicalMeter secondMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(secondLogicalMeter.id)
      .externalId("meter-two")
      .readIntervalMinutes(15)
      .build()
    );

    addMeasurementsForMeter(
      secondMeter,
      Collections.singleton(Quantity.POWER),
      ZonedDateTime.now().minusHours(2),
      Duration.ofHours(3),
      60L,
      3.0,
      1.0
    );

    Page<PagedLogicalMeterDto> page = asTestUser()
      .getPage("/meters?quantity=Power&maxValue=4.0 W", PagedLogicalMeterDto.class);

    assertThat(page.getTotalElements()).isEqualTo(1);
    assertThat(page.getContent().get(0).id).isEqualTo(secondLogicalMeter.id);
  }

  @Test
  public void findAllMetersPaged_WithMeasurementBelowMin() {
    LogicalMeter firstLogicalMeter = saveLogicalMeter();
    PhysicalMeter firstMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(firstLogicalMeter.id)
      .externalId("meter-one")
      .readIntervalMinutes(15)
      .build()
    );

    addMeasurementsForMeter(
      firstMeter,
      Collections.singleton(Quantity.POWER),
      ZonedDateTime.now().minusHours(2),
      Duration.ofHours(3),
      60L,
      2.0,
      1.0
    );

    LogicalMeter secondLogicalMeter = saveLogicalMeter();
    PhysicalMeter secondMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(secondLogicalMeter.id)
      .externalId("meter-two")
      .readIntervalMinutes(15)
      .build()
    );

    addMeasurementsForMeter(
      secondMeter,
      Collections.singleton(Quantity.POWER),
      ZonedDateTime.now().minusHours(2),
      Duration.ofHours(3),
      60L,
      3.0,
      1.0
    );

    Page<PagedLogicalMeterDto> page = asTestUser()
      .getPage("/meters?quantity=Power&minValue=3.0 W", PagedLogicalMeterDto.class);

    assertThat(page.getTotalElements()).isEqualTo(1);
    assertThat(page.getContent().get(0).id).isEqualTo(firstLogicalMeter.id);
  }

  @Test
  public void findById_MeterContainsLatestMeasurements() {
    LogicalMeter logicalMeter = saveLogicalMeter(DISTRICT_HEATING_METER);

    PhysicalMeter physicalMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(logicalMeter.id)
      .externalId("bowie")
      .readIntervalMinutes(15)
      .build());

    saveStatusLogForMeter(
      StatusLogEntry.<UUID>builder()
        .entityId(physicalMeter.id)
        .status(StatusType.OK)
        .start(start)
        .build()
    );

    Set<Quantity> quantitiesWithoutDiffTemperature = new HashSet<>(asList(
      Quantity.ENERGY,
      Quantity.VOLUME,
      Quantity.VOLUME_FLOW,
      Quantity.POWER,
      Quantity.FORWARD_TEMPERATURE,
      Quantity.RETURN_TEMPERATURE
    ));

    addMeasurementsForMeter(
      physicalMeter,
      quantitiesWithoutDiffTemperature,
      ZonedDateTime.now().minusDays(3),
      Duration.ofDays(1),
      60L,
      1.0
    );

    addMeasurementsForMeter(
      physicalMeter,
      Collections.singleton(Quantity.POWER),
      ZonedDateTime.now(),
      Duration.ofDays(1),
      60L,
      2.0
    );

    ZonedDateTime start = NOW.minusDays(10);
    ZonedDateTime stop = NOW.plusHours(1);
    String url = meterDetailsUrl(logicalMeter.id) + "&after=" + start + "&before=" + stop;
    ResponseEntity<List<LogicalMeterDto>> response = asTestUser()
      .getList(url, LogicalMeterDto.class);

    List<LogicalMeterDto> meters = response.getBody();
    List<MeasurementDto> measurements = meters.get(0).measurements;

    assertThatStatusIsOk(response);
    assertThat(meters).hasSize(1);
    assertThat(measurements)
      .as("The difference temperature is missing")
      .hasSize(DISTRICT_HEATING_METER.quantities.size() - 1)
      .anyMatch(m -> m.quantity.equals(Quantity.ENERGY.name))
      .anyMatch(m -> m.quantity.equals(Quantity.VOLUME.name))
      .anyMatch(m -> m.quantity.equals(Quantity.POWER.name))
      .anyMatch(m -> m.quantity.equals(Quantity.FORWARD_TEMPERATURE.name))
      .anyMatch(m -> m.quantity.equals(Quantity.RETURN_TEMPERATURE.name))
      .noneMatch(m -> m.quantity.equals(Quantity.DIFFERENCE_TEMPERATURE.name));

    List<MeasurementDto> power = measurements.stream()
      .filter(m -> m.quantity.equals(Quantity.POWER.name))
      .collect(toList());

    assertThat(power)
      .as("Not showing duplicate values for a quantity")
      .hasSize(1);

    assertThat(power.get(0).value)
      .as("Only showing the latest value for a quantity")
      .isEqualTo(2.0);
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
      60L,
      1.0
    );

    ResponseEntity<ErrorMessageDto> response = asTestUser()
      .delete("/meters/" + districtHeatingMeter.id, ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    assertNothingIsRemoved(districtHeatingMeter, physicalMeter);
  }

  @Test
  public void removingLogicalMeter_ShouldNotLeakInformation() {
    ResponseEntity<ErrorMessageDto> response = asTestUser()
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
      60L,
      1.0
    );

    ResponseEntity<Unauthorized> response = asTestAdmin()
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
      60L,
      1.0
    );

    ResponseEntity<LogicalMeterDto> response = asTestSuperAdmin()
      .delete("/meters/" + districtHeatingMeter.id, LogicalMeterDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(logicalMeters.findById(districtHeatingMeter.id)).isEmpty();
    assertThat(physicalMeterJpaRepository.findById(physicalMeter.id)).isEmpty();
    assertThat(measurementUseCases.findBy(physicalMeter.id, Quantity.VOLUME.name, date)).isEmpty();
  }

  @Test
  public void superAdminRemoveNonExistingLogicalMeter() {
    ResponseEntity<ErrorMessageDto> response = asTestSuperAdmin()
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

    JsonNode logicalMeterJson = asTestUser()
      .getJson("/meters/" + meterId);

    assertThat(logicalMeterJson.has("collectionPercentage")).isFalse();
  }

  @Test
  public void wildcardSearchMatchesFacilityStart() {
    UUID meterId = UUID.randomUUID();
    logicalMeters.save(LogicalMeter.builder()
      .id(meterId)
      .externalId("abcdef")
      .organisationId(context().organisationId())
      .meterDefinition(DISTRICT_HEATING_METER)
      .location(UNKNOWN_LOCATION)
      .build());

    Page<PagedLogicalMeterDto> page = asTestUser().getPage(
      "/meters?w=abc",
      PagedLogicalMeterDto.class
    );

    assertThat(page).hasSize(1);
    PagedLogicalMeterDto logicalMeterDto = page.getContent().get(0);
    assertThat(logicalMeterDto.facility).isEqualTo("abcdef");
  }

  @Test
  public void wildcardSearchMatchesCityStart() {
    UUID meterId = UUID.randomUUID();
    logicalMeters.save(LogicalMeter.builder()
      .id(meterId)
      .externalId(meterId.toString())
      .organisationId(context().organisationId())
      .meterDefinition(DISTRICT_HEATING_METER)
      .location(new LocationBuilder().city("ringhals").build())
      .build());

    Page<PagedLogicalMeterDto> page = asTestUser().getPage(
      "/meters?w=ring",
      PagedLogicalMeterDto.class
    );

    assertThat(page).hasSize(1);
    PagedLogicalMeterDto logicalMeterDto = page.getContent().get(0);
    assertThat(logicalMeterDto.location.city.name).isEqualTo("ringhals");
  }

  @Test
  public void wildcardSearchMatchesAddressStart() {
    UUID meterId = UUID.randomUUID();
    logicalMeters.save(LogicalMeter.builder()
      .id(meterId)
      .externalId(meterId.toString())
      .organisationId(context().organisationId())
      .meterDefinition(DISTRICT_HEATING_METER)
      .location(new LocationBuilder().city("ringhals").address("storgatan 34").build())
      .build());

    Page<PagedLogicalMeterDto> page = asTestUser().getPage(
      "/meters?w=storgat",
      PagedLogicalMeterDto.class
    );

    assertThat(page).hasSize(1);
    PagedLogicalMeterDto logicalMeterDto = page.getContent().get(0);
    assertThat(logicalMeterDto.location.address.name).isEqualTo("storgatan 34");
  }

  @Test
  public void wildcardSearchMatchesManufacturerStart() {
    UUID meterId = randomUUID();

    logicalMeters.save(LogicalMeter.builder()
      .id(meterId)
      .externalId(meterId.toString())
      .organisationId(context().organisationId())
      .meterDefinition(DISTRICT_HEATING_METER)
      .location(UNKNOWN_LOCATION)
      .build());

    physicalMeters.save(PhysicalMeter.builder()
      .organisation(context().organisation())
      .externalId(randomUUID().toString())
      .manufacturer("ELV")
      .address("1234")
      .logicalMeterId(meterId)
      .build());

    Page<PagedLogicalMeterDto> page = asTestUser().getPage(
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
      .location(UNKNOWN_LOCATION)
      .build());

    Page<PagedLogicalMeterDto> page = asTestUser().getPage(
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
      .meterDefinition(HOT_WATER_METER)
      .location(UNKNOWN_LOCATION)
      .build());

    physicalMeters.save(PhysicalMeter.builder()
      .organisation(context().organisation())
      .externalId(randomUUID().toString())
      .manufacturer("ELV")
      .address("032123")
      .logicalMeterId(logicalMeterId)
      .build());

    Page<PagedLogicalMeterDto> page = asTestUser()
      .getPage("/meters?w=03", PagedLogicalMeterDto.class);

    assertThat(page).hasSize(1);
    assertThat(page.getContent()).extracting("address").containsExactly("032123");
  }

  @Test
  public void wildcardSearchDoesNotReturnNonMatches() {
    UUID meterId = randomUUID();
    LogicalMeter.LogicalMeterBuilder builder = LogicalMeter.builder()
      .id(meterId)
      .organisationId(context().organisationId())
      .meterDefinition(HOT_WATER_METER)
      .location(UNKNOWN_LOCATION);

    logicalMeters.save(builder.externalId("first facility").build());
    logicalMeters.save(builder.externalId("second facility").build());

    Page<PagedLogicalMeterDto> page = asTestUser()
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
      .meterDefinition(HOT_WATER_METER)
      .location(new LocationBuilder().city("city town").address("street road 1").build())
      .build());

    physicalMeters.save(PhysicalMeter.builder()
      .organisation(context().organisation())
      .address("12345")
      .externalId(randomUUID().toString())
      .manufacturer("stre")
      .logicalMeterId(meterId)
      .build());

    Page<PagedLogicalMeterDto> page = asTestUser().getPage(
      "/meters?w=str",
      PagedLogicalMeterDto.class
    );

    assertThat(page).hasSize(1);
  }

  @Test
  public void wildcardSearchReturnsAllMatches() {
    UUID meterIdOne = UUID.randomUUID();
    logicalMeters.save(LogicalMeter.builder()
      .id(meterIdOne)
      .externalId(meterIdOne.toString())
      .organisationId(context().organisationId())
      .meterDefinition(HOT_WATER_METER)
      .location(new LocationBuilder().address("street 1").build())
      .build());

    physicalMeters.save(PhysicalMeter.builder()
      .organisation(context().organisation())
      .address("12345")
      .externalId(randomUUID().toString())
      .logicalMeterId(meterIdOne)
      .build());

    UUID meterIdTwo = UUID.randomUUID();
    logicalMeters.save(LogicalMeter.builder()
      .id(meterIdTwo)
      .externalId("street facility")
      .organisationId(context().organisationId())
      .meterDefinition(HOT_WATER_METER)
      .location(UNKNOWN_LOCATION)
      .build());

    physicalMeters.save(PhysicalMeter.builder()
      .organisation(context().organisation())
      .address("12345")
      .externalId(randomUUID().toString())
      .logicalMeterId(meterIdTwo)
      .build());

    Page<PagedLogicalMeterDto> page = asTestUser().getPage(
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
      .meterDefinition(HOT_WATER_METER)
      .location(new LocationBuilder().address("street 1").build())
      .build());

    physicalMeters.save(PhysicalMeter.builder()
      .organisation(context().organisation())
      .address("12345")
      .externalId(randomUUID().toString())
      .logicalMeterId(meterId)
      .build());

    List<PagedLogicalMeterDto> content = asTestUser().getPage(
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

    Page<PagedLogicalMeterDto> paginatedLogicalMeters = asTestUser()
      .getPage(metersUrl(start, start.plusHours(1)), PagedLogicalMeterDto.class);

    assertThat(paginatedLogicalMeters.getTotalElements()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getTotalPages()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getContent().get(0).alarm).isNull();
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

    Page<PagedLogicalMeterDto> paginatedLogicalMeters = asTestUser()
      .getPage(metersUrl(start, start.plusHours(1)), PagedLogicalMeterDto.class);

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

    Page<PagedLogicalMeterDto> paginatedLogicalMeters = asTestUser()
      .getPage(metersUrl(start, start.plusHours(1)), PagedLogicalMeterDto.class);

    assertThat(paginatedLogicalMeters.getTotalElements()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getTotalPages()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getContent().get(0).alarm).isNull();
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

    Page<PagedLogicalMeterDto> paginatedLogicalMeters = asTestUser()
      .getPage(metersUrl(start, start.plusHours(4)), PagedLogicalMeterDto.class);

    assertThat(paginatedLogicalMeters.getTotalElements()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getTotalPages()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getContent().get(0).alarm)
      .isEqualTo(new AlarmDto(activeAlarm.id, activeAlarm.mask));
  }

  @Test
  public void findById_ShouldHaveMeterWithAlarm() {
    LogicalMeter logicalMeter = saveLogicalMeter();

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

    LogicalMeterDto logicalMeterDto = asTestUser()
      .getList(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody()
      .get(0);

    assertThat(logicalMeterDto.alarm).isEqualTo(new AlarmDto(
      alarm.id,
      alarm.mask,
      alarm.description
    ));
  }

  @Test
  public void findById_ShouldHaveMeterWithLatestActiveAlarm() {
    LogicalMeter logicalMeter = saveLogicalMeter();

    PhysicalMeter physicalMeter = physicalMeters.save(
      physicalMeter()
        .logicalMeterId(logicalMeter.id)
        .externalId("123123")
        .build()
    );

    AlarmLogEntry alarm1 = meterAlarmLogs.save(AlarmLogEntry.builder()
      .entityId(physicalMeter.id)
      .mask(12)
      .start(start.plusHours(2))
      .description("something is wrong")
      .build());

    meterAlarmLogs.save(AlarmLogEntry.builder()
      .entityId(physicalMeter.id)
      .mask(33)
      .start(start)
      .description("testing")
      .build());

    LogicalMeterDto logicalMeterDto = asTestUser()
      .getList(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody()
      .get(0);

    assertThat(logicalMeterDto.alarm).isEqualTo(new AlarmDto(
      alarm1.id,
      alarm1.mask,
      alarm1.description
    ));
  }

  @Test
  public void findById_ShouldNotHaveMeterWithAlarm() {
    LogicalMeter logicalMeter = saveLogicalMeter();

    physicalMeters.save(
      physicalMeter()
        .logicalMeterId(logicalMeter.id)
        .externalId("123123")
        .build()
    );

    LogicalMeterDto logicalMeterDto = asTestUser()
      .getList(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody()
      .get(0);

    assertThat(logicalMeterDto.alarm).isNull();
  }

  private void createMeterWithGateway(String meterExternalId, String gatewaySerial) {
    LogicalMeterEntity logicalMeterEntity = new LogicalMeterEntity(
      randomUUID(),
      meterExternalId,
      context().organisationId(),
      ZonedDateTime.now(),
      MeterDefinitionEntityMapper.toEntity(MeterDefinition.UNKNOWN_METER)
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
    Long minuteInterval,
    double value
  ) {
    addMeasurementsForMeter(
      physicalMeter,
      quantities,
      start,
      periodDuration,
      minuteInterval,
      value,
      0
    );
  }

  private void addMeasurementsForMeter(
    PhysicalMeter physicalMeter,
    Set<Quantity> quantities,
    ZonedDateTime start,
    Duration periodDuration,
    Long minuteInterval,
    double value,
    double valueIncrementation
  ) {
    ZonedDateTime now = start;
    double incrementedValue = value;
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
      measurementUseCases.save(singletonList(Measurement.builder()
        .created(when)
        .quantity(quantity.name)
        .value(value)
        .unit(quantity.presentationUnit())
        .physicalMeter(physicalMeter)
        .build())
      );
    }
  }

  private LogicalMeter saveLogicalMeter(ZonedDateTime dateTime) {
    return logicalMeters.save(logicalMeterBuilder(MeterDefinition.DISTRICT_HEATING_METER)
      .created(dateTime)
      .build());
  }

  private LogicalMeter saveLogicalMeter() {
    return saveLogicalMeter(MeterDefinition.UNKNOWN_METER);
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
      .language(Language.en)
      .organisation(context().organisation2())
      .asUser();
  }

  private void testSorting(
    String url,
    String errorMessage,
    Function<PagedLogicalMeterDto, String> actual,
    String expected
  ) {
    Page<PagedLogicalMeterDto> response = asTestUser()
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
      .medium("Heat")
      .manufacturer("ELV1");
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

  private static void assertThatStatusIsOk(ResponseEntity<?> response) {
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  private static LocationBuilder kungsbacka() {
    return new LocationBuilder()
      .country("sweden")
      .city("kungsbacka")
      .address("kabelgatan 1")
      .longitude(11.123)
      .latitude(12.345)
      .confidence(0.75);
  }

  private static String metersUrl(ZonedDateTime start, ZonedDateTime before) {
    return String.format("/meters?after=%s&before=%s", start, before);
  }

  private static String meterDetailsUrl(UUID logicalMeterId) {
    return String.format("/meters/details?id=%s", logicalMeterId);
  }
}
