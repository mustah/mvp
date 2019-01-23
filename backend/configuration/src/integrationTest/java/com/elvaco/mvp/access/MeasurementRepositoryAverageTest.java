package com.elvaco.mvp.access;

import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import com.elvaco.mvp.core.domainmodels.MeasurementParameter;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MeasurementRepositoryAverageTest extends IntegrationTest {

  @Test
  public void correctNumberOfValuesAreReturnedRelativeToStart() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());

    given(series(meter, Quantity.VOLUME, start, Duration.ofHours(1),
      1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0
    ));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        List.of(meter.id),
        List.of(Quantity.VOLUME),
        start.plusHours(5),
        start.plusHours(9),
        TemporalResolution.hour
      ));
    assertThat(result.get(Quantity.VOLUME.name)).hasSize(5);
  }

  @Test
  public void resultsCanBeFetchedWithDayResolution() {
    ZonedDateTime start = context().now();
    var meter = given(physicalMeter());

    given(series(meter, Quantity.POWER, start, Duration.ofDays(1), 2.0, 4.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        List.of(meter.id),
        List.of(Quantity.POWER),
        start,
        start.plusDays(1),
        TemporalResolution.day
      ));

    assertThat(result.get(Quantity.POWER.name)).extracting(v -> v.value).containsExactly(2.0, 4.0);
  }

  @Test
  public void resultsCanBeFetchedWithMonthResolution() {
    ZonedDateTime start = context().now();
    var meter = given(physicalMeter());

    given(series(meter, Quantity.POWER, start, Period.ofMonths(1), 2.0, 4.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        List.of(meter.id),
        List.of(Quantity.POWER),
        start,
        start.plusMonths(1),
        TemporalResolution.month
      ));

    assertThat(result.get(Quantity.POWER.name)).extracting(v -> v.value).containsExactly(2.0, 4.0);
  }

  @Test
  public void missingIntervalValuesAreRepresentedAsNull() {
    ZonedDateTime start = context().now();
    var meter = given(physicalMeter());

    given(series(meter, Quantity.POWER, start, 2.0));
    given(series(meter, Quantity.POWER, start.plusHours(2), 3.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        List.of(meter.id),
        List.of(Quantity.POWER),
        start,
        start.plusHours(2),
        TemporalResolution.hour
      ));

    assertThat(result.get(Quantity.POWER.name)).extracting(v -> v.value).containsExactly(
      2.0,
      null,
      3.0
    );
  }

  @Test
  public void averageShouldOnlyIncludeValuesAtResolutionPoints() {
    ZonedDateTime start = context().now();
    var meter = given(physicalMeter());
    var interval = Duration.ofMinutes(1);

    given(series(meter, Quantity.POWER, start, interval, 2.0, 100.0));
    given(series(meter, Quantity.POWER, start.plusHours(1), interval, 1.0, 9.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        List.of(meter.id),
        List.of(Quantity.POWER),
        start,
        start.plusHours(1).plusMinutes(1),
        TemporalResolution.hour
      ));

    assertThat(result.get(Quantity.POWER.name)).extracting(v -> v.value).containsExactly(2.0, 1.0);
  }

  @Test
  public void unspecifiedMetersAreNotIncluded() {
    ZonedDateTime start = context().now();
    var meterOne = given(logicalMeter());
    var meterTwo = given(logicalMeter());

    given(series(meterOne, Quantity.POWER, start, 12));
    given(series(meterTwo, Quantity.POWER, start, 99.8));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        List.of(meterOne.id),
        List.of(Quantity.POWER),
        start,
        start.plusSeconds(1),
        TemporalResolution.hour
      ));

    assertThat(result.get(Quantity.POWER.name)).extracting(v -> v.value).containsExactly(12.0);
  }

  @Test
  public void valuesAreFilteredByQuantity() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());

    given(series(meter, Quantity.POWER, start, 2.0));
    given(series(meter, Quantity.RETURN_TEMPERATURE, start, 6.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        List.of(meter.id),
        List.of(Quantity.POWER),
        start,
        start.plusSeconds(1),
        TemporalResolution.hour
      ));

    assertThat(result.keySet()).containsOnly(Quantity.POWER.name);
    assertThat(result.get(Quantity.POWER.name)).extracting(v -> v.value).containsExactly(2.0);
  }

  @Test
  public void fetchMultipleReadoutQuantities() {

    ZonedDateTime start = context().now();
    var meter = given(logicalMeter().meterDefinition(MeterDefinition.ROOM_SENSOR_METER));

    given(series(meter, Quantity.EXTERNAL_TEMPERATURE, start, 2.0));
    given(series(meter, Quantity.HUMIDITY, start, 6.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        List.of(meter.id),
        List.of(Quantity.EXTERNAL_TEMPERATURE, Quantity.HUMIDITY),
        start,
        start.plusSeconds(1),
        TemporalResolution.hour
      ));

    assertThat(result.get(Quantity.EXTERNAL_TEMPERATURE.name)).extracting(v -> v.value)
      .containsExactly(2.0);
    assertThat(result.get(Quantity.HUMIDITY.name)).extracting(v -> v.value)
      .containsExactly(6.0);
  }

  @Test
  public void fetchMultipleConsumptionQuantities() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());

    given(series(meter, Quantity.ENERGY, start, 2.0, 4.0));
    given(series(meter, Quantity.VOLUME, start, 6.0, 12.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        List.of(meter.id),
        List.of(Quantity.ENERGY, Quantity.VOLUME),
        start,
        start.plusSeconds(1),
        TemporalResolution.hour
      ));

    assertThat(result.get(Quantity.ENERGY.name)).extracting(v -> v.value).containsExactly(2.0);
    assertThat(result.get(Quantity.VOLUME.name)).extracting(v -> v.value).containsExactly(6.0);
  }

  @Test
  public void quantitiesAreFilteredOnMeterDefinition() {

    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());

    given(series(meter, Quantity.POWER, start, 2.0));
    given(series(meter, Quantity.RETURN_TEMPERATURE, start, 6.0));
    given(series(meter, Quantity.EXTERNAL_TEMPERATURE, start, 18.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        List.of(meter.id),
        List.of(Quantity.POWER, Quantity.EXTERNAL_TEMPERATURE),
        start,
        start.plusSeconds(1),
        TemporalResolution.hour
      ));

    assertThat(result.keySet()).containsOnly(Quantity.POWER.name);
    assertThat(result.get(Quantity.POWER.name)).extracting(v -> v.value).containsExactly(2.0);
  }

  @Test
  public void timesAreCorrect() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());

    given(series(meter, Quantity.ENERGY, start, 2.0));

    Map<String, List<MeasurementValue>> resultDayResolution = measurements.findAverageForPeriod(
      new MeasurementParameter(
        List.of(meter.id),
        List.of(Quantity.ENERGY),
        start,
        start.plusSeconds(1),
        TemporalResolution.day
      ));

    Map<String, List<MeasurementValue>> resultMonthResolution = measurements.findAverageForPeriod(
      new MeasurementParameter(
        List.of(meter.id),
        List.of(Quantity.ENERGY),
        start,
        start.plusSeconds(1),
        TemporalResolution.month
      ));

    assertThat(resultDayResolution.get(Quantity.ENERGY.name)).extracting(v -> v.when)
      .containsExactly(start.toInstant());
    assertThat(resultMonthResolution.get(Quantity.ENERGY.name)).extracting(v -> v.when)
      .containsExactly(start.toInstant());
  }

  @Test
  public void averageForConsumptionSeries() {
    ZonedDateTime start = context().now();
    var meterOne = given(logicalMeter());
    var meterTwo = given(logicalMeter());
    var interval = Duration.ofHours(1);

    given(series(meterOne, Quantity.ENERGY, start.plusHours(1), interval, 0.0, 1.0, 5.0));
    given(series(meterTwo, Quantity.ENERGY, start.plusHours(1), interval, 1.0, 2.0, 3.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        List.of(meterOne.id, meterTwo.id),
        List.of(Quantity.ENERGY),
        start.plusHours(1),
        start.plusHours(3),
        TemporalResolution.hour
      ));

    assertThat(result.get(Quantity.ENERGY.name)).extracting(v -> v.value).containsExactly(
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

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        List.of(meterOne.id, meterTwo.id),
        List.of(Quantity.ENERGY),
        start.plusHours(1),
        start.plusHours(2),
        TemporalResolution.hour
      ));

    assertThat(result.get(Quantity.ENERGY.name)).extracting(v -> v.value).containsExactly(
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

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        List.of(meterOne.id, meterTwo.id),
        List.of(Quantity.POWER),
        start.plusHours(1),
        start.plusHours(4),
        TemporalResolution.hour
      ));

    assertThat(result.get(Quantity.POWER.name)).extracting(v -> v.value).containsExactly(
      7.0,
      1.5,
      4.0,
      null
    );
  }

  @Test
  public void averageForConsumptionSeries_missingMeasurementsForOneMeter() {
    ZonedDateTime start = context().now();
    var meterOne = given(logicalMeter());
    var meterTwo = given(logicalMeter());
    var interval = Duration.ofHours(1);

    given(series(meterOne, Quantity.ENERGY, start.plusHours(2), interval, 1.0, 5.0));
    given(series(meterTwo, Quantity.ENERGY, start.plusHours(1), interval, 0.0, 2.0, 3.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        List.of(meterOne.id, meterTwo.id),
        List.of(Quantity.ENERGY),
        start.plusHours(1),
        start.plusHours(2),
        TemporalResolution.hour
      ));

    assertThat(result.get(Quantity.ENERGY.name)).extracting(v -> v.value).containsExactly(
      2.0, // (2.0 - 0.0) / 1
      2.5  // ((5.0 - 1.0) + (3.0 - 2.0)) / 2
    );
  }

  @Test
  public void readoutValuesAverageAreFilteredOnActivePeriod() {

    ZonedDateTime start = context().now();
    var logicalMeter = given(
      logicalMeter(),
      physicalMeter().activePeriod(PeriodRange.halfOpenFrom(start.minusDays(2), start)),
      physicalMeter().activePeriod(PeriodRange.from(start))
    );

    var physicalMeterOne = logicalMeter.physicalMeters.get(0);
    var physicalMeterTwo = logicalMeter.physicalMeters.get(1);
    var interval = Duration.ofDays(1);

    given(series(physicalMeterOne, Quantity.VOLUME_FLOW, start.minusDays(2), interval, 2.0, 4.0));
    given(series(physicalMeterTwo, Quantity.VOLUME_FLOW, start, interval, 6.0, 12.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        List.of(logicalMeter.id),
        List.of(Quantity.VOLUME_FLOW),
        start.minusDays(2),
        start.plusDays(1),
        TemporalResolution.day
      ));

    assertThat(result.get(Quantity.VOLUME_FLOW.name))
      .extracting(l -> l.value)
      .containsExactly(2.0, 4.0, 6.0, 12.0);
  }

  @Test
  public void consumptionValuesAverageAreFilteredOnActivePeriod() {

    ZonedDateTime start = context().now();
    var logicalMeter = given(
      logicalMeter(),
      physicalMeter().activePeriod(PeriodRange.halfOpenFrom(start.minusDays(2), start)),
      physicalMeter().activePeriod(PeriodRange.from(start))
    );

    var physicalMeterOne = logicalMeter.physicalMeters.get(0);
    var physicalMeterTwo = logicalMeter.physicalMeters.get(1);
    var interval = Duration.ofDays(1);

    given(series(physicalMeterOne, Quantity.VOLUME, start.minusDays(2), interval, 2.0, 4.0));
    given(series(physicalMeterTwo, Quantity.VOLUME, start, interval, 6.0, 12.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        List.of(logicalMeter.id),
        List.of(Quantity.VOLUME),
        start.minusDays(2),
        start,
        TemporalResolution.day
      ));

    assertThat(result.get(Quantity.VOLUME.name))
      .extracting(l -> l.value)
      .containsExactly(2.0, 2.0, 6.0);
  }
}
