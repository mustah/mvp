package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.core.util.Dates;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.testdata.UrlTemplate;
import com.elvaco.mvp.web.dto.AlarmDto;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.MeterStatusLogDto;

import org.junit.After;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.domainmodels.StatusType.ERROR;
import static com.elvaco.mvp.core.domainmodels.StatusType.OK;
import static com.elvaco.mvp.core.domainmodels.StatusType.UNKNOWN;
import static com.elvaco.mvp.core.spi.data.RequestParameter.AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.BEFORE;
import static com.elvaco.mvp.core.spi.data.RequestParameter.LOGICAL_METER_ID;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterDetailsControllerTest extends IntegrationTest {

  @After
  public void tearDown() {
    measurementJpaRepository.deleteAll();
    meterAlarmLogJpaRepository.deleteAll();
    gatewayJpaRepository.deleteAll();
  }

  @Test
  public void findById_WithinDefaultPeriod_WithUnknownStatus() {
    ZonedDateTime start = context().now();
    LogicalMeter logicalMeter = given(logicalMeter());
    given(statusLog(logicalMeter).start(start).status(UNKNOWN));

    LogicalMeterDto logicalMeterDto = asUser()
      .getList(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody()
      .get(0);

    assertThat(logicalMeterDto.isReported).isTrue();
  }

  @Test
  public void findById_WithinExplicitPeriod_WithUnknownStatus() {
    LogicalMeter logicalMeter = given(logicalMeter());

    given(statusLog(logicalMeter).start(context().yesterday()).status(UNKNOWN));

    UrlTemplate urlTemplate = Url.builder()
      .path("/meters/details")
      .parameter(LOGICAL_METER_ID, logicalMeter.id)
      .parameter(BEFORE, context().now())
      .parameter(AFTER, context().yesterday())
      .build();

    var meters = asUser()
      .getList(urlTemplate, LogicalMeterDto.class)
      .getBody();

    assertThat(meters)
      .extracting(m -> m.isReported)
      .containsExactly(true);
  }

  @Test
  public void findById_WithinPeriod_ShouldBeNotReportedWhenOkStatus() {
    LogicalMeter logicalMeter = given(logicalMeter());

    given(statusLog(logicalMeter).status(OK).start(context().yesterday()));

    LogicalMeterDto logicalMeterDto = asUser()
      .getList(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody()
      .get(0);

    assertThat(logicalMeterDto.isReported).isFalse();
  }

  @Test
  public void shouldOnlyIncludeUnique_ById_LogicalMeters() {
    LogicalMeter logicalMeter = given(
      logicalMeter(),
      physicalMeter().activePeriod(PeriodRange.halfOpenFrom(
        context().yesterday(),
        context().now()
      )),
      physicalMeter().activePeriod(PeriodRange.halfOpenFrom(context().now(), null))
    );

    given(
      statusLog(logicalMeter).primaryKey(logicalMeter.physicalMeters.get(0).primaryKey())
        .status(OK)
        .start(context().yesterday()),
      statusLog(logicalMeter).primaryKey(logicalMeter.physicalMeters.get(1).primaryKey())
        .status(ERROR)
        .start(context().yesterday())
    );

    List<LogicalMeterDto> logicalMeters = asUser()
      .getList(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody();

    assertThat(logicalMeters).hasSize(1);
  }

  @Test
  public void findById_MeterIncludesStatusChangeLog() {
    LogicalMeter logicalMeter = given(logicalMeter());

    given(
      statusLog(logicalMeter)
        .status(OK)
        .start(ZonedDateTime.parse("2001-01-01T10:14:00Z"))
        .stop(ZonedDateTime.parse("2001-01-06T10:14:00Z"))
    );

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
    ZonedDateTime start = context().now();
    LogicalMeter logicalMeter = given(logicalMeter());

    given(
      statusLog(logicalMeter).status(OK).start(start)
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
    ZonedDateTime start = context().now();
    LogicalMeter logicalMeter1 = given(logicalMeter());
    LogicalMeter logicalMeter2 = given(logicalMeter());

    given(
      statusLog(logicalMeter1).status(OK).start(start),
      statusLog(logicalMeter2).status(ERROR).start(start)
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
    LogicalMeter logicalMeter1 = given(logicalMeter());
    LogicalMeter logicalMeter2 = given(logicalMeter());
    given(logicalMeter());

    var gateway = given(gateway().meters(List.of(logicalMeter1, logicalMeter2)));

    List<LogicalMeterDto> logicalMetersResponse = asUser()
      .getList(gatewayMeterDetailsUrl(gateway.id), LogicalMeterDto.class)
      .getBody();

    assertThat(logicalMetersResponse)
      .extracting("id")
      .containsExactlyInAnyOrder(logicalMeter1.id, logicalMeter2.id);
  }

  @Test
  public void meterIsNotReported_WhenNoReportedStatusExists() {
    LogicalMeter logicalMeter = given(logicalMeter());

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
    ZonedDateTime start = context().now();

    LogicalMeter logicalMeter = given(logicalMeter());
    given(statusLog(logicalMeter).status(OK).start(start));

    LogicalMeterDto logicalMeterDto = asUser()
      .getList(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody()
      .get(0);

    assertThat(logicalMeterDto.isReported).isFalse();
    assertThat(logicalMeterDto.statusChanged).isEqualTo(Dates.formatUtc(start));
  }

  @Test
  public void latestStartedStatusIsSetWhenMeterHasMultipleActiveStatuses() {
    var logicalMeter = given(logicalMeter());

    given(
      statusLog(logicalMeter).start(context().now()).status(OK),
      statusLog(logicalMeter).start(context().now().plusMinutes(1)).status(ERROR)
    );

    LogicalMeterDto logicalMeterDto = asUser()
      .getList(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody()
      .get(0);

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
    LogicalMeter logicalMeter = given(logicalMeter());

    var alarm = given(alarm(logicalMeter).mask(12)
      .start(context().now())
      .description("something is wrong")).iterator().next();

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
    LogicalMeter logicalMeter = given(logicalMeter());

    AlarmLogEntry alarm1 = given(
      alarm(logicalMeter).mask(12)
        .start(context().now().plusHours(2))
        .description("something is wrong"),
      alarm(logicalMeter).mask(33).start(context().now()).description("testing")
    ).stream().filter(a -> a.mask == 12).findAny().orElseThrow();

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
    LogicalMeter logicalMeter = given(logicalMeter());

    LogicalMeterDto logicalMeterDto = asUser()
      .getList(meterDetailsUrl(logicalMeter.id), LogicalMeterDto.class)
      .getBody()
      .get(0);

    assertThat(logicalMeterDto.alarm).isNull();
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
