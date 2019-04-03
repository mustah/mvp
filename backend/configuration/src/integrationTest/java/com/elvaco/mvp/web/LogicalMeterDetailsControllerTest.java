package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.core.util.Dates;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.AlarmDto;
import com.elvaco.mvp.web.dto.EventLogDto;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.LogicalMeterDto;

import org.junit.After;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.domainmodels.StatusType.ERROR;
import static com.elvaco.mvp.core.domainmodels.StatusType.OK;
import static com.elvaco.mvp.core.domainmodels.StatusType.UNKNOWN;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class LogicalMeterDetailsControllerTest extends IntegrationTest {

  @After
  public void tearDown() {
    measurementJpaRepository.deleteAll();
    meterAlarmLogJpaRepository.deleteAll();
    gatewayStatusLogJpaRepository.deleteAll();
    gatewayJpaRepository.deleteAll();
  }

  @Test
  public void findById_WithinDefaultPeriod_WithUnknownStatus() {
    var start = context().now();
    var logicalMeter = given(
      logicalMeter(),
      physicalMeter().activePeriod(PeriodRange.halfOpenFrom(start, start.plusHours(5)))
    );
    given(statusLog(logicalMeter).start(start).status(UNKNOWN));

    LogicalMeterDto logicalMeterDto = asUser()
      .get(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody();

    assertThat(logicalMeterDto.isReported).isTrue();
  }

  @Test
  public void findById_WithinExplicitPeriod_WithUnknownStatus() {
    LogicalMeter logicalMeter = given(
      logicalMeter(),
      physicalMeter().activePeriod(PeriodRange.halfOpenFrom(
        context().now(),
        context().now().plusHours(5)
      ))
    );

    given(statusLog(logicalMeter).start(context().yesterday()).status(UNKNOWN));

    var url = meterDetailsUrl(logicalMeter.id);

    var meters = asUser()
      .get(url, LogicalMeterDto.class)
      .getBody();

    assertThat(meters)
      .extracting(m -> m.isReported)
      .isEqualTo(true);
  }

  @Test
  public void findById_WithinPeriod_ShouldBeNotReportedWhenOkStatus() {
    LogicalMeter logicalMeter = given(
      logicalMeter(),
      physicalMeter().activePeriod(PeriodRange.halfOpenFrom(
        context().now(),
        context().now().plusHours(5)
      ))
    );

    given(statusLog(logicalMeter).status(OK).start(context().yesterday()));

    LogicalMeterDto logicalMeterDto = asUser()
      .get(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody();

    assertThat(logicalMeterDto.isReported).isFalse();
  }

  @Test
  public void findById_Meter_IncludesStatusChanges() {
    ZonedDateTime start = ZonedDateTime.parse("2001-01-01T10:14:00Z");
    ZonedDateTime stop = ZonedDateTime.parse("2001-01-06T10:14:00Z");

    LogicalMeter logicalMeter = given(
      logicalMeter(),
      physicalMeter().activePeriod(PeriodRange.halfOpenFrom(
        context().now(),
        context().now().plusHours(10)
      ))
    );

    given(
      statusLog(logicalMeter)
        .status(OK)
        .start(start)
        .stop(stop)
    );

    LogicalMeterDto logicalMeterDto = asUser()
      .get(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody();

    assertThat(logicalMeterDto.eventLog).hasSize(2);
    EventLogDto eventLogDto = logicalMeterDto.eventLog.get(1);
    assertThat(eventLogDto.name).isEqualTo("ok");
    assertThat(eventLogDto.start).isEqualTo("2001-01-01T10:14:00Z");
  }

  @Test
  public void findById_Meter_Includes_AllStatusChanges() {
    var start = context().now();
    var stop = start.plusDays(6);

    var logicalMeter = given(
      logicalMeter(),
      physicalMeter().activePeriod(PeriodRange.from(start))
    );

    given(
      statusLog(logicalMeter)
        .status(OK)
        .start(start)
        .stop(stop),
      statusLog(logicalMeter)
        .status(ERROR)
        .start(stop)
    );

    var url = meterDetailsUrl(logicalMeter.id);

    LogicalMeterDto logicalMeterDto = asUser()
      .get(url, LogicalMeterDto.class)
      .getBody();

    assertThat(logicalMeterDto.eventLog)
      .extracting(m -> m.name)
      .containsExactlyInAnyOrder(
        logicalMeter.activePhysicalMeter().get().address,
        OK.name,
        ERROR.name
      );
  }

  @Test
  public void findById_Meter_IncludesStatusChangeLogs_OutsideOfTimePeriod() {
    var start = ZonedDateTime.parse("2001-01-01T10:14:00Z");
    var logicalMeter = given(
      logicalMeter(),
      physicalMeter().activePeriod(PeriodRange.from(start))
    );

    given(statusLog(logicalMeter).status(OK).start(start));

    var url = meterDetailsUrl(logicalMeter.id);

    LogicalMeterDto logicalMeterDto = asUser()
      .get(url, LogicalMeterDto.class)
      .getBody();

    assertThat(logicalMeterDto.eventLog)
      .extracting(m -> m.name)
      .containsExactlyInAnyOrder(logicalMeter.activePhysicalMeter().get().address, OK.name);
  }

  @Test
  public void findById_Meter_IncludesEventLog_MeterReplacements_WithoutPeriod_Desc() {
    ZonedDateTime start = ZonedDateTime.parse("2001-01-01T10:14:00Z");
    ZonedDateTime stop = ZonedDateTime.parse("2001-01-06T10:14:00Z");

    var logicalMeter = given(
      logicalMeter(),
      physicalMeter().activePeriod(PeriodRange.halfOpenFrom(start, stop)),
      physicalMeter().activePeriod(PeriodRange.from(stop))
    );

    LogicalMeterDto logicalMeterDto = asUser()
      .get(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody();

    assertThat(logicalMeterDto.eventLog)
      .hasSize(2)
      .extracting(e -> e.start)
      .containsExactly("2001-01-06T10:14:00Z", "2001-01-01T10:14:00Z");
  }

  @Test
  public void pagedMeterDetailsIsNotReported() {
    ZonedDateTime start = context().now();
    LogicalMeter logicalMeter = given(
      logicalMeter(),
      physicalMeter().activePeriod(PeriodRange.halfOpenFrom(start, start.plusHours(5)))
    );

    given(statusLog(logicalMeter).status(OK).start(start));

    LogicalMeterDto logicalMeterDto = asUser()
      .get(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody();

    assertThat(logicalMeterDto.isReported).isFalse();
    assertThat(logicalMeterDto.statusChanged).isEqualTo(Dates.formatUtc(start));
  }

  @Test
  public void meterIsNotReported_WhenNoReportedStatusExists() {
    LogicalMeter logicalMeter = given(
      logicalMeter(),
      physicalMeter().activePeriod(PeriodRange.halfOpenFrom(
        context().now(),
        context().now().plusHours(5)
      ))
    );

    physicalMeters.save(physicalMeter()
      .logicalMeterId(logicalMeter.id)
      .readIntervalMinutes(15)
      .build()
    );

    LogicalMeterDto logicalMeterDto = asUser()
      .get(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody();

    assertThat(logicalMeterDto.isReported).isFalse();
  }

  @Test
  public void metersIsNotReportedWhenStatusOk() {
    ZonedDateTime start = context().now();

    LogicalMeter logicalMeter = given(
      logicalMeter(),
      physicalMeter().activePeriod(PeriodRange.halfOpenFrom(
        start,
        start.plusHours(5)
      ))
    );
    given(statusLog(logicalMeter).status(OK).start(start));

    LogicalMeterDto logicalMeterDto = asUser()
      .get(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody();

    assertThat(logicalMeterDto.isReported).isFalse();
    assertThat(logicalMeterDto.statusChanged).isEqualTo(Dates.formatUtc(start));
  }

  @Test
  public void latestStartedStatusIsSetWhenMeterHasMultipleActiveStatuses() {
    var logicalMeter = given(
      logicalMeter(),
      physicalMeter().activePeriod(PeriodRange.halfOpenFrom(
        context().now(),
        context().now().plusHours(5)
      ))
    );

    given(
      statusLog(logicalMeter).start(context().now()).status(OK),
      statusLog(logicalMeter).start(context().now().plusMinutes(1)).status(ERROR)
    );

    LogicalMeterDto logicalMeterDto = asUser()
      .get(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody();

    assertThat(logicalMeterDto.isReported).isTrue();
  }

  @Test
  public void latestStartedStatusIsSetWhenMeterHasMultipleActiveStatusesOnMultiplePhysicalMeters() {
    LogicalMeter logicalMeter = given(
      logicalMeter(),
      physicalMeter().activePeriod(PeriodRange.halfOpenFrom(
        context().yesterday(),
        context().now()
      )),
      physicalMeter().activePeriod(PeriodRange.halfOpenFrom(context().now(), null))
    );

    given(
      statusLog(logicalMeter)
        .primaryKey(logicalMeter.physicalMeters.get(0).primaryKey())
        .status(OK)
        .start(context().now().plusSeconds(1)),
      statusLog(logicalMeter)
        .primaryKey(logicalMeter.physicalMeters.get(1).primaryKey())
        .status(ERROR)
        .start(context().now())
    );

    LogicalMeterDto logicalMeterDto = asUser()
      .get(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody();

    assertThat(logicalMeterDto.isReported).isFalse();
  }

  @Test
  public void meterNotFound() {
    ResponseEntity<LogicalMeterDto> response = asUser()
      .get(meterDetailsUrl(randomUUID()), LogicalMeterDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void findById_ShouldHaveMeterWithAlarm() {
    LogicalMeter logicalMeter = given(
      logicalMeter(),
      physicalMeter().activePeriod(PeriodRange.halfOpenFrom(
        context().now(),
        context().now().plusHours(1)
      ))
    );

    var alarm = given(
      alarm(logicalMeter).mask(16).start(context().now())
    ).iterator().next();

    LogicalMeterDto logicalMeterDto = asUser()
      .get(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody();

    assertThat(logicalMeterDto.alarms).isEqualTo(List.of(new AlarmDto(
      alarm.id,
      alarm.mask
    )));
  }

  @Test
  public void findById_ShouldHaveMeterWithAllActiveAlarms() {
    LogicalMeter logicalMeter = given(
      logicalMeter(), physicalMeter().activePeriod(PeriodRange.halfOpenFrom(
        context().now(),
        context().now().plusHours(5)
      )));

    List<AlarmLogEntry> alarms = new ArrayList<>(given(
      alarm(logicalMeter).mask(4).start(context().now().plusHours(2)),
      alarm(logicalMeter).mask(2).start(context().now()),
      alarm(logicalMeter).mask(1)
        .start(context().now().minusHours(10))
        .stop(context().now().minusHours(5))
    ));

    LogicalMeterDto logicalMeterDto = asUser()
      .get(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody();

    assertThat(logicalMeterDto.alarms).containsExactlyInAnyOrder(
      new AlarmDto(
        alarms.get(0).id,
        alarms.get(0).mask
      ), new AlarmDto(
        alarms.get(1).id,
        alarms.get(1).mask
      )
    );
  }

  @Test
  public void findById_ShouldNotHaveMeterWithAlarm() {
    LogicalMeter logicalMeter = given(
      logicalMeter(),
      physicalMeter().activePeriod(PeriodRange.halfOpenFrom(
        context().now(),
        context().now().plusHours(5)
      ))
    );

    LogicalMeterDto logicalMeterDto = asUser()
      .get(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody();

    assertThat(logicalMeterDto.alarms).isEmpty();
  }

  @Test
  public void shouldContainInfoFromActiveMeter() {
    var meter = given(
      logicalMeter(),
      physicalMeter().manufacturer("ELV")
        .address("1234")
        .activePeriod(PeriodRange.halfOpenFrom(context().yesterday(), context().now())),
      physicalMeter().manufacturer("KAM")
        .address("5678")
        .activePeriod(PeriodRange.halfOpenFrom(context().now(), null))
    );
    given(gateway().meter(meter));

    var url = meterDetailsUrl(meter.id);

    LogicalMeterDto logicalMeterDto = asUser()
      .get(url, LogicalMeterDto.class)
      .getBody();

    assertThat(logicalMeterDto.manufacturer).isEqualTo("KAM");
    assertThat(logicalMeterDto.address).isEqualTo("5678");
  }

  @Test
  public void gateway_StatusIsIncluded() {
    var meter = given(
      logicalMeter(),
      physicalMeter().activePeriod(PeriodRange.from(context().yesterday()))
    );
    var gateway = given(gateway().meter(meter));

    given(statusLog(gateway).status(OK).start(context().yesterday()));

    var url = meterDetailsUrl(meter.id);

    var content = asUser()
      .get(url, LogicalMeterDto.class)
      .getBody();

    assertThat(content)
      .extracting(m -> m.gateway.status)
      .isEqualTo(new IdNamedDto("ok"));
  }

  @Test
  public void alarm_hasDescription() {
    var meter = given(
      logicalMeter(),
      physicalMeter().manufacturer("ELV").mbusDeviceType(4).revision(2)
    );
    given(alarm(meter).mask(2).start(context().now().minusDays(1)));

    var url = meterDetailsUrl(meter.id);

    var meterDto = asUser()
      .get(url, LogicalMeterDto.class)
      .getBody();

    assertThat(meterDto.alarms)
      .extracting((alarm) -> tuple(alarm.mask, alarm.description))
      .containsExactly(
        tuple(2, "Interruption of flow temperature sensor - Error F1")
      );
  }

  @Test
  public void multipleAlarms_haveDescriptions() {
    var meter = given(
      logicalMeter(),
      physicalMeter().manufacturer("ELV").mbusDeviceType(4).revision(2)
    );
    given(
      alarm(meter).mask(2).start(context().now().minusDays(1)),
      alarm(meter).mask(4).start(context().now().minusDays(1))
    );

    var url = meterDetailsUrl(meter.id);

    var meterDto = asUser()
      .get(url, LogicalMeterDto.class)
      .getBody();

    assertThat(meterDto.alarms)
      .extracting((alarm) -> tuple(alarm.mask, alarm.description))
      .containsExactly(
        tuple(2, "Interruption of flow temperature sensor - Error F1"),
        tuple(4, "Interruption of return temperature sensor - Error F2")
      );
  }

  private static String meterDetailsUrl(UUID logicalMeterId) {
    return String.format("/meters/%s", logicalMeterId);
  }
}
