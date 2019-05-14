package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.stream.DoubleStream;

import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.PagedLogicalMeterDto;

import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.spi.data.RequestParameter.THRESHOLD;
import static com.elvaco.mvp.core.spi.data.RequestParameter.THRESHOLD_AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.THRESHOLD_BEFORE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class LogicalMeterControllerThresholdSelectionTest extends IntegrationTest {

  @Test
  public void findAllMeters_WithMeasurementThresholdMatching_OnAnotherDay() {
    ZonedDateTime now = context().now();
    var meter = given(physicalMeter());
    given(measurementSeries()
      .forMeter(meter)
      .withQuantity(Quantity.POWER)
      .startingAt(context().now())
      .withValues(-1));
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(now.plusDays(2))
      .withQuantity(Quantity.POWER)
      .withValues(1));

    Url url = Url.builder()
      .path("/meters")
      .parameter(THRESHOLD_AFTER, now)
      .parameter(THRESHOLD_BEFORE, now.plusHours(1))
      .parameter(THRESHOLD, "Power >= 0 W")
      .build();

    waitForMeasurementStat();

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage(url, PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(0);
  }

  @Test
  public void findAllMeters_WithMeasurementThresholdMatchingOneButNotAll() {
    ZonedDateTime now = context().now();
    var meter = given(physicalMeter());
    given(measurementSeries()
      .forMeter(meter)
      .withQuantity(Quantity.POWER)
      .startingAt(context().now())
      .withValues(-1, 1.1));

    Url url = Url.builder()
      .path("/meters")
      .parameter(THRESHOLD_AFTER, now)
      .parameter(THRESHOLD_BEFORE, now.plusHours(1))
      .parameter(THRESHOLD, "Power < 0 W")
      .build();

    waitForMeasurementStat();

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage(url, PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(1);
  }

  @Test
  public void findAllMeters_WithMeasurementThresholdNotMatching() {
    var meter = given(logicalMeter());
    given(measurementSeries()
      .forMeter(meter)
      .withQuantity(Quantity.POWER)
      .startingAt(context().now())
      .withValues(1.1));

    ZonedDateTime now = context().now();
    Url url = Url.builder()
      .path("/meters")
      .parameter(THRESHOLD_AFTER, now)
      .parameter(THRESHOLD_BEFORE, now.plusHours(1))
      .parameter(THRESHOLD, "Power < 0 W")
      .build();

    waitForMeasurementStat();

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage(url, PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(0);
  }

  @Test
  public void findAllMeters_WithMeasurementThresholdMatching_LessThan() {
    var meter = given(logicalMeter());
    given(measurementSeries()
      .forMeter(meter)
      .withQuantity(Quantity.POWER)
      .startingAt(context().now())
      .withValues(-0.1));
    var now = context().now();

    Url url = Url.builder()
      .path("/meters")
      .parameter(THRESHOLD_AFTER, now)
      .parameter(THRESHOLD_BEFORE, now.plusHours(1))
      .parameter(THRESHOLD, "Power < 0 W")
      .build();

    waitForMeasurementStat();

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage(url, PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(1);
  }

  @Test
  public void findAllMeters_WithMeasurementThresholdMatching_LessThanOrEquals() {
    ZonedDateTime now = context().now();
    var meter = given(logicalMeter());
    given(measurementSeries()
      .forMeter(meter)
      .withQuantity(Quantity.POWER)
      .startingAt(context().now())
      .withValues(0));

    Url url = Url.builder()
      .path("/meters")
      .parameter(THRESHOLD_AFTER, now)
      .parameter(THRESHOLD_BEFORE, now.plusHours(1))
      .parameter(THRESHOLD, "Power <= 0 W")
      .build();

    waitForMeasurementStat();

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage(url, PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(1);
  }

  @Test
  public void findAllMeters_WithMeasurementThresholdMatching_GreaterThanOrEquals() {
    ZonedDateTime now = context().now();
    var meter = given(logicalMeter());
    given(measurementSeries()
      .forMeter(meter)
      .withQuantity(Quantity.POWER)
      .startingAt(context().now())
      .withValues(9));

    Url url = Url.builder()
      .path("/meters")
      .parameter(THRESHOLD_AFTER, now)
      .parameter(THRESHOLD_BEFORE, now.plusHours(1))
      .parameter(THRESHOLD, "Power >= 9 W")
      .build();

    waitForMeasurementStat();

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage(url, PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(1);
  }

  @Test
  public void findAllMeters_WithMeasurementThresholdMatching_GreaterThan() {
    ZonedDateTime now = context().now();
    var meter = given(logicalMeter());
    given(measurementSeries()
      .forMeter(meter)
      .withQuantity(Quantity.POWER)
      .startingAt(context().now())
      .withValues(9001));

    Url url = Url.builder()
      .path("/meters")
      .parameter(THRESHOLD_AFTER, now)
      .parameter(THRESHOLD_BEFORE, now.plusHours(1))
      .parameter(THRESHOLD, "Power > 9000 W")
      .build();

    waitForMeasurementStat();

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage(url, PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(1);
  }

  @Test
  public void findAllMeters_WithMeasurementThresholdMatching_DifferentUnitSameDimension() {
    ZonedDateTime now = context().now();
    var meter = given(logicalMeter());
    given(measurementSeries()
      .forMeter(meter)
      .withQuantity(Quantity.POWER)
      .startingAt(context().now())
      .withValues(8999));

    Url url = Url.builder()
      .path("/meters")
      .parameter(THRESHOLD_AFTER, now)
      .parameter(THRESHOLD_BEFORE, now.plusHours(1))
      .parameter(THRESHOLD, "Power < 9 kW")
      .build();

    waitForMeasurementStat();

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage(url, PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(1);
  }

  @Test
  public void findAllMeters_WithMeasurementThresholdWrongDimension() {
    ZonedDateTime now = context().now();
    var meter = given(logicalMeter());
    given(measurementSeries()
      .forMeter(meter)
      .withQuantity(Quantity.POWER)
      .startingAt(context().now())
      .withValues(8999));

    Url url = Url.builder()
      .path("/meters")
      .parameter(THRESHOLD_AFTER, now)
      .parameter(THRESHOLD_BEFORE, now.plusHours(1))
      .parameter(THRESHOLD, "Power < 9 m^3")
      .build();

    waitForMeasurementStat();

    var result = asUser()
      .get(url, ErrorMessageDto.class)
      .getBody();

    assertThat(result.message).isEqualTo(
      "Invalid unit 'm³' for quantity 'Power' in measurement threshold");
  }

  @Test
  public void atAnyTime_24hIntervalAsHourConsumption() {
    ZonedDateTime now = context().now();
    var meter = given(logicalMeter(), physicalMeter().readIntervalMinutes(60 * 24));
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(now)
      .withQuantity(Quantity.VOLUME)
      .withValues(DoubleStream.iterate(0, (m) -> m + 24).limit(4).toArray()));

    waitForMeasurementStat();

    Page<PagedLogicalMeterDto> page = asUser().getPage(Url.builder()
      .path("/meters")
      .thresholdPeriod(now.plusDays(1), now.plusDays(2))
      .parameter(THRESHOLD, "Volume < 1 m^3")
      .build(), PagedLogicalMeterDto.class);

    assertThat(page.getTotalElements()).isEqualTo(0);

    page = asUser().getPage(Url.builder()
      .path("/meters")
      .thresholdPeriod(now.plusDays(1), now.plusDays(2))
      .parameter(THRESHOLD, "Volume <= 1 m^3")
      .build(), PagedLogicalMeterDto.class);

    assertThat(page.getTotalElements()).isEqualTo(1);
  }

  @Test
  public void atAnyTime_15mIntervalAsHourConsumption() {
    ZonedDateTime now = context().now();
    var meter = given(logicalMeter(), physicalMeter().readIntervalMinutes(15));
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(now)
      .withQuantity(Quantity.VOLUME)
      .withValues(DoubleStream.iterate(0, (m) -> m + 1).limit(24 * 8).toArray()));

    waitForMeasurementStat();

    Page<PagedLogicalMeterDto> page = asUser().getPage(Url.builder()
      .path("/meters")
      .thresholdPeriod(now.plusDays(1), now.plusDays(2))
      .parameter(THRESHOLD, "Volume < 4 m^3")
      .build(), PagedLogicalMeterDto.class);

    assertThat(page.getTotalElements()).isEqualTo(0);

    page = asUser().getPage(Url.builder()
      .path("/meters")
      .thresholdPeriod(now.plusDays(1), now.plusDays(2))
      .parameter(THRESHOLD, "Volume <= 4 m^3")
      .build(), PagedLogicalMeterDto.class);

    assertThat(page.getTotalElements()).isEqualTo(1);
  }

  @Test
  public void forDuration_FindLeakingMeters() {
    ZonedDateTime now = context().now();
    var leakingMeter = given(logicalMeter().meterDefinition(MeterDefinition.DEFAULT_HOT_WATER));

    //first, it leaks for a day
    given(measurementSeries()
      .forMeter(leakingMeter)
      .startingAt(now)
      .withQuantity(Quantity.VOLUME)
      .withValues(DoubleStream.iterate(0, (m) -> m + 1).limit(24).toArray()));

    //then, someone fixes it
    given(measurementSeries()
      .forMeter(leakingMeter)
      .startingAt(now.plusDays(1))
      .withQuantity(Quantity.VOLUME)
      .withValues(DoubleStream.generate(() -> 24).limit(24).toArray()));

    waitForMeasurementStat();

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage(Url.builder()
        .path("/meters")
        .thresholdPeriod(now, now.plusDays(2))
        .parameter(THRESHOLD, "Volume > 0 m^3 for 1 days")
        .build(), PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(1);

    result = asUser()
      .getPage(Url.builder()
        .path("/meters")
        .thresholdPeriod(now.plusDays(1), now.plusDays(2))
        .parameter(THRESHOLD, "Volume > 0 m^3 for 1 days")
        .build(), PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(0);
  }

  @Test
  public void forDuration_FindZeroConsumptionMeter() {
    ZonedDateTime now = context().now();
    var brokenMeter =
      given(logicalMeter().meterDefinition(MeterDefinition.DEFAULT_DISTRICT_HEATING));

    // first, it works for a day
    given(measurementSeries()
      .forMeter(brokenMeter)
      .startingAt(now)
      .withQuantity(Quantity.VOLUME)
      .withValues(DoubleStream.iterate(0, (m) -> m + 1).limit(24).toArray()));

    // then, it breaks, and stays broken for a week
    given(measurementSeries()
      .forMeter(brokenMeter)
      .startingAt(now.plusDays(1))
      .withQuantity(Quantity.ENERGY)
      .withValues(DoubleStream.generate(() -> 0.1).limit(24 * 7).toArray()));

    waitForMeasurementStat();

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage(Url.builder()
        .path("/meters")
        .thresholdPeriod(now, now.plusDays(8))
        .parameter(THRESHOLD, "Energy <= 0 kWh for 7 days")
        .build(), PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(1);

    result = asUser()
      .getPage(Url.builder()
        .path("/meters")
        .thresholdPeriod(now, now.plusDays(7))
        .parameter(THRESHOLD, "Volume <= 0 m^3 for 7 days")
        .build(), PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(0);
  }

  @Test
  public void forDuration_FindBelowPromisedTemperatureMeters() {
    ZonedDateTime now = context().now();
    var brokenMeter = given(logicalMeter().meterDefinition(MeterDefinition.DEFAULT_ROOM_SENSOR));

    // first, it's cold
    given(measurementSeries()
      .forMeter(brokenMeter)
      .startingAt(now)
      .withQuantity(Quantity.EXTERNAL_TEMPERATURE)
      .withValues(DoubleStream.generate(() -> 19.5).limit(24 * 3).toArray()));

    // then, someone turns up the heat
    given(measurementSeries()
      .forMeter(brokenMeter)
      .startingAt(now.plusDays(3))
      .withQuantity(Quantity.EXTERNAL_TEMPERATURE)
      .withValues(DoubleStream.generate(() -> 23).limit(24 * 3).toArray()));

    waitForMeasurementStat();

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage(Url.builder()
        .path("/meters")
        .thresholdPeriod(now, now.plusDays(6))
        .parameter(THRESHOLD, "External temperature <= 20°C for 3 days")
        .build(), PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(1);

    result = asUser()
      .getPage(Url.builder()
        .path("/meters")
        .thresholdPeriod(now.plusDays(3), now.plusDays(6))
        .parameter(THRESHOLD, "External temperature <= 20°C for 3 days")
        .build(), PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(0);
  }

  @Test
  public void forDuration_DurationLongerThanSelectionPeriod() {
    ZonedDateTime now = context().now();
    ResponseEntity<ErrorMessageDto> response = asUser()
      .get(Url.builder()
        .path("/meters")
        .thresholdPeriod(now, now.plusDays(1))
        .parameter(THRESHOLD, "External temperature <= 20°C for 3 days")
        .build(), ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).contains(
      "Threshold duration too long to fit in selection period"
    );
  }

  @Test
  public void forDuration_0mIntervalDoesNotCrashEverything() {
    ZonedDateTime now = context().now();
    var meter = given(logicalMeter(), physicalMeter().readIntervalMinutes(0));
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(now)
      .withQuantity(Quantity.VOLUME)
      .withValues(DoubleStream.iterate(0, (m) -> m + 1).limit(24).toArray()));

    waitForMeasurementStat();

    ResponseEntity<PagedLogicalMeterDto> response = asUser()
      .get(Url.builder()
        .path("/meters")
        .thresholdPeriod(now.plusDays(1), now.plusDays(2))
        .parameter(THRESHOLD, "Volume < 0 m^3 for 1 days")
        .build(), PagedLogicalMeterDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void forDuration_15mIntervalAsHourConsumption() {
    ZonedDateTime now = context().now();
    var meter = given(logicalMeter(), physicalMeter().readIntervalMinutes(15));
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(now)
      .withQuantity(Quantity.VOLUME)
      .withValues(DoubleStream.iterate(0, (m) -> m + 1).limit(24 * 8).toArray()));

    waitForMeasurementStat();

    Page<PagedLogicalMeterDto> page = asUser().getPage(Url.builder()
      .path("/meters")
      .thresholdPeriod(now.plusDays(1), now.plusDays(2))
      .parameter(THRESHOLD, "Volume < 4 m^3 for 1 days")
      .build(), PagedLogicalMeterDto.class);

    assertThat(page.getTotalElements()).isEqualTo(0);

    page = asUser().getPage(Url.builder()
      .path("/meters")
      .thresholdPeriod(now.plusDays(1), now.plusDays(2))
      .parameter(THRESHOLD, "Volume <= 4 m^3 for 1 days")
      .build(), PagedLogicalMeterDto.class);

    assertThat(page.getTotalElements()).isEqualTo(1);
  }

  @Test
  public void forDuration_24hIntervalAsHourConsumption() {
    ZonedDateTime now = context().now();
    var meter = given(logicalMeter(), physicalMeter().readIntervalMinutes(60 * 24));
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(now)
      .withQuantity(Quantity.VOLUME)
      .withValues(DoubleStream.iterate(0, (m) -> m + 24).limit(4).toArray()));

    waitForMeasurementStat();

    Page<PagedLogicalMeterDto> page = asUser().getPage(Url.builder()
      .path("/meters")
      .thresholdPeriod(now.plusDays(1), now.plusDays(2))
      .parameter(THRESHOLD, "Volume < 1 m^3 for 1 days")
      .build(), PagedLogicalMeterDto.class);

    assertThat(page.getTotalElements()).isEqualTo(0);

    page = asUser().getPage(Url.builder()
      .path("/meters")
      .thresholdPeriod(now.plusDays(1), now.plusDays(2))
      .parameter(THRESHOLD, "Volume <= 1 m^3 for 1 days")
      .build(), PagedLogicalMeterDto.class);

    assertThat(page.getTotalElements()).isEqualTo(1);
  }

  @Test
  public void forDuration_NonConsumptionQuantityIsNotConvertedToHourly() {
    ZonedDateTime now = context().now();
    var meter = given(logicalMeter(), physicalMeter().readIntervalMinutes(60 * 24));
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(now)
      .withQuantity(Quantity.RETURN_TEMPERATURE)
      .withValues(DoubleStream.iterate(0, (m) -> m + 24).limit(2).toArray()));

    waitForMeasurementStat();

    Page<PagedLogicalMeterDto> page = asUser().getPage(Url.builder()
      .path("/meters")
      .thresholdPeriod(now.plusDays(1), now.plusDays(2))
      .parameter(THRESHOLD, "Return temperature < 24 °C for 1 days")
      .build(), PagedLogicalMeterDto.class);

    assertThat(page.getTotalElements()).isEqualTo(0);

    page = asUser().getPage(Url.builder()
      .path("/meters")
      .thresholdPeriod(now.plusDays(1), now.plusDays(2))
      .parameter(THRESHOLD, "Return temperature <= 24 °C for 1 days")
      .build(), PagedLogicalMeterDto.class);

    assertThat(page.getTotalElements()).isEqualTo(1);
  }
}
