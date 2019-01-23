package com.elvaco.mvp.access;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementKey;
import com.elvaco.mvp.core.domainmodels.MeasurementParameter;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.core.exception.UnitConversionError;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MeasurementRepositoryTest extends IntegrationTest {

  private static final OffsetDateTime START_TIME = OffsetDateTime.parse(
    "2018-01-01T00:00:00+00:00");

  @Test
  public void getSeriesForConsumption_PutConsumptionOnStartOfInterval() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());
    given(series(meter, Quantity.VOLUME, start, 3.0, 6.0, 12.0, 24.0));

    Map<MeasurementKey, List<MeasurementValue>> result =
      measurements.findSeriesForPeriod(
        new MeasurementParameter(
          List.of(meter.id),
          List.of(Quantity.VOLUME),
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
    given(series(meter, Quantity.VOLUME, start, 3.0, 6.0));
    given(series(meter, Quantity.VOLUME, start.plusHours(4), 48.0, 96.0));

    Map<MeasurementKey, List<MeasurementValue>> result =
      measurements.findSeriesForPeriod(
        new MeasurementParameter(
          List.of(meter.id),
          List.of(Quantity.VOLUME),
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
    given(series(meter, Quantity.VOLUME, start.minusHours(1), 1.0));
    // missing measurement for 'start'
    given(series(meter, Quantity.VOLUME, start.plusHours(1), 6.0, 12.0));
    // missing measurement after interval

    Map<MeasurementKey, List<MeasurementValue>> result =
      measurements.findSeriesForPeriod(
        new MeasurementParameter(
          List.of(meter.id),
          List.of(Quantity.VOLUME),
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
    given(series(meter, Quantity.VOLUME, start, 3.0, 6.0));
    // missing measurement at end of interval

    Map<MeasurementKey, List<MeasurementValue>> result =
      measurements.findSeriesForPeriod(
        new MeasurementParameter(
          List.of(meter.id),
          List.of(Quantity.VOLUME),
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
    given(series(meter, Quantity.VOLUME, start, 3.0, 6.0));
    // missing measurement at end of interval
    given(series(meter, Quantity.VOLUME, start.plusHours(4), 24.0));

    Map<MeasurementKey, List<MeasurementValue>> result =
      measurements.findSeriesForPeriod(
        new MeasurementParameter(
          List.of(meter.id),
          List.of(Quantity.VOLUME),
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
    given(series(meter, Quantity.VOLUME, start, 3.0, 6.0, 12.0));
    // missing measurement at START_TIME.plusHours(3)

    Map<MeasurementKey, List<MeasurementValue>> result =
      measurements.findSeriesForPeriod(
        new MeasurementParameter(
          List.of(meter.id),
          List.of(Quantity.VOLUME),
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
    given(series(meter, Quantity.POWER, start, 1.0, 2.0));

    Map<MeasurementKey, List<MeasurementValue>> result = measurements.findSeriesForPeriod(
      new MeasurementParameter(
        List.of(meter.id),
        List.of(Quantity.POWER),
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
    given(series(meter, Quantity.POWER, start, Duration.ofMinutes(30), 1.0, 1.5, 2.0));

    Map<MeasurementKey, List<MeasurementValue>> result = measurements.findSeriesForPeriod(
      new MeasurementParameter(
        List.of(meter.id),
        List.of(Quantity.POWER),
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

    given(series(meterOne, Quantity.POWER, start, 1.0, 2.0, 3.0));
    given(series(meterTwo, Quantity.POWER, start, 10.0, 20.0, 30.0));

    Map<MeasurementKey, List<MeasurementValue>> result = measurements.findSeriesForPeriod(
      new MeasurementParameter(
        List.of(meterOne.id, meterTwo.id),
        List.of(Quantity.POWER),
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

    given(series(meterOne, Quantity.VOLUME, start, 1.0, 2.0, 3.0));
    given(series(meterTwo, Quantity.VOLUME, start, 10.0, 20.0, 30.0));

    Map<MeasurementKey, List<MeasurementValue>> result = measurements.findSeriesForPeriod(
      new MeasurementParameter(
        List.of(meterOne.id, meterTwo.id),
        List.of(Quantity.VOLUME),
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

    given(series(physicalMeterOne, Quantity.VOLUME_FLOW, start.minusDays(2), interval, 2.0, 4.0));
    given(series(physicalMeterTwo, Quantity.VOLUME_FLOW, start, interval, 6.0, 12.0));

    Map<MeasurementKey, List<MeasurementValue>> result = measurements.findSeriesForPeriod(
      new MeasurementParameter(
        List.of(logicalMeter.id),
        List.of(Quantity.VOLUME_FLOW),
        start.minusDays(2),
        start.plusDays(1),
        TemporalResolution.day
      ));

    assertThat(result.get(getKey(logicalMeter, Quantity.VOLUME_FLOW)))
      .extracting(l -> l.value)
      .containsExactly(2.0, 4.0, 6.0, 12.0);
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

    given(series(physicalMeterOne, Quantity.VOLUME, start.minusDays(2), interval, 2.0, 4.0));
    given(series(physicalMeterTwo, Quantity.VOLUME, start, interval, 6.0, 12.0));

    Map<MeasurementKey, List<MeasurementValue>> result = measurements.findSeriesForPeriod(
      new MeasurementParameter(
        List.of(logicalMeter.id),
        List.of(Quantity.VOLUME),
        start.minusDays(2),
        start,
        TemporalResolution.day
      ));

    assertThat(result.get(getKey(logicalMeter, Quantity.VOLUME)))
      .extracting(l -> l.value)
      .containsExactly(2.0, 2.0, 6.0);
  }

  @Test
  @Transactional
  public void valuesAreSavedWithStorageUnit() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());

    measurements.createOrUpdate(Measurement.builder()
      .unit("MWh")
      .value(2.0)
      .physicalMeter(meter.activePhysicalMeter().orElseThrow())
      .created(start)
      .quantity("Energy")
      .build());

    assertThat(measurementJpaRepository.findAll()).extracting(e -> e.value).containsOnly(2000.0);
  }

  @Test
  public void correctScaleIsReturned() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());

    newMeasurement(meter.activePhysicalMeter().get(), start, 2.0, "kW", "Power");

    Map<String, List<MeasurementValue>> results = measurements.findAverageForPeriod(
      new MeasurementParameter(
        List.of(meter.id),
        List.of(Quantity.POWER),
        start,
        start.plusSeconds(1),
        TemporalResolution.hour
      ));

    assertThat(results.get(Quantity.POWER.name)).extracting(v -> v.value).containsOnly(2000.0);
  }

  @Test
  public void allValuesHaveSameScale() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());

    var physicalMeter = meter.activePhysicalMeter().get();

    newMeasurement(physicalMeter, start, 2.0, "W", "Power");
    newMeasurement(physicalMeter, start.plusHours(1), 0.002, "kW", "Power");

    Map<String, List<MeasurementValue>> results = measurements.findAverageForPeriod(
      new MeasurementParameter(
        List.of(meter.id),
        List.of(Quantity.POWER),
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

    newMeasurement(physicalMeter, start, 1.0, "m³", "Volume");

    assertThatThrownBy(() ->
      newMeasurement(physicalMeter, start.plusMinutes(1), 2.0, "m³/s", "Volume")
    ).isInstanceOf(UnitConversionError.class);
  }

  private MeasurementKey getKey(LogicalMeter meter, Quantity quantity) {
    return new MeasurementKey(meter.id, quantity.name);
  }

  private void newMeasurement(
    PhysicalMeter meter,
    ZonedDateTime when,
    double value,
    String unit,
    String quantity
  ) {
    measurements.save(
      Measurement.builder()
        .unit(unit)
        .value(value)
        .physicalMeter(meter)
        .created(when)
        .quantity(quantity)
        .build()
    );
  }
}
