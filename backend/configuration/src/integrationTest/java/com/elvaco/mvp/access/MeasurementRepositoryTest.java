package com.elvaco.mvp.access;

import java.time.Duration;
import java.time.ZonedDateTime;
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
    given(series(meter, Quantity.VOLUME, start, 3.0, 6.0, 12.0, 24.0));

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
    given(series(meter, Quantity.VOLUME, start, 3.0, 6.0));
    given(series(meter, Quantity.VOLUME, start.plusHours(4), 48.0, 96.0));

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
    given(series(meter, Quantity.VOLUME, start.minusHours(1), 1.0));
    // missing measurement for 'start'
    given(series(meter, Quantity.VOLUME, start.plusHours(1), 6.0, 12.0));
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
    given(series(meter, Quantity.VOLUME, start, 3.0, 6.0));
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
    given(series(meter, Quantity.VOLUME, start, 3.0, 6.0));
    // missing measurement at end of interval
    given(series(meter, Quantity.VOLUME, start.plusHours(4), 24.0));

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
    given(series(meter, Quantity.VOLUME, start, 3.0, 6.0, 12.0));
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
    given(series(meter, Quantity.POWER, start, 1.0, 2.0));

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
    given(series(meter, Quantity.POWER, start, Duration.ofMinutes(30), 1.0, 1.5, 2.0));

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

    given(series(meterOne, Quantity.POWER, start, 1.0, 2.0, 3.0));
    given(series(meterTwo, Quantity.POWER, start, 10.0, 20.0, 30.0));

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

    given(series(meterOne, Quantity.VOLUME, start, 1.0, 2.0, 3.0));
    given(series(meterTwo, Quantity.VOLUME, start, 10.0, 20.0, 30.0));

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

    given(series(
      meterOne,
      Quantity.VOLUME,
      start,
      DoubleStream.iterate(1, d -> d + 1).limit(25).toArray()
    ));

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

    given(series(physicalMeterOne, Quantity.POWER, start.minusDays(2), interval, 2.0, 4.0));
    given(series(physicalMeterTwo, Quantity.POWER, start, interval, 6.0, 12.0));

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
        .created(start)
        .value(2.0)
        .quantity(Quantity.POWER.name)
        .unit(WATT)
        .physicalMeter(physicalMeter)
        .build()
    );
    measurements.save(
      Measurement.builder()
        .created(start.plusHours(1))
        .value(0.002)
        .quantity(Quantity.POWER.name)
        .unit(KILOWATT)
        .physicalMeter(physicalMeter)
        .build()
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
