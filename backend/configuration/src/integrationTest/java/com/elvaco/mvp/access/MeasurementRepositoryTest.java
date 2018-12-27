package com.elvaco.mvp.access;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementParameter;
import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.core.exception.UnitConversionError;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.Test;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.offset;

public class MeasurementRepositoryTest extends IntegrationTest {

  private static final OffsetDateTime START_TIME = OffsetDateTime.parse(
    "2018-01-01T00:00:00+00:00");

  @Test
  public void correctScaleIsReturned() {
    var meter = newPhysicalMeter();
    newMeasurement(meter, START_TIME, 2.0, "kW");

    List<MeasurementValue> results = measurements.findAverageForPeriod(
      new MeasurementParameter(
        List.of(meter.id),
        Quantity.POWER,
        START_TIME.toZonedDateTime(),
        START_TIME.plusHours(1).toZonedDateTime(),
        TemporalResolution.hour
      ));

    assertThat(results.get(0).value).isCloseTo(2000.0, offset(0.1));
  }

  @Test
  public void allValuesHaveSameScale() {
    var meter = newPhysicalMeter();
    var oneHourLater = START_TIME.plusHours(1);

    newMeasurement(meter, START_TIME, 2.0, "W");
    newMeasurement(meter, oneHourLater, 0.002, "kW");

    List<MeasurementValue> results = measurements.findAverageForPeriod(
      new MeasurementParameter(
        List.of(meter.id),
        Quantity.POWER,
        START_TIME.toZonedDateTime(),
        oneHourLater.toZonedDateTime(),
        TemporalResolution.hour
      ));

    assertThat(results).hasSize(2);
    assertThat(results.get(0).value).isCloseTo(results.get(1).value, offset(0.01));
  }

  @Test
  public void valuesAreScaledAccordingToSpecifiedUnit() {
    var meter = newPhysicalMeter();
    newMeasurement(meter, START_TIME, 2.0, "kW");

    List<MeasurementValue> results = measurements.findAverageForPeriod(
      new MeasurementParameter(
        List.of(meter.id),
        Quantity.POWER,
        START_TIME.toZonedDateTime(),
        START_TIME.plusSeconds(1).toZonedDateTime(),
        TemporalResolution.hour
      ));

    assertThat(results.get(0).value).isCloseTo(2000.0, offset(0.01));
  }

  @Test
  public void mixedDimensionsAreRejected() {
    var meter = newPhysicalMeter();
    newMeasurement(meter, START_TIME, 1.0, "m³", "Volume");
    assertThatThrownBy(() ->
      newMeasurement(meter, START_TIME.plusMinutes(1), 2.0, "m³/s", "Volume")
    ).isInstanceOf(UnitConversionError.class);
  }

  private void newMeasurement(
    PhysicalMeter meter,
    OffsetDateTime when,
    double value,
    String unit,
    String quantity
  ) {
    measurements.save(
      Measurement.builder()
        .unit(unit)
        .value(value)
        .physicalMeter(meter)
        .created(when.toZonedDateTime())
        .quantity(quantity)
        .build()
    );
  }

  private void newMeasurement(
    PhysicalMeter meter,
    OffsetDateTime when,
    double value,
    String unit
  ) {
    newMeasurement(meter, when, value, unit, "Power");
  }

  private PhysicalMeter newPhysicalMeter() {
    UUID uuid = UUID.randomUUID();
    return physicalMeters.save(PhysicalMeter.builder()
      .id(uuid)
      .organisationId(context().organisationId())
      .externalId(uuid.toString())
      .address("")
      .medium("")
      .manufacturer("")
      .logicalMeterId(null)
      .readIntervalMinutes(60)
      .revision(1)
      .mbusDeviceType(1)
      .statuses(emptySet())
      .alarms(emptySet())
      .build());
  }
}
