package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter.PhysicalMeterBuilder;
import com.elvaco.mvp.core.domainmodels.Pk;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.spi.repository.MeterAlarmLogs;
import com.elvaco.mvp.core.spi.repository.MeterStatusLogs;
import com.elvaco.mvp.core.util.Dates;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.testdata.UrlTemplate;
import com.elvaco.mvp.web.dto.AlarmDto;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.MeterStatusLogDto;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.domainmodels.StatusType.ERROR;
import static com.elvaco.mvp.core.domainmodels.StatusType.OK;
import static com.elvaco.mvp.core.spi.data.RequestParameter.AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.BEFORE;
import static com.elvaco.mvp.core.spi.data.RequestParameter.LOGICAL_METER_ID;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterDetailsControllerTest extends IntegrationTest {

  private static final ZonedDateTime NOW = ZonedDateTime.now();

  private static final ZonedDateTime YESTERDAY = ZonedDateTime.now()
    .minusDays(1)
    .truncatedTo(ChronoUnit.DAYS);

  @Autowired
  private MeterAlarmLogs meterAlarmLogs;

  @Autowired
  private MeterStatusLogs meterStatusLogs;

  private ZonedDateTime start = ZonedDateTime.parse("2001-01-01T00:00:00.00Z");

  @After
  public void tearDown() {
    measurementJpaRepository.deleteAll();
    meterAlarmLogJpaRepository.deleteAll();
    gatewayJpaRepository.deleteAll();
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
      StatusLogEntry.builder()
        .primaryKey(new Pk(physicalMeter.id, context().organisationId()))
        .status(StatusType.UNKNOWN)
        .start(start)
        .build()
    );

    LogicalMeterDto logicalMeterDto = asUser()
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

    saveStatusLogForMeter(StatusLogEntry.builder()
      .primaryKey(new Pk(physicalMeter.id, context().organisationId()))
      .status(StatusType.UNKNOWN)
      .start(start)
      .build()
    );

    UrlTemplate urlTemplate = Url.builder()
      .path("/meters/details")
      .parameter(LOGICAL_METER_ID, logicalMeter.id)
      .parameter(BEFORE, NOW)
      .parameter(AFTER, YESTERDAY)
      .build();
    LogicalMeterDto logicalMeterDto = asUser()
      .getList(urlTemplate, LogicalMeterDto.class)
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

    saveStatusLogForMeter(StatusLogEntry.builder()
      .primaryKey(new Pk(physicalMeterId, context().organisationId()))
      .status(OK)
      .start(YESTERDAY)
      .build()
    );
    LogicalMeterDto logicalMeterDto = asUser()
      .getList(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody()
      .get(0);

    assertThat(logicalMeterDto.isReported).isFalse();
  }

  @Test
  public void shouldOnlyIncludeUnique_ById_LogicalMeters() {
    LogicalMeter logicalMeter = saveLogicalMeter();

    UUID physicalMeterId1 = randomUUID();
    physicalMeters.save(physicalMeter()
      .id(physicalMeterId1)
      .logicalMeterId(logicalMeter.id)
      .build());

    UUID physicalMeterId2 = randomUUID();
    physicalMeters.save(physicalMeter()
      .id(physicalMeterId2)
      .logicalMeterId(logicalMeter.id)
      .build());

    saveStatusLogForMeter(StatusLogEntry.builder()
      .primaryKey(new Pk(physicalMeterId1, context().organisationId()))
      .status(OK)
      .start(YESTERDAY)
      .build()
    );

    saveStatusLogForMeter(StatusLogEntry.builder()
      .primaryKey(new Pk(physicalMeterId2, context().organisationId()))
      .status(ERROR)
      .start(YESTERDAY)
      .build()
    );

    List<LogicalMeterDto> logicalMeters = asUser()
      .getList(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody();

    assertThat(logicalMeters).hasSize(1);
  }

  @Test
  public void findById_MeterIncludesStatusChangeLog() {
    LogicalMeter logicalMeter = saveLogicalMeter();

    UUID physicalMeterId = randomUUID();

    physicalMeters.save(physicalMeter()
      .id(physicalMeterId)
      .logicalMeterId(logicalMeter.id)
      .build());

    saveStatusLogForMeter(StatusLogEntry.builder()
      .primaryKey(new Pk(physicalMeterId, context().organisationId()))
      .status(OK)
      .start(ZonedDateTime.parse("2001-01-01T10:14:00Z"))
      .stop(ZonedDateTime.parse("2001-01-06T10:14:00Z"))
      .build());

    LogicalMeterDto logicalMeterDto = asUser()
      .getList(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody()
      .get(0);

    MeterStatusLogDto meterStatusLogDto = logicalMeterDto.statusChangelog.get(0);
    assertThat(meterStatusLogDto.id).isNotNull();
    assertThat(meterStatusLogDto.name).isEqualTo("ok");
    assertThat(meterStatusLogDto.start).isEqualTo("2001-01-01T10:14:00Z");
    assertThat(meterStatusLogDto.stop).isEqualTo("2001-01-06T10:14:00Z");
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

    saveStatusLogForMeter(StatusLogEntry.builder()
      .primaryKey(new Pk(physicalMeter.id, context().organisationId()))
      .status(OK)
      .start(start)
      .build()
    );

    List<LogicalMeterDto> response = asUser()
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

    saveStatusLogForMeter(
      StatusLogEntry.builder()
        .primaryKey(new Pk(physicalMeter1.id, context().organisationId()))
        .status(OK)
        .start(start)
        .build(),
      StatusLogEntry.builder()
        .primaryKey(new Pk(physicalMeter2.id, context().organisationId()))
        .status(ERROR)
        .start(start)
        .build()
    );

    String url = meterDetailsUrl(logicalMeter1.id) + "&id=" + logicalMeter2.id;
    List<LogicalMeterDto> logicalMetersResponse = asUser()
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
  public void findOnlyMetersConnectedToGateway() {
    Gateway gateway = gateways.save(Gateway.builder()
      .organisationId(context().organisationId())
      .productModel("ELV")
      .serial("123123")
      .build());

    LogicalMeter logicalMeter1 = logicalMeters.save(logicalMeter()
      .gateway(gateway)
      .build());
    LogicalMeter logicalMeter2 = logicalMeters.save(logicalMeter()
      .gateway(gateway)
      .build());
    LogicalMeter logicalMeter3 = logicalMeters.save(logicalMeter().build());

    PhysicalMeter physicalMeter1 = physicalMeters.save(physicalMeter()
      .logicalMeterId(logicalMeter1.id)
      .externalId("meter-one")
      .build()
    );
    PhysicalMeter physicalMeter2 = physicalMeters.save(physicalMeter()
      .logicalMeterId(logicalMeter2.id)
      .externalId("meter-two")
      .build()
    );
    PhysicalMeter physicalMeter3 = physicalMeters.save(physicalMeter()
      .logicalMeterId(logicalMeter3.id)
      .externalId("meter-three")
      .build()
    );

    saveStatusLogForMeter(
      StatusLogEntry.builder()
        .primaryKey(new Pk(physicalMeter1.id, context().organisationId()))
        .status(OK)
        .start(NOW)
        .build(),
      StatusLogEntry.builder()
        .primaryKey(new Pk(physicalMeter2.id, context().organisationId()))
        .status(OK)
        .start(NOW.plusMinutes(15))
        .build(),
      StatusLogEntry.builder()
        .primaryKey(new Pk(physicalMeter3.id, context().organisationId()))
        .status(ERROR)
        .start(NOW.plusMinutes(60))
        .build()
    );

    List<LogicalMeterDto> logicalMetersResponse = asUser()
      .getList(gatewayMeterDetailsUrl(gateway.id), LogicalMeterDto.class)
      .getBody();

    assertThat(logicalMetersResponse)
      .extracting("id")
      .containsExactlyInAnyOrder(logicalMeter1.id, logicalMeter2.id);
  }

  @Test
  public void meterIsNotReported_WhenNoReportedStatusExists() {
    LogicalMeter logicalMeter = saveLogicalMeter();

    physicalMeters.save(physicalMeter()
      .logicalMeterId(logicalMeter.id)
      .readIntervalMinutes(15)
      .build()
    );

    LogicalMeterDto logicalMeterDto = asUser()
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
    saveStatusLogForMeter(StatusLogEntry.builder()
      .primaryKey(new Pk(physicalMeter.id, context().organisationId()))
      .status(OK)
      .start(start)
      .build()
    );

    LogicalMeterDto logicalMeterDto = asUser()
      .getList(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody()
      .get(0);

    assertThat(logicalMeterDto.isReported).isFalse();
    assertThat(logicalMeterDto.statusChanged).isEqualTo(Dates.formatUtc(start));
  }

  @Test
  public void latestStartedStatusIsSetWhenMeterHasMultipleActiveStatuses() {
    var logicalMeter = saveLogicalMeter();

    var physicalMeter = physicalMeters.save(physicalMeter()
      .logicalMeterId(logicalMeter.id)
      .readIntervalMinutes(15)
      .build()
    );

    var primaryKey = new Pk(physicalMeter.id, context().organisationId());
    saveStatusLogForMeter(
      StatusLogEntry.builder()
        .primaryKey(primaryKey)
        .status(OK)
        .start(start)
        .build(),
      StatusLogEntry.builder()
        .primaryKey(primaryKey)
        .status(ERROR)
        .start(start.plusMinutes(1))
        .build()
    );

    LogicalMeterDto logicalMeterDto = asUser()
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
      StatusLogEntry.builder()
        .primaryKey(new Pk(firstMeter.id, context().organisationId()))
        .status(OK)
        .start(start.plusSeconds(1))
        .build(),
      StatusLogEntry.builder()
        .primaryKey(new Pk(secondMeter.id, context().organisationId()))
        .status(ERROR)
        .start(start)
        .build()
    );
    LogicalMeterDto logicalMeterDto = asUser()
      .getList(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody()
      .get(0);

    assertThat(logicalMeterDto.isReported).isFalse();
  }

  @Test
  public void meterNotFound() {
    ResponseEntity<List<LogicalMeterDto>> response = asUser()
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
      .primaryKey(physicalMeter.primaryKey())
      .mask(12)
      .start(start)
      .description("something is wrong")
      .build());

    LogicalMeterDto logicalMeterDto = asUser()
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
        .build()
    );

    AlarmLogEntry alarm1 = meterAlarmLogs.save(AlarmLogEntry.builder()
      .primaryKey(physicalMeter.primaryKey())
      .mask(12)
      .start(start.plusHours(2))
      .description("something is wrong")
      .build());

    meterAlarmLogs.save(AlarmLogEntry.builder()
      .primaryKey(physicalMeter.primaryKey())
      .mask(33)
      .start(start)
      .description("testing")
      .build());

    LogicalMeterDto logicalMeterDto = asUser()
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

    LogicalMeterDto logicalMeterDto = asUser()
      .getList(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody()
      .get(0);

    assertThat(logicalMeterDto.alarm).isNull();
  }

  private LogicalMeter saveLogicalMeter() {
    return logicalMeters.save(logicalMeter().build());
  }

  private LogicalMeter.LogicalMeterBuilder logicalMeter() {
    UUID meterId = randomUUID();
    return LogicalMeter.builder()
      .id(meterId)
      .externalId(meterId.toString())
      .organisationId(context().organisationId())
      .utcOffset(DEFAULT_UTC_OFFSET);
  }

  @SafeVarargs
  private void saveStatusLogForMeter(StatusLogEntry... statusLogs) {
    asList(statusLogs).forEach(meterStatusLogs::save);
  }

  private PhysicalMeterBuilder physicalMeter() {
    return PhysicalMeter.builder()
      .organisationId(context().organisationId())
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

  private static String gatewayMeterDetailsUrl(UUID gatewayId) {
    return String.format("/meters/details?gatewayId=%s", gatewayId);
  }
}
