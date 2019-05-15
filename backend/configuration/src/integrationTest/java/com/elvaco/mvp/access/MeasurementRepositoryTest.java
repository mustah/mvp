package com.elvaco.mvp.access;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.DoubleStream;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.DisplayMode;
import com.elvaco.mvp.core.domainmodels.DisplayQuantity;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementKey;
import com.elvaco.mvp.core.domainmodels.MeasurementParameter;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.PeriodBound;
import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.QuantityParameter;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.core.exception.UnitConversionError;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import static com.elvaco.mvp.core.domainmodels.DisplayMode.READOUT;
import static com.elvaco.mvp.core.domainmodels.Units.CUBIC_METRES;
import static com.elvaco.mvp.core.domainmodels.Units.KILOWATT;
import static com.elvaco.mvp.core.domainmodels.Units.WATT;
import static com.elvaco.mvp.core.spi.data.RequestParameter.LOGICAL_METER_ID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MeasurementRepositoryTest extends IntegrationTest {

  private static final DisplayQuantity VOLUME_DISPLAY = new DisplayQuantity(
    Quantity.VOLUME,
    DisplayMode.CONSUMPTION,
    CUBIC_METRES
  );

  private static final DisplayQuantity POWER_DISPLAY = new DisplayQuantity(
    Quantity.POWER,
    READOUT,
    WATT
  );
  private static final DisplayQuantity POWER_DISPLAY_KW = new DisplayQuantity(
    Quantity.POWER,
    READOUT,
    KILOWATT
  );

  @Test
  public void getSeriesForConsumption_PutConsumptionOnStartOfInterval() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(start)
      .withQuantity(Quantity.VOLUME)
      .withValues(3.0, 6.0, 12.0, 24.0));

    Map<MeasurementKey, List<MeasurementValue>> result =
      measurements.findSeriesForPeriod(
        new MeasurementParameter(
          idParametersOf(meter),
          quantityParametersOf(VOLUME_DISPLAY),
          start,
          start.plusHours(2),
          TemporalResolution.hour
        )
      );

    assertThat(result.get(getKey(meter, Quantity.VOLUME)))
      .extracting(value -> value.value)
      .containsExactly(3.0, 6.0, 12.0);
  }

  @Test
  public void getSeriesForConsumption_PutSumOfMissingIntervalsOnStartOfInterval() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(start)
      .withQuantity(Quantity.VOLUME)
      .withValues(3.0, 6.0));
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(start.plusHours(4))
      .withQuantity(Quantity.VOLUME)
      .withValues(48.0, 96.0));

    Map<MeasurementKey, List<MeasurementValue>> result =
      measurements.findSeriesForPeriod(
        new MeasurementParameter(
          idParametersOf(meter),
          quantityParametersOf(VOLUME_DISPLAY),
          start,
          start.plusHours(4),
          TemporalResolution.hour
        )
      );
    assertThat(result.get(getKey(meter, Quantity.VOLUME)))
      .extracting(value -> value.value)
      .containsExactly(
        3.0,
        42.0,
        null,
        null,
        48.0
      );
  }

  @Test
  public void getSeriesForConsumption_MissingMeasurementAtStartOfIntervalAndAfterInterval() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(start.minusHours(1))
      .withQuantity(Quantity.VOLUME)
      .withValues(1.0));
    // missing measurement for 'start'
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(start.plusHours(1))
      .withQuantity(Quantity.VOLUME)
      .withValues(6.0, 12.0));
    // missing measurement after interval

    Map<MeasurementKey, List<MeasurementValue>> result =
      measurements.findSeriesForPeriod(
        new MeasurementParameter(
          idParametersOf(meter),
          quantityParametersOf(VOLUME_DISPLAY),
          start,
          start.plusHours(2),
          TemporalResolution.hour
        )
      );

    assertThat(result.get(getKey(meter, Quantity.VOLUME)))
      .extracting(value -> value.value)
      .containsExactly(
        null,
        6.0,
        null
      );
  }

  @Test
  public void getSeriesForConsumption_MissingMeasurementAtEndOfInterval() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(start)
      .withQuantity(Quantity.VOLUME)
      .withValues(3.0, 6.0));
    // missing measurement at end of interval

    Map<MeasurementKey, List<MeasurementValue>> result =
      measurements.findSeriesForPeriod(
        new MeasurementParameter(
          idParametersOf(meter),
          quantityParametersOf(VOLUME_DISPLAY),
          start,
          start.plusHours(2),
          TemporalResolution.hour
        )
      );

    assertThat(result.get(getKey(meter, Quantity.VOLUME)))
      .extracting(value -> value.value)
      .containsExactly(
        3.0,
        null,
        null
      );
  }

  @Test
  public void getSeriesForConsumption_MissingMeasurementAtEndOfIntervalButLaterExists() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(start)
      .withQuantity(Quantity.VOLUME)
      .withValues(3.0, 6.0));
    // missing measurement at end of interval
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(start.plusHours(4))
      .withQuantity(Quantity.VOLUME)
      .withValues(24.0));

    Map<MeasurementKey, List<MeasurementValue>> result =
      measurements.findSeriesForPeriod(
        new MeasurementParameter(
          idParametersOf(meter),
          quantityParametersOf(VOLUME_DISPLAY),
          start,
          start.plusHours(2),
          TemporalResolution.hour
        )
      );

    assertThat(result.get(getKey(meter, Quantity.VOLUME)))
      .extracting(value -> value.value)
      .containsExactly(
        3.0,
        null,
        null
      );
  }

  @Test
  public void getSeriesForConsumption_MissingMeasurementAfterInterval() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(start)
      .withQuantity(Quantity.VOLUME)
      .withValues(3.0, 6.0, 12.0));
    // missing measurement at START_TIME.plusHours(3)

    Map<MeasurementKey, List<MeasurementValue>> result =
      measurements.findSeriesForPeriod(
        new MeasurementParameter(
          idParametersOf(meter),
          quantityParametersOf(VOLUME_DISPLAY),
          start,
          start.plusHours(2),
          TemporalResolution.hour
        ));

    assertThat(result.get(getKey(meter, Quantity.VOLUME)))
      .extracting(value -> value.value)
      .containsExactly(3.0, 6.0, null);
  }

  @Test
  public void seriesShouldIncludeEmptyResolutionPoints() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());
    given(measurementSeries()
      .forMeter(meter)
      .startingAt(start)
      .withQuantity(Quantity.POWER)
      .withValues(1.0, 2.0));

    Map<MeasurementKey, List<MeasurementValue>> result = measurements.findSeriesForPeriod(
      new MeasurementParameter(
        idParametersOf(meter),
        quantityParametersOf(POWER_DISPLAY),
        start,
        start.plusHours(2),
        TemporalResolution.hour
      )
    );

    assertThat(result.get(getKey(meter, Quantity.POWER)))
      .extracting(value -> value.value)
      .containsExactly(
        1.0,
        2.0,
        null
      );
  }

  @Test
  public void seriesShouldNotIncludeValuesInBetweenResolutionPoints() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());
    given(measurementSeries()
      .forMeter(meter)
      .withQuantity(Quantity.POWER)
      .startingAt(start)
      .withInterval(Duration.ofMinutes(30))
      .withValues(1.0, 1.5, 2.0));

    Map<MeasurementKey, List<MeasurementValue>> result = measurements.findSeriesForPeriod(
      new MeasurementParameter(
        idParametersOf(meter),
        quantityParametersOf(POWER_DISPLAY),
        start,
        start.plusHours(1),
        TemporalResolution.hour
      )
    );

    assertThat(result.get(getKey(meter, Quantity.POWER)))
      .extracting(value -> value.value)
      .containsExactly(
        1.0,
        2.0
      );
  }

  @Test
  public void readoutSeriesForMultipleMeters() {
    ZonedDateTime start = context().now();
    var meterOne = given(logicalMeter());
    var meterTwo = given(logicalMeter());

    given(measurementSeries()
      .forMeter(meterOne)
      .startingAt(start)
      .withQuantity(Quantity.POWER)
      .withValues(1.0, 2.0, 3.0));
    given(measurementSeries()
      .forMeter(meterTwo)
      .startingAt(start)
      .withQuantity(Quantity.POWER)
      .withValues(10.0, 20.0, 30.0));

    Map<MeasurementKey, List<MeasurementValue>> result = measurements.findSeriesForPeriod(
      new MeasurementParameter(
        idParametersOf(meterOne, meterTwo),
        quantityParametersOf(POWER_DISPLAY),
        start,
        start.plusHours(2),
        TemporalResolution.hour
      )
    );

    assertThat(result.get(getKey(meterOne, Quantity.POWER)))
      .extracting(value -> value.value)
      .containsExactly(
        1.0,
        2.0,
        3.0
      );

    assertThat(result.get(getKey(meterTwo, Quantity.POWER)))
      .extracting(value -> value.value)
      .containsExactly(
        10.0,
        20.0,
        30.0
      );
  }

  @Test
  public void consumptionSeriesForMultipleMeters() {
    ZonedDateTime start = context().now();
    var meterOne = given(logicalMeter());
    var meterTwo = given(logicalMeter());

    given(measurementSeries()
      .forMeter(meterOne)
      .startingAt(start)
      .withQuantity(Quantity.VOLUME)
      .withValues(1.0, 2.0, 3.0));
    given(measurementSeries()
      .forMeter(meterTwo)
      .startingAt(start)
      .withQuantity(Quantity.VOLUME)
      .withValues(10.0, 20.0, 30.0));

    Map<MeasurementKey, List<MeasurementValue>> result = measurements.findSeriesForPeriod(
      new MeasurementParameter(
        idParametersOf(meterOne, meterTwo),
        quantityParametersOf(VOLUME_DISPLAY),
        start,
        start.plusHours(1),
        TemporalResolution.hour
      )
    );

    assertThat(result.get(getKey(meterOne, Quantity.VOLUME)))
      .extracting(value -> value.value)
      .containsExactly(
        1.0,
        1.0
      );

    assertThat(result.get(getKey(meterTwo, Quantity.VOLUME)))
      .extracting(value -> value.value)
      .containsExactly(
        10.0,
        10.0
      );
  }

  @Test
  public void consumptionSeriesWithDayResolution() {
    ZonedDateTime start = context().now();
    var meterOne = given(logicalMeter());

    given(measurementSeries()
      .forMeter(meterOne)
      .startingAt(start)
      .withQuantity(Quantity.VOLUME)
      .withValues(DoubleStream.iterate(1, d -> d + 1).limit(25).toArray()));

    Map<MeasurementKey, List<MeasurementValue>> result = measurements.findSeriesForPeriod(
      new MeasurementParameter(
        idParametersOf(meterOne),
        quantityParametersOf(VOLUME_DISPLAY),
        start,
        start.plusDays(1).minusSeconds(1),
        TemporalResolution.day
      )
    );

    assertThat(result.get(getKey(meterOne, Quantity.VOLUME)))
      .extracting(value -> value.value)
      .containsExactly(
        24.0 // 25.0 at hour 0 of day 1 - 1.0 at hour 0 of day 0 = 24.0.
      );
  }

  @Test
  public void readoutValuesAreFilteredOnActivePeriod() {
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

    Map<MeasurementKey, List<MeasurementValue>> result = measurements.findSeriesForPeriod(
      new MeasurementParameter(
        idParametersOf(logicalMeter),
        quantityParametersOf(POWER_DISPLAY),
        start.minusDays(2),
        start.plusDays(1),
        TemporalResolution.day
      ));

    assertThat(result.get(getKey(logicalMeter, physicalMeterOne, Quantity.POWER)))
      .extracting(l -> l.value)
      .containsExactly(2.0, 4.0);

    assertThat(result.get(getKey(logicalMeter, physicalMeterTwo, Quantity.POWER)))
      .extracting(l -> l.value)
      .containsExactly(6.0, 12.0);
  }

  @Test
  public void readoutValuesForMetersWithMixedOffsets() {
    ZonedDateTime start = context().now().withZoneSameLocal(ZoneId.of("Z"));
    var logicalMeterPlus1 = given(
      logicalMeter().utcOffset("+01")
    );

    var logicalMeterPlus2 = given(
      logicalMeter().utcOffset("+02")
    );

    var interval = Duration.ofHours(1);

    given(measurementSeries()
      .forMeter(logicalMeterPlus1)
      .withQuantity(Quantity.POWER)
      .startingAt(start.minusHours(1)) // 00:00:00 +01
      .withInterval(interval)
      .withValues(DoubleStream.iterate(1, (d) -> d + 1.0).limit(25).toArray()));

    given(measurementSeries()
      .forMeter(logicalMeterPlus2)
      .withQuantity(Quantity.POWER)
      .startingAt(start.minusHours(2)) // 00:00:00 +02
      .withInterval(interval)
      .withValues(DoubleStream.iterate(1, (d) -> d + 1.0).limit(25).toArray()));

    Map<MeasurementKey, List<MeasurementValue>> result = measurements.findSeriesForPeriod(
      new MeasurementParameter(
        idParametersOf(logicalMeterPlus1, logicalMeterPlus2),
        quantityParametersOf(POWER_DISPLAY),
        start.minusHours(2),
        start.plusHours(1).plusDays(1),
        TemporalResolution.day
      ));

    assertThat(result.get(getKey(logicalMeterPlus1, Quantity.POWER)))
      .extracting(l -> l.value)
      .containsExactly(
        1.0, // first day's first hourly value (d1 00:00:00 +01:00 / 23:00:00Z)
        25.0 // second day's first hourly value (d2 00:00:00 +01:00 / 23:00:00Z)
      );


    assertThat(result.get(getKey(logicalMeterPlus2, Quantity.POWER)))
      .extracting(l -> l.value)
      .containsExactly(
        1.0, // first day's first hourly value (d1 00:00:00 +02:00 / 22:00:00Z)
        25.0 // second day's first hourly value (d2 00:00:00 +02:00 / 22:00:00Z)
      );
  }

  @Test
  public void readoutValuesForMeterWithExoticOffset() {
    ZonedDateTime start = context().now().withZoneSameLocal(ZoneId.of("Z"));
    var logicalMeter = given(
      logicalMeter().utcOffset("+04")
    );

    var physicalMeter = logicalMeter.physicalMeters.get(0);
    var interval = Duration.ofHours(1);

    given(measurementSeries()
      .forMeter(logicalMeter)
      .forPhysicalMeter(physicalMeter)
      .withQuantity(Quantity.POWER)
      .startingAt(start.minusDays(1).minusHours(4))
      .withInterval(interval)
      .withValues(DoubleStream.iterate(1, (d) -> d + 1.0).limit(48).toArray()));

    Map<MeasurementKey, List<MeasurementValue>> result = measurements.findSeriesForPeriod(
      new MeasurementParameter(
        idParametersOf(logicalMeter),
        quantityParametersOf(POWER_DISPLAY),
        start.minusDays(2),
        start,
        TemporalResolution.day
      ));

    assertThat(result.get(getKey(logicalMeter, physicalMeter, Quantity.POWER)))
      .extracting(l -> l.value)
      .containsExactly(
        1.0, // first day's first hourly value (d1 00:00:00 +04:00 / 20:00:00Z)
        25.0 // second day's first hourly value (d2 00:00:00 +04:00 / 20:00:00Z)
      );
  }

  @Test
  public void consumptionValuesAreFilteredOnActivePeriod() {
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

    Map<MeasurementKey, List<MeasurementValue>> result = measurements.findSeriesForPeriod(
      new MeasurementParameter(
        idParametersOf(logicalMeter),
        quantityParametersOf(VOLUME_DISPLAY),
        start.minusDays(2),
        start,
        TemporalResolution.day
      ));

    assertThat(result.get(getKey(logicalMeter, physicalMeterOne, Quantity.VOLUME)))
      .extracting(l -> l.value)
      .containsExactly(2.0, null);

    assertThat(result.get(getKey(logicalMeter, physicalMeterTwo, Quantity.VOLUME)))
      .extracting(l -> l.value)
      .containsExactly(6.0);
  }

  @Test
  @Transactional
  public void valuesAreSavedWithStorageUnit() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());

    measurements.createOrUpdate(
      Measurement.builder()
        .unit("MWh")
        .value(2.0)
        .physicalMeter(meter.activePhysicalMeter().orElseThrow())
        .readoutTime(start)
        .quantity("Energy")
        .build(),
      meter
    );

    assertThat(measurementJpaRepository.findAll()).extracting(e -> e.value).containsOnly(2000.0);
  }

  @Test
  @Transactional
  public void valuesAreSavedWithStorageUnitUsingSave() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());

    measurements.save(
      Measurement.builder()
        .unit("MWh")
        .value(2.0)
        .physicalMeter(meter.activePhysicalMeter().orElseThrow())
        .readoutTime(start)
        .quantity("Energy")
        .build(),
      meter
    );

    assertThat(measurementJpaRepository.findAll()).extracting(e -> e.value).containsOnly(2000.0);
  }

  @Test
  public void correctScaleIsReturned() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());

    given(measurementSeries()
      .forMeter(meter)
      .forPhysicalMeter(meter.activePhysicalMeter().orElseThrow())
      .withQuantity(Quantity.POWER)
      .startingAt(start)
      .withValues(2000.0));

    DisplayQuantity displayQuantity = POWER_DISPLAY_KW;

    Map<String, List<MeasurementValue>> results = measurements.findAverageForPeriod(
      new MeasurementParameter(
        idParametersOf(meter),
        quantityParametersOf(displayQuantity),
        start,
        start.plusSeconds(1),
        TemporalResolution.hour
      ));

    assertThat(results.get(Quantity.POWER.name)).extracting(v -> v.value).containsOnly(2.0);
  }

  @Test
  public void allValuesHaveSameScale() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());

    PhysicalMeter physicalMeter = meter.activePhysicalMeter().orElseThrow();
    measurements.save(
      Measurement.builder()
        .readoutTime(start)
        .value(2.0)
        .quantity(Quantity.POWER.name)
        .unit(WATT)
        .physicalMeter(physicalMeter)
        .build(),
      meter
    );
    measurements.save(
      Measurement.builder()
        .readoutTime(start.plusHours(1))
        .value(0.002)
        .quantity(Quantity.POWER.name)
        .unit(KILOWATT)
        .physicalMeter(physicalMeter)
        .build(),
      meter
    );

    Map<String, List<MeasurementValue>> results = measurements.findAverageForPeriod(
      new MeasurementParameter(
        idParametersOf(meter),
        quantityParametersOf(POWER_DISPLAY),
        start,
        start.plusHours(1),
        TemporalResolution.hour
      ));

    assertThat(results.get(Quantity.POWER.name)).extracting(v -> v.value).containsOnly(2.0, 2.0);
  }

  @Test
  public void mixedDimensionsAreRejected() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());
    var physicalMeter = meter.activePhysicalMeter().get();

    given(measurementSeries()
      .forMeter(meter)
      .forPhysicalMeter(physicalMeter)
      .withQuantity(Quantity.VOLUME)
      .startingAt(start)
      .withValues(2.0));

    assertThatThrownBy(() -> measurements.save(
      Measurement.builder()
        .physicalMeter(physicalMeter)
        .readoutTime(start.plusMinutes(1))
        .value(2.0)
        .unit("m³/s")
        .quantity("Volume")
        .build(),
      meter
      )
    ).isInstanceOf(UnitConversionError.class);
  }

  @Test
  @Transactional
  public void valuesExpectedTimeIsSetWhenExcpected() {
    ZonedDateTime start = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS);
    var meter = given(logicalMeter().utcOffset("+02"));
    //On schedule
    measurements.createOrUpdate(
      Measurement.builder()
        .unit("MWh")
        .value(2.0)
        .physicalMeter(meter.activePhysicalMeter().orElseThrow())
        .readoutTime(start)
        .quantity("Energy")
        .build(),
      meter
    );
    //Two seconds off schedule
    measurements.createOrUpdate(
      Measurement.builder()
        .unit("MWh")
        .value(2.0)
        .physicalMeter(meter.activePhysicalMeter().orElseThrow())
        .readoutTime(start.plusSeconds(2))
        .quantity("Energy")
        .build(),
      meter
    );

    assertThat(measurementJpaRepository.findAll())
      .extracting(e -> e.expectedTime).containsExactly(start, null);
  }

  @Test
  @Transactional
  public void valuesExpectedTimeIsSetWhenExcpectedTimeZoneDailySchedule() {
    ZonedDateTime start = ZonedDateTime.parse("2007-12-03T00:00:00+03:00");
    var meter = given(logicalMeter().utcOffset("+3")
      .physicalMeter(physicalMeter()
        .readIntervalMinutes(1440)
        .build()));
    //On schedule
    measurements.createOrUpdate(
      Measurement.builder()
        .unit("MWh")
        .value(2.0)
        .physicalMeter(meter.activePhysicalMeter().orElseThrow())
        .readoutTime(start)
        .quantity("Energy")
        .build(),
      meter
    );
    //Two seconds off schedule
    measurements.createOrUpdate(
      Measurement.builder()
        .unit("MWh")
        .value(2.0)
        .physicalMeter(meter.activePhysicalMeter().orElseThrow())
        .readoutTime(start.plusSeconds(2))
        .quantity("Energy")
        .build(),
      meter
    );
    //On schedule
    measurements.createOrUpdate(
      Measurement.builder()
        .unit("MWh")
        .value(2.0)
        .physicalMeter(meter.activePhysicalMeter().orElseThrow())
        .readoutTime(start.plusDays(1L))
        .quantity("Energy")
        .build(),
      meter
    );

    assertThat(measurementJpaRepository.findAll())
      .extracting(e -> e.expectedTime != null ? e.expectedTime.toInstant() : null)
      .containsExactly(start.toInstant(), null, start.plusDays(1L).toInstant());
  }

  @Test
  @Transactional
  public void valuesExpectedTimeIsNotSetWhenTimeZoneDiffFromValuesTimeZone() {
    ZonedDateTime start = ZonedDateTime.parse("2007-12-03T00:00:00+00:00");
    var meter = given(logicalMeter().utcOffset("+3")
      .physicalMeter(physicalMeter()
        .readIntervalMinutes(1440)
        .build()));
    //On schedule
    measurements.createOrUpdate(
      Measurement.builder()
        .unit("MWh")
        .value(2.0)
        .physicalMeter(meter.activePhysicalMeter().orElseThrow())
        .readoutTime(start)
        .quantity("Energy")
        .build(),
      meter
    );
    //Two seconds off schedule
    measurements.createOrUpdate(
      Measurement.builder()
        .unit("MWh")
        .value(2.0)
        .physicalMeter(meter.activePhysicalMeter().orElseThrow())
        .readoutTime(start.plusSeconds(2))
        .quantity("Energy")
        .build(),
      meter
    );
    //On schedule
    measurements.createOrUpdate(
      Measurement.builder()
        .unit("MWh")
        .value(2.0)
        .physicalMeter(meter.activePhysicalMeter().orElseThrow())
        .readoutTime(start.plusDays(1L))
        .quantity("Energy")
        .build(),
      meter
    );

    assertThat(measurementJpaRepository.findAll())
      .extracting(e -> e.expectedTime != null ? e.expectedTime.toInstant() : null)
      .containsExactly(null, null, null);
  }

  @Test
  @Transactional
  public void valuesExpectedTimeIsSetCorrectlyWhenMeterIsActiveAndInactive() {
    ZonedDateTime start = ZonedDateTime.parse("2007-12-03T00:00:00+03:00");
    var meter = given(logicalMeter().utcOffset("+3")
      .physicalMeter(physicalMeter()
        .activePeriod(PeriodRange.builder().start(PeriodBound.inclusiveOf(start.plusHours(1)))
          .stop(PeriodBound.exclusiveOf(start.plusDays(2))).build())
        .readIntervalMinutes(1440)
        .build()));
    //On schedule, not on active period
    measurements.createOrUpdate(
      Measurement.builder()
        .unit("MWh")
        .value(2.0)
        .physicalMeter(meter.physicalMeters.get(0))
        .readoutTime(start)
        .quantity("Energy")
        .build(),
      meter
    );
    //Two seconds off schedule
    measurements.createOrUpdate(
      Measurement.builder()
        .unit("MWh")
        .value(2.0)
        .physicalMeter(meter.physicalMeters.get(0))
        .readoutTime(start.plusSeconds(2))
        .quantity("Energy")
        .build(),
      meter
    );
    //On schedule
    measurements.createOrUpdate(
      Measurement.builder()
        .unit("MWh")
        .value(2.0)
        .physicalMeter(meter.physicalMeters.get(0))
        .readoutTime(start.plusDays(1L))
        .quantity("Energy")
        .build(),
      meter
    );

    //On schedule, outside period
    measurements.createOrUpdate(
      Measurement.builder()
        .unit("MWh")
        .value(2.0)
        .physicalMeter(meter.physicalMeters.get(0))
        .readoutTime(start.plusDays(2L))
        .quantity("Energy")
        .build(),
      meter
    );

    assertThat(measurementJpaRepository.findAll())
      .extracting(e -> e.expectedTime != null ? e.expectedTime.toInstant() : null)
      .containsExactly(null, null, start.plusDays(1L).toInstant(), null);
  }

  private List<QuantityParameter> quantityParametersOf(DisplayQuantity quantity) {
    return List.of(new QuantityParameter(
      quantity.quantity.name,
      quantity.unit,
      quantity.displayMode
    ));
  }

  private MeasurementKey getKey(LogicalMeter meter, Quantity quantity) {
    assertThat(meter.physicalMeters.size()).isEqualTo(1);
    return getKey(meter, meter.physicalMeters.get(0), quantity);
  }

  private MeasurementKey getKey(
    LogicalMeter meter,
    PhysicalMeter physicalMeter,
    Quantity quantity
  ) {
    return new MeasurementKey(
      meter.id,
      meter.utcOffset,
      physicalMeter.address,
      physicalMeter.activePeriod,
      quantity.name,
      meter.externalId,
      meter.location.getCity(),
      meter.location.getAddress(),
      meter.getMedium().name
    );
  }

  private RequestParameters idParametersOf(LogicalMeter... meters) {
    return RequestParametersAdapter.of(Map.of(
      LOGICAL_METER_ID.toString(),
      Arrays.stream(meters).map(meter -> meter.id.toString()).collect(toList())
    ));
  }
}
