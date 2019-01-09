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

import static com.elvaco.mvp.core.spi.data.RequestParameter.AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.BEFORE;
import static com.elvaco.mvp.core.spi.data.RequestParameter.THRESHOLD;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class LogicalMeterControllerThresholdSelectionTest extends IntegrationTest {

  @Test
  public void findAllMeters_WithMeasurementThresholdMatching_OnAnotherDay() {
    ZonedDateTime now = context().now();
    var meter = given(physicalMeter());
    given(series(meter, Quantity.POWER, -1));
    given(series(meter, Quantity.POWER, now.plusDays(2), 1));

    Url url = Url.builder()
      .path("/meters")
      .parameter(AFTER, now)
      .parameter(BEFORE, now.plusHours(1))
      .parameter(THRESHOLD, "Power >= 0 W")
      .build();

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage(url, PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(0);
  }

  @Test
  public void findAllMeters_WithMeasurementThresholdMatchingOneButNotAll() {
    ZonedDateTime now = context().now();
    var meter = given(physicalMeter());
    given(series(meter, Quantity.POWER, -1));
    given(series(meter, Quantity.POWER, 1.1));

    Url url = Url.builder()
      .path("/meters")
      .parameter(AFTER, now)
      .parameter(BEFORE, now.plusHours(1))
      .parameter(THRESHOLD, "Power < 0 W")
      .build();

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage(url, PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(0);
  }

  @Test
  public void findAllMeters_WithMeasurementThresholdNotMatching() {
    var meter = given(logicalMeter());
    given(series(meter, Quantity.POWER, 1.1));

    ZonedDateTime now = context().now();
    Url url = Url.builder()
      .path("/meters")
      .parameter(AFTER, now)
      .parameter(BEFORE, now.plusHours(1))
      .parameter(THRESHOLD, "Power < 0 W")
      .build();

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage(url, PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(0);
  }

  @Test
  public void findAllMeters_WithMeasurementThresholdMatching_LessThan() {
    var meter = given(logicalMeter());
    given(series(meter, Quantity.POWER, -0.1));
    var now = context().now();

    Url url = Url.builder()
      .path("/meters")
      .parameter(AFTER, now)
      .parameter(BEFORE, now.plusHours(1))
      .parameter(THRESHOLD, "Power < 0 W")
      .build();

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage(url, PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(1);
  }

  @Test
  public void findAllMeters_WithMeasurementThresholdMatching_LessThanOrEquals() {
    ZonedDateTime now = context().now();
    var meter = given(logicalMeter());
    given(series(meter, Quantity.POWER, 0));

    Url url = Url.builder()
      .path("/meters")
      .parameter(AFTER, now)
      .parameter(BEFORE, now.plusHours(1))
      .parameter(THRESHOLD, "Power <= 0 W")
      .build();

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage(url, PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(1);
  }

  @Test
  public void findAllMeters_WithMeasurementThresholdMatching_GreaterThanOrEquals() {
    ZonedDateTime now = context().now();
    var meter = given(logicalMeter());
    given(series(meter, Quantity.POWER, 9));

    Url url = Url.builder()
      .path("/meters")
      .parameter(AFTER, now)
      .parameter(BEFORE, now.plusHours(1))
      .parameter(THRESHOLD, "Power >= 9 W")
      .build();

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage(url, PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(1);
  }

  @Test
  public void findAllMeters_WithMeasurementThresholdMatching_GreaterThan() {
    ZonedDateTime now = context().now();
    var meter = given(logicalMeter());
    given(series(meter, Quantity.POWER, 9001));

    Url url = Url.builder()
      .path("/meters")
      .parameter(AFTER, now)
      .parameter(BEFORE, now.plusHours(1))
      .parameter(THRESHOLD, "Power > 9000 W")
      .build();

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage(url, PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(1);
  }

  @Test
  public void findAllMeters_WithMeasurementThresholdMatching_DifferentUnitSameDimension() {
    ZonedDateTime now = context().now();
    var meter = given(logicalMeter());
    given(series(meter, Quantity.POWER, 8999));

    Url url = Url.builder()
      .path("/meters")
      .parameter(AFTER, now)
      .parameter(BEFORE, now.plusHours(1))
      .parameter(THRESHOLD, "Power < 9 kW")
      .build();

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage(url, PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(1);
  }

  @Test
  public void findAllMeters_WithMeasurementThresholdWrongDimension() {
    ZonedDateTime now = context().now();
    var meter = given(logicalMeter());
    given(series(meter, Quantity.POWER, 8999));

    Url url = Url.builder()
      .path("/meters")
      .parameter(AFTER, now)
      .parameter(BEFORE, now.plusHours(1))
      .parameter(THRESHOLD, "Power < 9 m^3")
      .build();

    var result = asUser()
      .get(url, ErrorMessageDto.class)
      .getBody();

    assertThat(result.message).isEqualTo(
      "Invalid unit 'm³' for quantity 'Power' in measurement threshold");
  }

  @Test
  public void forDuration_FindLeakingMeters() {
    ZonedDateTime now = context().now();
    var leakingMeter = given(logicalMeter().meterDefinition(MeterDefinition.HOT_WATER_METER));

    //first, it leaks for a day
    given(series(
      leakingMeter,
      Quantity.VOLUME,
      now,
      DoubleStream.generate(() -> 0.1).limit(24).toArray()
    ));

    //then, someone fixes it
    given(series(
      leakingMeter,
      Quantity.VOLUME,
      now.plusDays(1),
      DoubleStream.iterate(0, (m) -> m + 1).limit(24).toArray()
    ));

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage(Url.builder()
        .path("/meters")
        .period(now, now.plusDays(2))
        .parameter(THRESHOLD, "Volume > 0 m^3 for 1 days")
        .build(), PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(1);

    result = asUser()
      .getPage(Url.builder()
        .path("/meters")
        .period(now.plusDays(1), now.plusDays(2))
        .parameter(THRESHOLD, "Volume > 0 m^3 for 1 days")
        .build(), PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(0);
  }

  @Test
  public void forDuration_FindZeroConsumptionMeter() {
    ZonedDateTime now = context().now();
    var brokenMeter = given(logicalMeter().meterDefinition(MeterDefinition.DISTRICT_HEATING_METER));

    // first, it works for a day
    given(series(
      brokenMeter,
      Quantity.VOLUME,
      now,
      DoubleStream.iterate(0, (m) -> m + 1).limit(24).toArray()
    ));

    // then, it breaks, and stays broken for a week
    given(series(
      brokenMeter,
      Quantity.ENERGY,
      now.plusDays(1),
      DoubleStream.generate(() -> 0.1).limit(24 * 7).toArray()
    ));

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage(Url.builder()
        .path("/meters")
        .period(now, now.plusDays(8))
        .parameter(THRESHOLD, "Energy <= 0 kWh for 7 days")
        .build(), PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(1);

    result = asUser()
      .getPage(Url.builder()
        .path("/meters")
        .period(now, now.plusDays(7))
        .parameter(THRESHOLD, "Volume <= 0 m^3 for 7 days")
        .build(), PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(0);
  }

  @Test
  public void forDuration_FindBelowPromisedTemperatureMeters() {
    ZonedDateTime now = context().now();
    var brokenMeter = given(logicalMeter().meterDefinition(MeterDefinition.ROOM_SENSOR_METER));

    // first, it's cold
    given(series(
      brokenMeter,
      Quantity.EXTERNAL_TEMPERATURE,
      now,
      DoubleStream.generate(() -> 19.5).limit(24 * 3).toArray()
    ));

    // then, someone turns up the heat
    given(series(
      brokenMeter,
      Quantity.EXTERNAL_TEMPERATURE,
      now.plusDays(3),
      DoubleStream.generate(() -> 23).limit(24 * 3).toArray()
    ));

    Page<PagedLogicalMeterDto> result = asUser()
      .getPage(Url.builder()
        .path("/meters")
        .period(now, now.plusDays(6))
        .parameter(THRESHOLD, "External temperature <= 20°C for 3 days")
        .build(), PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(1);

    result = asUser()
      .getPage(Url.builder()
        .path("/meters")
        .period(now.plusDays(3), now.plusDays(6))
        .parameter(THRESHOLD, "External temperature <= 20°C for 3 days")
        .build(), PagedLogicalMeterDto.class);

    assertThat(result.getTotalElements()).isEqualTo(0);
  }

  @Test
  public void forDuration_DurationLongerThanSelectionPeriod() {
    assertThat(true).isFalse();
  }
}
