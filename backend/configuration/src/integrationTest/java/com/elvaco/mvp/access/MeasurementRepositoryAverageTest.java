package com.elvaco.mvp.access;

import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

import com.elvaco.mvp.core.domainmodels.MeasurementParameter;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.Test;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class MeasurementRepositoryAverageTest extends IntegrationTest {

  @Test
  public void correctNumberOfValuesAreReturnedRelativeToStart() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());

    given(series(meter, Quantity.VOLUME, start, Duration.ofHours(1),
      1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0
    ));

    List<MeasurementValue> results = measurements.findAverageForPeriod(
      new MeasurementParameter(
        List.of(meter.id),
        List.of(Quantity.VOLUME),
        start.plusHours(5),
        start.plusHours(9),
        TemporalResolution.hour
      ));
    assertThat(results).hasSize(5);
  }

  @Test
  public void resultsCanBeFetchedWithDayResolution() {
    ZonedDateTime start = context().now();
    var meter = given(physicalMeter());

    given(series(meter, Quantity.POWER, start, Duration.ofDays(1), 2.0, 4.0));

    List<MeasurementValue> results = measurements.findAverageForPeriod(
      new MeasurementParameter(
        meter.physicalMeters.stream().map(p -> p.id).collect(toList()),
        List.of(Quantity.POWER),
        start,
        start.plusDays(1),
        TemporalResolution.day
      ));

    assertThat(results).extracting(v -> v.value).containsExactly(2.0, 4.0);
  }

  @Test
  public void resultsCanBeFetchedWithMonthResolution() {
    ZonedDateTime start = context().now();
    var meter = given(physicalMeter());

    given(series(meter, Quantity.POWER, start, Period.ofMonths(1), 2.0, 4.0));

    List<MeasurementValue> results = measurements.findAverageForPeriod(
      new MeasurementParameter(
        meter.physicalMeters.stream().map(p -> p.id).collect(toList()),
        List.of(Quantity.POWER),
        start,
        start.plusMonths(1),
        TemporalResolution.month
      ));

    assertThat(results).extracting(v -> v.value).containsExactly(2.0, 4.0);
  }

  @Test
  public void missingIntervalValuesAreRepresentedAsNull() {
    ZonedDateTime start = context().now();
    var meter = given(physicalMeter());

    given(series(meter, Quantity.POWER, start, 2.0));
    given(series(meter, Quantity.POWER, start.plusHours(2), 3.0));

    List<MeasurementValue> results = measurements.findAverageForPeriod(
      new MeasurementParameter(
        meter.physicalMeters.stream().map(p -> p.id).collect(toList()),
        List.of(Quantity.POWER),
        start,
        start.plusHours(2),
        TemporalResolution.hour
      ));

    assertThat(results).extracting(v -> v.value).containsExactly(2.0, null, 3.0);
  }

  @Test
  public void averageShouldOnlyIncludeValuesAtResolutionPoints() {
    ZonedDateTime start = context().now();
    var meter = given(physicalMeter());
    var interval = Duration.ofMinutes(1);

    given(series(meter, Quantity.POWER, start, interval, 2.0, 100.0));
    given(series(meter, Quantity.POWER, start.plusHours(1), interval, 1.0, 9.0));

    List<MeasurementValue> results = measurements.findAverageForPeriod(
      new MeasurementParameter(
        meter.physicalMeters.stream().map(p -> p.id).collect(toList()),
        List.of(Quantity.POWER),
        start,
        start.plusHours(1).plusMinutes(1),
        TemporalResolution.hour
      ));

    assertThat(results).extracting(v -> v.value).containsExactly(2.0, 1.0);
  }

  @Test
  public void unspecifiedMetersAreNotIncluded() {
    ZonedDateTime start = context().now();
    var meterOne = given(logicalMeter());
    var meterTwo = given(logicalMeter());

    given(series(meterOne, Quantity.POWER, start, 12));
    given(series(meterTwo, Quantity.POWER, start, 99.8));

    List<MeasurementValue> results = measurements.findAverageForPeriod(
      new MeasurementParameter(
        meterOne.physicalMeters.stream().map(p -> p.id).collect(toList()),
        List.of(Quantity.POWER),
        start,
        start.plusSeconds(1),
        TemporalResolution.hour
      ));

    assertThat(results).extracting(v -> v.value).containsExactly(12.0);
  }

  @Test
  public void valuesAreFilteredByQuantity() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());

    given(series(meter, Quantity.POWER, start, 2.0));
    given(series(meter, Quantity.RETURN_TEMPERATURE, start, 6.0));

    List<MeasurementValue> results = measurements.findAverageForPeriod(
      new MeasurementParameter(
        meter.physicalMeters.stream().map(p -> p.id).collect(toList()),
        List.of(Quantity.POWER),
        start,
        start.plusSeconds(1),
        TemporalResolution.hour
      ));

    assertThat(results).extracting(v -> v.value).containsExactly(2.0);
  }

  @Test
  public void timesAreCorrect() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());

    given(series(meter, Quantity.ENERGY, start, 2.0));

    List<MeasurementValue> resultsWithDayResolution = measurements.findAverageForPeriod(
      new MeasurementParameter(
        meter.physicalMeters.stream().map(p -> p.id).collect(toList()),
        List.of(Quantity.ENERGY),
        start,
        start.plusSeconds(1),
        TemporalResolution.day
      ));

    List<MeasurementValue> resultsWithMonthResolution = measurements.findAverageForPeriod(
      new MeasurementParameter(
        meter.physicalMeters.stream().map(p -> p.id).collect(toList()),
        List.of(Quantity.ENERGY),
        start,
        start.plusSeconds(1),
        TemporalResolution.month
      ));

    assertThat(resultsWithDayResolution).extracting(v -> v.when).containsExactly(start.toInstant());
    assertThat(resultsWithDayResolution).extracting(v -> v.when).containsExactly(start.toInstant());
  }

  @Test
  public void averageForConsumptionSeries() {
    ZonedDateTime start = context().now();
    var meterOne = given(logicalMeter());
    var meterTwo = given(logicalMeter());
    var interval = Duration.ofHours(1);

    given(series(meterOne, Quantity.ENERGY, start.plusHours(1), interval, 0.0, 1.0, 5.0));
    given(series(meterTwo, Quantity.ENERGY, start.plusHours(1), interval, 1.0, 2.0, 3.0));

    var meterIds = Stream.concat(
      meterOne.physicalMeters.stream(),
      meterTwo.physicalMeters.stream()
    ).map(p -> p.id).collect(toList());

    List<MeasurementValue> results = measurements.findAverageForPeriod(
      new MeasurementParameter(
        meterIds,
        List.of(Quantity.ENERGY),
        start.plusHours(1),
        start.plusHours(3),
        TemporalResolution.hour
      ));

    assertThat(results).extracting(v -> v.value).containsExactly(
      1.0, // ((1.0 - 0.0) + (2.0 - 1.0)) / 2
      2.5, // ((5.0 - 1.0) + (3.0 - 2.0)) / 2
      null
    );
  }

  @Test
  public void averageForConsumptionSeries_lastIsNotNullWhenValueExistAfterPeriod() {
    ZonedDateTime start = context().now();
    var meterOne = given(logicalMeter());
    var meterTwo = given(logicalMeter());
    var interval = Duration.ofHours(1);

    given(series(meterOne, Quantity.ENERGY, start.plusHours(1), interval, 0.0, 1.0, 5.0));
    given(series(meterTwo, Quantity.ENERGY, start.plusHours(1), interval, 1.0, 2.0, 3.0));

    var meterIds = Stream.concat(
      meterOne.physicalMeters.stream(),
      meterTwo.physicalMeters.stream()
    ).map(p -> p.id).collect(toList());

    List<MeasurementValue> results = measurements.findAverageForPeriod(
      new MeasurementParameter(
        meterIds,
        List.of(Quantity.ENERGY),
        start.plusHours(1),
        start.plusHours(2),
        TemporalResolution.hour
      ));

    assertThat(results).extracting(v -> v.value).containsExactly(
      1.0, // ((1.0 - 0.0) + (2.0 - 1.0)) / 2
      2.5  // ((5.0 - 1.0) + (3.0 - 2.0)) / 2
    );
  }

  @Test
  public void averageForMissingMeasurements() {
    ZonedDateTime start = context().now();
    var meterOne = given(logicalMeter());
    var meterTwo = given(logicalMeter());
    var interval = Duration.ofHours(1);

    given(series(meterOne, Quantity.POWER, start.plusHours(2), interval, 1.0, 5.0));
    given(series(meterTwo, Quantity.POWER, start.plusHours(1), interval, 7.0, 2.0, 3.0));

    var meterIds = Stream.concat(
      meterOne.physicalMeters.stream(),
      meterTwo.physicalMeters.stream()
    ).map(p -> p.id).collect(toList());

    List<MeasurementValue> results = measurements.findAverageForPeriod(
      new MeasurementParameter(
        meterIds,
        List.of(Quantity.POWER),
        start.plusHours(1),
        start.plusHours(4),
        TemporalResolution.hour
      ));

    assertThat(results).extracting(v -> v.value).containsExactly(7.0, 1.5, 4.0, null);
  }

  @Test
  public void averageForConsumptionSeries_missingMeasurementsForOneMeter() {
    ZonedDateTime start = context().now();
    var meterOne = given(logicalMeter());
    var meterTwo = given(logicalMeter());
    var interval = Duration.ofHours(1);

    given(series(meterOne, Quantity.ENERGY, start.plusHours(2), interval, 1.0, 5.0));
    given(series(meterTwo, Quantity.ENERGY, start.plusHours(1), interval, 0.0, 2.0, 3.0));

    var meterIds = Stream.concat(
      meterOne.physicalMeters.stream(),
      meterTwo.physicalMeters.stream()
    ).map(p -> p.id).collect(toList());

    List<MeasurementValue> results = measurements.findAverageForPeriod(
      new MeasurementParameter(
        meterIds,
        List.of(Quantity.ENERGY),
        start.plusHours(1),
        start.plusHours(2),
        TemporalResolution.hour
      ));

    assertThat(results).extracting(v -> v.value).containsExactly(
      2.0, // (2.0 - 0.0) / 1
      2.5  // ((5.0 - 1.0) + (3.0 - 2.0)) / 2
    );
  }
}
