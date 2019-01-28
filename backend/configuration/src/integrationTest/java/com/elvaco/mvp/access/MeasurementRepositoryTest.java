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
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.QuantityPresentationInformation;
import com.elvaco.mvp.core.domainmodels.SeriesDisplayMode;
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

    assertThat(result.get(getKey(logicalMeter, physicalMeterOne.address, Quantity.VOLUME_FLOW)))
      .extracting(l -> l.value)
      .containsExactly(2.0, 4.0);

    assertThat(result.get(getKey(logicalMeter, physicalMeterTwo.address, Quantity.VOLUME_FLOW)))
      .extracting(l -> l.value)
      .containsExactly(6.0, 12.0);
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

    assertThat(result.get(getKey(logicalMeter, physicalMeterOne.address, Quantity.VOLUME)))
      .extracting(l -> l.value)
      .containsExactly(2.0, null);

    assertThat(result.get(getKey(logicalMeter, physicalMeterTwo.address, Quantity.VOLUME)))
      .extracting(l -> l.value)
      .containsExactly(6.0);
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
  @Transactional
  public void valuesAreSavedWithStorageUnitUsingSave() {
    ZonedDateTime start = context().now();
    var meter = given(logicalMeter());

    measurements.save(Measurement.builder()
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

    given(series(meter.activePhysicalMeter().orElseThrow(), Quantity.POWER, start, 2000.0));

    Quantity presentationQuantity = Quantity.of(Quantity.POWER.name).complementedBy(
      new QuantityPresentationInformation("kW", SeriesDisplayMode.READOUT), null);

    Map<String, List<MeasurementValue>> results = measurements.findAverageForPeriod(
      new MeasurementParameter(
        List.of(meter.id),
        List.of(presentationQuantity),
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

    var physicalMeter = meter.activePhysicalMeter().orElseThrow();

    var presentationInformation = Quantity.POWER.getPresentationInformation();
    var quantity1 = Quantity.of(Quantity.POWER.name).complementedBy(presentationInformation, "W");
    var quantity2 = Quantity.of(Quantity.POWER.name).complementedBy(presentationInformation, "kW");

    given(series(physicalMeter, quantity1, start, 2.0));
    given(series(physicalMeter, quantity2, start.plusHours(1), 0.002));

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

    given(series(physicalMeter, Quantity.VOLUME, start, 2.0));

    assertThatThrownBy(() -> measurements.save(Measurement.builder()
      .physicalMeter(physicalMeter)
      .created(start.plusMinutes(1))
      .value(2.0)
      .unit("m³/s")
      .quantity("Volume")
      .build())
    ).isInstanceOf(UnitConversionError.class);
  }

  private MeasurementKey getKey(
    LogicalMeter meter,
    String physicalMeterAddress,
    Quantity quantity
  ) {
    return new MeasurementKey(meter.id, physicalMeterAddress, quantity.name);
  }

  private MeasurementKey getKey(LogicalMeter meter, Quantity quantity) {
    assertThat(meter.physicalMeters.size()).isEqualTo(1);
    return new MeasurementKey(meter.id, meter.physicalMeters.get(0).address, quantity.name);
  }
}
