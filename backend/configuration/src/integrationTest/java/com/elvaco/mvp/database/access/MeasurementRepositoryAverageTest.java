package com.elvaco.mvp.database.access;

import java.time.Duration;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.DisplayQuantity;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeasurementParameter;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.QuantityParameter;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.DisplayMode.CONSUMPTION;
import static com.elvaco.mvp.core.domainmodels.DisplayMode.READOUT;
import static com.elvaco.mvp.core.domainmodels.Units.CUBIC_METRES;
import static com.elvaco.mvp.core.domainmodels.Units.DEGREES_CELSIUS;
import static com.elvaco.mvp.core.domainmodels.Units.KILOWATT_HOURS;
import static com.elvaco.mvp.core.domainmodels.Units.PERCENT;
import static com.elvaco.mvp.core.domainmodels.Units.WATT;
import static com.elvaco.mvp.core.spi.data.RequestParameter.LOGICAL_METER_ID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class MeasurementRepositoryAverageTest extends IntegrationTest {
  private static final DisplayQuantity VOLUME_DISPLAY = new DisplayQuantity(
    Quantity.VOLUME,
    CONSUMPTION,
    CUBIC_METRES
  );

  private static final DisplayQuantity POWER_DISPLAY = new DisplayQuantity(
    Quantity.POWER,
    READOUT,
    WATT
  );

  private static final DisplayQuantity EXTERNAL_TEMPERATUR_DISPLAY = new DisplayQuantity(
    Quantity.EXTERNAL_TEMPERATURE,
    READOUT,
    DEGREES_CELSIUS
  );

  private static final DisplayQuantity HUMIDITY_DISPLAY = new DisplayQuantity(
    Quantity.HUMIDITY,
    READOUT,
    PERCENT
  );

  private static final DisplayQuantity ENERGY_DISPLAY = new DisplayQuantity(
    Quantity.ENERGY,
    CONSUMPTION,
    KILOWATT_HOURS
  );

  @Test
  public void correctNumberOfValuesAreReturnedRelativeToStart() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());

    given(measurementSeries()
      .forMeter(meter)
      .withQuantity(Quantity.VOLUME)
      .startingAt(start)
      .withInterval(Duration.ofHours(1))
      .withValues(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        idParametersOf(meter),
        quantityParametersOf(VOLUME_DISPLAY),
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

    given(measurementSeries()
      .forMeter(meter)
      .withQuantity(Quantity.POWER)
      .startingAt(start)
      .withInterval(Duration.ofDays(1))
      .withValues(2.0, 4.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        idParametersOf(meter),
        quantityParametersOf(POWER_DISPLAY),
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

    given(measurementSeries()
      .forMeter(meter)
      .withQuantity(Quantity.POWER)
      .startingAt(start)
      .withInterval(Period.ofMonths(1))
      .withValues(2.0, 4.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        idParametersOf(meter),
        quantityParametersOf(POWER_DISPLAY),
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

    given(measurementSeries()
      .forMeter(meter)
      .startingAt(start)
      .withQuantity(Quantity.POWER)
      .withValues(2.0));
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(start.plusHours(2))
      .withQuantity(Quantity.POWER)
      .withValues(3.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        idParametersOf(meter),
        quantityParametersOf(POWER_DISPLAY),
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

    given(measurementSeries()
      .forMeter(meter)
      .withQuantity(Quantity.POWER)
      .startingAt(start)
      .withInterval(interval)
      .withValues(2.0, 100.0));
    given(measurementSeries()
      .forMeter(meter)
      .withQuantity(Quantity.POWER)
      .startingAt(start.plusHours(1))
      .withInterval(interval)
      .withValues(1.0, 9.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        idParametersOf(meter),
        quantityParametersOf(POWER_DISPLAY),
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

    given(measurementSeries()
      .forMeter(meterOne)
      .startingAt(start)
      .withQuantity(Quantity.POWER)
      .withValues(12));
    given(measurementSeries()
      .forMeter(meterTwo)
      .startingAt(start)
      .withQuantity(Quantity.POWER)
      .withValues(99.8));

    RequestParameters parameters = idParametersOf(meterOne);
    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        parameters,
        quantityParametersOf(POWER_DISPLAY),
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

    given(measurementSeries()
      .forMeter(meter)
      .startingAt(start)
      .withQuantity(Quantity.POWER)
      .withValues(2.0));
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(start)
      .withQuantity(Quantity.RETURN_TEMPERATURE)
      .withValues(6.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        idParametersOf(meter),
        quantityParametersOf(POWER_DISPLAY),
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
    var meter = given(logicalMeter().meterDefinition(MeterDefinition.DEFAULT_ROOM_SENSOR));

    given(measurementSeries()
      .forMeter(meter)
      .startingAt(start)
      .withQuantity(Quantity.EXTERNAL_TEMPERATURE)
      .withValues(2.0));
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(start)
      .withQuantity(Quantity.HUMIDITY)
      .withValues(6.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        idParametersOf(meter),
        quantityParametersOf(EXTERNAL_TEMPERATUR_DISPLAY, HUMIDITY_DISPLAY),
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
  public void averageForMetersInDifferentTimezones() {
    /* This test case exists for documentation purposes. The behaviour it asserts is
    * not necessarily "correct" from a user's point of view.
    *
    * The answer to the question "What should an average series for meters in different
    * time zones look like?" is unfortunately, "It depends.".
    *
    * The current behaviour is to group values on the global time line and average those. Another
    * approach might be to group the values according to their local time, so that the average for
    * 00:00 includes the values 00:00+01 (23:00Z) for the first meter and 00:00+02 (22:00Z) for the
    * second meter. Yet another approach could be to include the offset in the grouping, rendering
    * one series for each distinct offset. For example:
    * [00:00+01 (23:00Z), 01:00+01 (00:00Z), 02:00+01 (01:00Z)]
    * and
    * [00:00+02 (22:00Z), 01:00+02 (23:00Z), 02:00+02 (00:00Z)]
    * .
    *
    * But, as mentioned above, what the user actually want, depends on what they hope to accomplish,
    * and we don't know that right now.
    * */
    ZonedDateTime start = context().now().withZoneSameLocal(ZoneId.of("Z"));
    var meterInUtcPlusOne = given(logicalMeter().utcOffset("+01"));
    var meterInUtcPlusTwo = given(logicalMeter().utcOffset("+02"));

    given(measurementSeries()
      .forMeter(meterInUtcPlusOne)
      .startingAt(start.minusHours(1)) // 00:00:00 +01
      .withQuantity(Quantity.POWER)
      .withValues(1.0, 2.0));

    given(measurementSeries()
      .forMeter(meterInUtcPlusTwo)
      .startingAt(start.minusHours(2)) // 00:00:00 +02
      .withQuantity(Quantity.POWER)
      .withValues(1.0, 2.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        idParametersOf(meterInUtcPlusOne, meterInUtcPlusTwo),
        quantityParametersOf(POWER_DISPLAY),
        start.minusHours(2),
        start.plusHours(1),
        TemporalResolution.hour
      ));

    assertThat(result.get(Quantity.POWER.name))
      .extracting(v -> v.value)
      .containsExactly(
        1.0, // 1.0 / 1 (only meter+01 has a value for this hour)
        1.5, // (1.0 + 2.0) / 2 (meter+01's second value + meter+02's first value)
        2.0, // 2.0 / 1 (only meter+02 has a value for this hour)
        null // no values
      );
  }

  @Test
  public void fetchMultipleConsumptionQuantities() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());

    given(measurementSeries()
      .forMeter(meter)
      .startingAt(start)
      .withQuantity(Quantity.ENERGY)
      .withValues(2.0, 4.0));
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(start)
      .withQuantity(Quantity.VOLUME)
      .withValues(6.0, 12.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        idParametersOf(meter),
        quantityParametersOf(ENERGY_DISPLAY, VOLUME_DISPLAY),
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
    var meter = given(logicalMeter().meterDefinition(MeterDefinition.DEFAULT_DISTRICT_HEATING));

    //should be included
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(start)
      .withQuantity(Quantity.POWER)
      .withValues(2.0));
    //should not be included (not requested)
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(start)
      .withQuantity(Quantity.RETURN_TEMPERATURE)
      .withValues(6.0));
    //should be included but not contain values (not part of meter definition)
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(start)
      .withQuantity(Quantity.HUMIDITY)
      .withValues(18.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        idParametersOf(meter),
        quantityParametersOf(POWER_DISPLAY, HUMIDITY_DISPLAY),
        start,
        start.plusSeconds(1),
        TemporalResolution.hour
      ));

    assertThat(result.keySet()).containsExactlyInAnyOrder(
      Quantity.POWER.name,
      Quantity.HUMIDITY.name
    );
    assertThat(result.get(Quantity.POWER.name)).extracting(v -> v.value).containsExactly(2.0);
    assertThat(result.get(Quantity.HUMIDITY.name)).extracting(v -> v.value).containsOnlyNulls();
  }

  @Test
  public void timesAreCorrect() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());

    given(measurementSeries()
      .forMeter(meter)
      .startingAt(start)
      .withQuantity(Quantity.ENERGY)
      .withValues(2.0));

    Map<String, List<MeasurementValue>> resultDayResolution = measurements.findAverageForPeriod(
      new MeasurementParameter(
        idParametersOf(meter),
        quantityParametersOf(ENERGY_DISPLAY),
        start,
        start.plusSeconds(1),
        TemporalResolution.day
      ));

    Map<String, List<MeasurementValue>> resultMonthResolution = measurements.findAverageForPeriod(
      new MeasurementParameter(
        idParametersOf(meter),
        quantityParametersOf(ENERGY_DISPLAY),
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

    given(measurementSeries()
      .forMeter(meterOne)
      .withQuantity(Quantity.ENERGY)
      .startingAt(start.plusHours(1))
      .withInterval(interval)
      .withValues(0.0, 1.0, 5.0));
    given(measurementSeries()
      .forMeter(meterTwo)
      .withQuantity(Quantity.ENERGY)
      .startingAt(start.plusHours(1))
      .withInterval(interval)
      .withValues(1.0, 2.0, 3.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        idParametersOf(meterOne, meterTwo),
        quantityParametersOf(ENERGY_DISPLAY),
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

    given(measurementSeries()
      .forMeter(meterOne)
      .withQuantity(Quantity.ENERGY)
      .startingAt(start.plusHours(1))
      .withInterval(interval)
      .withValues(0.0, 1.0, 5.0));
    given(measurementSeries()
      .forMeter(meterTwo)
      .withQuantity(Quantity.ENERGY)
      .startingAt(start.plusHours(1))
      .withInterval(interval)
      .withValues(1.0, 2.0, 3.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        idParametersOf(meterOne, meterTwo),
        quantityParametersOf(ENERGY_DISPLAY),
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

    given(measurementSeries()
      .forMeter(meterOne)
      .withQuantity(Quantity.POWER)
      .startingAt(start.plusHours(2))
      .withInterval(interval)
      .withValues(1.0, 5.0));
    given(measurementSeries()
      .forMeter(meterTwo)
      .withQuantity(Quantity.POWER)
      .startingAt(start.plusHours(1))
      .withInterval(interval)
      .withValues(7.0, 2.0, 3.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        idParametersOf(meterOne, meterTwo),
        quantityParametersOf(POWER_DISPLAY),
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

    given(measurementSeries()
      .forMeter(meterOne)
      .withQuantity(Quantity.ENERGY)
      .startingAt(start.plusHours(2))
      .withInterval(interval)
      .withValues(1.0, 5.0));
    given(measurementSeries()
      .forMeter(meterTwo)
      .withQuantity(Quantity.ENERGY)
      .startingAt(start.plusHours(1))
      .withInterval(interval)
      .withValues(0.0, 2.0, 3.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        idParametersOf(meterOne, meterTwo),
        quantityParametersOf(ENERGY_DISPLAY),
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

    given(measurementSeries()
      .forMeter(logicalMeter)
      .forPhysicalMeter(physicalMeterOne)
      .withQuantity(Quantity.POWER)
      .startingAt(start.minusDays(2))
      .withInterval(interval)
      .withValues(2.0, 4.0));
    given(measurementSeries()
      .forMeter(logicalMeter)
      .forPhysicalMeter(physicalMeterTwo)
      .withQuantity(Quantity.POWER)
      .startingAt(start)
      .withInterval(interval)
      .withValues(6.0, 12.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        idParametersOf(logicalMeter),
        quantityParametersOf(POWER_DISPLAY),
        start.minusDays(2),
        start.plusDays(1),
        TemporalResolution.day
      ));

    assertThat(result.get(Quantity.POWER.name))
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

    given(measurementSeries()
      .forMeter(logicalMeter)
      .forPhysicalMeter(physicalMeterOne)
      .withQuantity(Quantity.VOLUME)
      .startingAt(start.minusDays(2))
      .withInterval(interval)
      .withValues(2.0, 4.0));
    given(measurementSeries()
      .forMeter(logicalMeter)
      .forPhysicalMeter(physicalMeterTwo)
      .withQuantity(Quantity.VOLUME)
      .startingAt(start)
      .withInterval(interval)
      .withValues(6.0, 12.0));

    Map<String, List<MeasurementValue>> result = measurements.findAverageForPeriod(
      new MeasurementParameter(
        idParametersOf(logicalMeter),
        quantityParametersOf(VOLUME_DISPLAY),
        start.minusDays(2),
        start,
        TemporalResolution.day
      ));

    assertThat(result.get(Quantity.VOLUME.name))
      .extracting(l -> l.value)
      .containsExactly(2.0, null, 6.0);
  }

  private RequestParameters idParametersOf(LogicalMeter... meters) {
    Map<String, List<String>> multiValueMap = Map.of(
      LOGICAL_METER_ID.toString(),
      Arrays.stream(meters).map(meter -> meter.id.toString()).collect(toList())
    );
    return RequestParametersAdapter.of(multiValueMap);
  }

  private List<QuantityParameter> quantityParametersOf(DisplayQuantity... quantities) {
    return Arrays.stream(quantities).map(quantity -> new QuantityParameter(
      quantity.quantity.name,
      quantity.unit,
      quantity.displayMode
    )).collect(toList());
  }
}
