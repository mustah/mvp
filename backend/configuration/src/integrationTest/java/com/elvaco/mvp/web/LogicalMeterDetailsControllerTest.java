package com.elvaco.mvp.web;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter.PhysicalMeterBuilder;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Measurements;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.util.Dates;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.AlarmDto;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.MeasurementDto;
import com.elvaco.mvp.web.dto.MeterStatusLogDto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DISTRICT_HEATING_METER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.UNKNOWN_METER;
import static com.elvaco.mvp.core.domainmodels.StatusType.ERROR;
import static com.elvaco.mvp.core.domainmodels.StatusType.OK;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterDetailsControllerTest extends IntegrationTest {

  private static final ZonedDateTime NOW = ZonedDateTime.now();
  private static final ZonedDateTime YESTERDAY = ZonedDateTime.now()
    .minusDays(1)
    .truncatedTo(ChronoUnit.DAYS);

  @Autowired
  private LogicalMeters logicalMeters;

  @Autowired
  private MeterAlarmLogs meterAlarmLogs;

  @Autowired
  private Measurements measurements;

  @Autowired
  private PhysicalMeters physicalMeters;

  @Autowired
  private MeterStatusLogs meterStatusLogs;

  private ZonedDateTime start;

  @Before
  public void setUp() {
    start = ZonedDateTime.parse("2001-01-01T00:00:00.00Z");
  }

  @After
  public void tearDown() {
    measurementJpaRepository.deleteAll();
    physicalMeterStatusLogJpaRepository.deleteAll();
    physicalMeterJpaRepository.deleteAll();
    meterAlarmLogJpaRepository.deleteAll();
    logicalMeterJpaRepository.deleteAll();
  }

  @Test
  public void findById_WithinDefaultPeriod_WithUnknownStatus() {
    LogicalMeter logicalMeter = saveLogicalMeter();

    ZonedDateTime start = ZonedDateTime.parse("2001-01-01T01:00:00.00Z");

    PhysicalMeter physicalMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(logicalMeter.id)
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
      .readIntervalMinutes(60)
      .build()
    );

    saveStatusLogForMeter(StatusLogEntry.<UUID>builder()
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

    physicalMeters.save(physicalMeter()
      .id(physicalMeterId)
      .logicalMeterId(logicalMeter.id)
      .build());

    saveStatusLogForMeter(StatusLogEntry.<UUID>builder()
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

    physicalMeters.save(physicalMeter()
      .id(physicalMeterId)
      .logicalMeterId(logicalMeter.id)
      .build());

    StatusLogEntry<UUID> logEntry = saveStatusLogForMeter(StatusLogEntry.<UUID>builder()
      .entityId(physicalMeterId)
      .status(OK)
      .start(ZonedDateTime.parse("2001-01-01T10:14:00Z"))
      .stop(ZonedDateTime.parse("2001-01-06T10:14:00Z"))
      .build()
    );

    LogicalMeterDto logicalMeterDto = asTestUser()
      .getList(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
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
  public void pagedMeterDetailsIsNotReported() {
    LogicalMeter logicalMeter = saveLogicalMeter();

    PhysicalMeter physicalMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(logicalMeter.id)
      .readIntervalMinutes(15)
      .build()
    );

    ZonedDateTime start = ZonedDateTime.parse("2018-04-03T08:00:00Z");

    saveStatusLogForMeter(StatusLogEntry.<UUID>builder()
      .entityId(physicalMeter.id)
      .status(OK)
      .start(start)
      .build()
    );

    List<LogicalMeterDto> response = asTestUser()
      .getList(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
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

    saveStatusLogForMeter(StatusLogEntry.<UUID>builder()
      .entityId(physicalMeter1.id)
      .status(OK)
      .start(start)
      .build()
    );
    saveStatusLogForMeter(StatusLogEntry.<UUID>builder()
      .entityId(physicalMeter2.id)
      .status(ERROR)
      .start(start)
      .build()
    );

    String url = meterDetailsUrl(logicalMeter1.id) + "&id=" + logicalMeter2.id;
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
      .readIntervalMinutes(15)
      .build()
    );

    ZonedDateTime start = ZonedDateTime.parse("2018-04-03T08:00:00Z");
    saveStatusLogForMeter(StatusLogEntry.<UUID>builder()
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
      .readIntervalMinutes(15)
      .build()
    );

    saveStatusLogForMeter(StatusLogEntry.<UUID>builder()
      .entityId(firstMeter.id)
      .status(OK)
      .start(start)
      .build()
    );

    saveStatusLogForMeter(StatusLogEntry.<UUID>builder()
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
    saveStatusLogForMeter(StatusLogEntry.<UUID>builder()
      .entityId(firstMeter.id)
      .status(OK)
      .start(start.plusSeconds(1))
      .build()
    );

    saveStatusLogForMeter(StatusLogEntry.<UUID>builder()
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
  public void meterNotFound() {
    ResponseEntity<List<LogicalMeterDto>> response = asTestUser()
      .getList(meterDetailsUrl(randomUUID()), LogicalMeterDto.class);

    assertThatStatusIsOk(response);
    assertThat(response.getBody()).isEmpty();
  }

  @Test
  public void findById_ShouldHaveMeterWithAlarm() {
    LogicalMeter logicalMeter = saveLogicalMeter();

    PhysicalMeter physicalMeter = physicalMeters.save(
      physicalMeter()
        .logicalMeterId(logicalMeter.id)
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
  public void findById_MeterContainsLatestMeasurements() {
    LogicalMeter logicalMeter = saveLogicalMeter(DISTRICT_HEATING_METER);

    PhysicalMeter physicalMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(logicalMeter.id)
      .readIntervalMinutes(15)
      .build());

    saveStatusLogForMeter(StatusLogEntry.<UUID>builder()
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
      1.0
    );

    addMeasurementsForMeter(
      physicalMeter,
      Collections.singleton(Quantity.POWER),
      ZonedDateTime.now(),
      Duration.ofDays(1),
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
  public void findById_ShouldHaveMeterWithLatestActiveAlarm() {
    LogicalMeter logicalMeter = saveLogicalMeter();

    PhysicalMeter physicalMeter = physicalMeters.save(
      physicalMeter()
        .logicalMeterId(logicalMeter.id)
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
        .build()
    );

    LogicalMeterDto logicalMeterDto = asTestUser()
      .getList(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody()
      .get(0);

    assertThat(logicalMeterDto.alarm).isNull();
  }

  private void addMeasurementsForMeter(
    PhysicalMeter physicalMeter,
    Set<Quantity> quantities,
    ZonedDateTime start,
    Duration periodDuration,
    double value
  ) {
    ZonedDateTime now = start;
    double incrementedValue = value;
    while (now.isBefore(start.plus(periodDuration))) {
      addMeasurementsForMeterQuantities(physicalMeter, quantities, now, incrementedValue);
      now = now.plusMinutes(60L);
      incrementedValue += (double) 0;
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

  private PhysicalMeterBuilder physicalMeter() {
    return PhysicalMeter.builder()
      .organisation(context().organisation())
      .address("address-123")
      .externalId(randomUUID().toString())
      .medium(Medium.HOT_WATER.medium)
      .manufacturer("ELV1");
  }

  private static void assertThatStatusIsOk(ResponseEntity<?> response) {
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  private static String meterDetailsUrl(UUID logicalMeterId) {
    return String.format("/meters/details?id=%s", logicalMeterId);
  }
}
