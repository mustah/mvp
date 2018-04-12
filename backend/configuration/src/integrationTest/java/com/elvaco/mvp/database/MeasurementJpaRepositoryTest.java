package com.elvaco.mvp.database;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.MeasurementUnit;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeasurementValueProjection;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class MeasurementJpaRepositoryTest extends IntegrationTest {

  private static final OffsetDateTime START_TIME =
    OffsetDateTime.parse("2018-01-01T00:00:00+00:00");

  @Autowired
  MeasurementJpaRepository measurementJpaRepository;
  @Autowired
  PhysicalMeterJpaRepository physicalMeterJpaRepository;

  @Before
  public void setUp() {
    Assume.assumeTrue(isPostgresDialect());
  }

  @After
  public void tearDown() {
    physicalMeterJpaRepository.deleteAll();
    measurementJpaRepository.deleteAll();
  }

  @Test
  public void correctNumberOfValuesAreReturnedRelativeToStart() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    generateSeries(meter, 10, START_TIME, Duration.ofHours(1), 2.0, "W");
    OffsetDateTime fiveHoursIn = START_TIME.plus(Duration.ofHours(5));
    OffsetDateTime lastHourWithMeasurements = START_TIME.plus(Duration.ofHours(9));

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        singletonList(meter.id),
        "hour",
        "Energy",
        "W",
        fiveHoursIn,
        lastHourWithMeasurements
      );

    assertThat(results).hasSize(5);
  }

  @Test
  public void allValuesHaveSameScale() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME, 2.0, "W");
    OffsetDateTime oneHourLater = START_TIME.plus(Duration.ofHours(1));
    newMeasurement(meter, oneHourLater, 0.002, "kW");

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        singletonList(meter.id), "hour", "Energy", "W", START_TIME, oneHourLater);

    assertThat(results).hasSize(2);
    assertThat(results).allMatch(v -> v.getValueValue() == 2.0);
  }

  @Test
  public void valuesAreScaledAccordingToSpecifiedUnit() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME, 2.0, "W");

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        singletonList(meter.id),
        "hour",
        "Energy",
        "kW",
        START_TIME,
        START_TIME.plus(Duration.ofSeconds(1))
      );

    assertThat(results).hasSize(1);
    assertThat(results.get(0).getValueValue()).isEqualTo(0.002);
  }

  @Test
  public void resultsCanBeFetchedWithDayResolution() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME, 2.0, "W");
    OffsetDateTime dayTwo = START_TIME.plus(Period.ofDays(1));
    newMeasurement(meter, dayTwo, 4.0, "W");

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        singletonList(meter.id),
        "day",
        "Energy",
        "W",
        START_TIME,
        dayTwo
      );

    assertThat(results).hasSize(2);
    assertThat(results.get(0).getValueValue()).isEqualTo(2.0);
    assertThat(results.get(1).getValueValue()).isEqualTo(4.0);
  }

  @Test
  public void resultsCanBeFetchedWithMonthResolution() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME, 2.0, "W");
    OffsetDateTime nextMonth = START_TIME.plus(Period.ofMonths(1));
    newMeasurement(meter, nextMonth, 4.0, "W");

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        singletonList(meter.id),
        "month",
        "Energy",
        "W",
        START_TIME,
        nextMonth
      );

    assertThat(results).hasSize(2);
    assertThat(results.get(0).getValueValue()).isEqualTo(2.0);
    assertThat(results.get(1).getValueValue()).isEqualTo(4.0);
  }

  @Test
  public void missingIntervalValuesAreRepresentedAsNull() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME, 2.0, "W");
    OffsetDateTime twoHoursLater = START_TIME.plus(Duration.ofHours(2));
    newMeasurement(meter, twoHoursLater, 2.0, "W");

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        singletonList(meter.id), "hour", "Energy", "W", START_TIME, twoHoursLater);

    assertThat(results).hasSize(3);
    assertThat(results.get(0).getValue()).isEqualTo("2 W");
    assertThat(results.get(1).getValue()).isEqualTo(null);
    assertThat(results.get(2).getValue()).isEqualTo("2 W");
  }

  @Test
  public void averageValueIsCorrectForPeriod() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME, 2.0, "W");
    newMeasurement(meter, START_TIME, 100.0, "W");

    OffsetDateTime oneHourLater = START_TIME.plus(Duration.ofHours(1));
    newMeasurement(meter, oneHourLater, 1.0, "W");
    newMeasurement(meter, oneHourLater, 9.0, "W");

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        singletonList(meter.id), "hour", "Energy", "W", START_TIME, oneHourLater);

    assertThat(results).hasSize(2);
    assertThat(results.get(0).getValue()).isEqualTo("51 W");
    assertThat(results.get(1).getValue()).isEqualTo("5 W");
  }

  @Test
  public void unspecifiedMetersAreNotIncluded() {
    PhysicalMeterEntity firstMeter = newPhysicalMeterEntity();
    PhysicalMeterEntity secondMeter = newPhysicalMeterEntity();
    newMeasurement(firstMeter, START_TIME, 12, "W");
    newMeasurement(secondMeter, START_TIME, 99.8, "W");

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        singletonList(firstMeter.id),
        "hour",
        "Energy",
        "W",
        START_TIME,
        START_TIME.plus(Duration.ofSeconds(1))
      );

    assertThat(results).hasSize(1);
    assertThat(results.get(0).getValue()).isEqualTo("12 W");
  }

  @Test
  public void valuesAreFilteredByQuantity() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME, 2.0, "W", "A");
    newMeasurement(meter, START_TIME, 6.0, "W", "B");

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        singletonList(meter.id),
        "hour",
        "A",
        "W",
        START_TIME,
        START_TIME.plus(Duration.ofSeconds(1))
      );

    assertThat(results).hasSize(1);
    assertThat(results.get(0).getValue()).isEqualTo("2 W");
  }

  @Test
  public void timesAreCorrect() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME, 2.0, "W");

    List<MeasurementValueProjection> resultsWithHourResolution = measurementJpaRepository
      .getAverageForPeriod(
        singletonList(meter.id),
        "hour",
        "Energy",
        "W",
        START_TIME,
        START_TIME.plus(Duration.ofSeconds(1))
      );

    List<MeasurementValueProjection> resultsWithDayResolution = measurementJpaRepository
      .getAverageForPeriod(
        singletonList(meter.id),
        "day",
        "Energy",
        "W",
        START_TIME,
        START_TIME.plus(Duration.ofSeconds(1))
      );

    List<MeasurementValueProjection> resultsWithMonthResolution = measurementJpaRepository
      .getAverageForPeriod(
        singletonList(meter.id),
        "month",
        "Energy",
        "W",
        START_TIME,
        START_TIME.plus(Duration.ofSeconds(1))
      );

    assertThat(resultsWithHourResolution).hasSize(1);
    assertThat(resultsWithHourResolution.get(0)
      .getWhen()
      .toInstant()).isEqualTo(START_TIME.toInstant());

    assertThat(resultsWithDayResolution).hasSize(1);
    assertThat(resultsWithDayResolution.get(0)
      .getWhen()
      .toInstant()).isEqualTo(START_TIME.toInstant());

    assertThat(resultsWithMonthResolution).hasSize(1);
    assertThat(resultsWithMonthResolution.get(0)
      .getWhen()
      .toInstant()).isEqualTo(START_TIME.toInstant());

  }

  @Test
  public void correctScaleIsReturned() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME, 2.0, "W");

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        singletonList(meter.id),
        "hour",
        "Energy",
        "W",
        START_TIME,
        START_TIME.plus(Duration.ofSeconds(1))
      );

    assertThat(results).hasSize(1);
    assertThat(results.get(0).getUnit()).isEqualTo("W");
  }

  @Test
  public void findLatestMeasurementsForPhysicalMeter() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    PhysicalMeterEntity irrelevantMeter = newPhysicalMeterEntity();

    OffsetDateTime olderEnergyTime = OffsetDateTime.parse("2018-01-01T00:00:00+00:00");
    OffsetDateTime energyTime = OffsetDateTime.parse("2018-02-01T00:00:00+00:00");
    OffsetDateTime powerTime = OffsetDateTime.parse("2018-03-01T00:00:00+00:00");

    newMeasurement(meter, olderEnergyTime, 1.0, "kWh", "Energy");
    newMeasurement(meter, energyTime, 2.0, "kWh", "Energy");
    newMeasurement(meter, powerTime, 3.0, "W", "Power");
    newMeasurement(irrelevantMeter, energyTime, 4.0, "kWh", "Energy");
    newMeasurement(irrelevantMeter, powerTime, 5.0, "W", "Power");

    List<MeasurementEntity> measurements = measurementJpaRepository
      .findLatestForPhysicalMeter(meter.id);

    assertThat(measurements)
      .as("Measurements are filtered by physical meter")
      .hasSize(2)
      .allMatch(m -> m.physicalMeter.id.equals(meter.id))
      .noneMatch(m -> m.physicalMeter.id.equals(irrelevantMeter.id));

    Comparator<ZonedDateTime> timeZoneInsensitive = (o1, o2) -> o1.isEqual(o2) ? 0 : 1;
    assertThat(measurements.get(0).created)
      .usingComparator(timeZoneInsensitive)
      .isEqualTo(energyTime.toZonedDateTime());

    assertThat(measurements.get(1).created)
      .usingComparator(timeZoneInsensitive)
      .isEqualTo(powerTime.toZonedDateTime());
  }

  @Test
  public void seriesInDefaultMode() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();

    newMeasurement(meter, START_TIME, 1.0, "kWh", "Energy");
    newMeasurement(meter, START_TIME.plusMinutes(1), 2.0, "kWh", "Energy");

    List<MeasurementValueProjection> result = measurementJpaRepository.getSeriesForPeriod(
      meter.id,
      "Energy",
      "MWh",
      "default",
      START_TIME,
      START_TIME.plusMinutes(1)
    );

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getMeasurementUnit().get()).isEqualTo(new MeasurementUnit(
      "MWh",
      0.001
    ));
  }

  @Test
  public void seriesInConsumptionMode() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();

    newMeasurement(meter, START_TIME.minusMinutes(1), 0.0, "kWh", "Energy");
    newMeasurement(meter, START_TIME, 1.0, "kWh", "Energy");
    newMeasurement(meter, START_TIME.plusMinutes(1), 2.0, "kWh", "Energy");

    List<MeasurementValueProjection> result = measurementJpaRepository.getSeriesForPeriod(
      meter.id,
      "Energy",
      "MWh",
      "consumption",
      START_TIME.minusMinutes(1),
      START_TIME.plusMinutes(1)
    );

    assertThat(result).hasSize(3);
    assertThat(result.get(0).getValue()).isNull();
    assertThat(result.get(1).getValueValue()).isEqualTo(0.001);
    assertThat(result.get(2).getValueValue()).isEqualTo(0.001);
  }

  @Test
  public void emptySeries() {
    PhysicalMeterEntity physicalMeterEntity = newPhysicalMeterEntity();

    List<MeasurementValueProjection> result = measurementJpaRepository.getSeriesForPeriod(
      physicalMeterEntity.id,
      "Energy",
      "MW",
      "default",
      START_TIME,
      START_TIME.plus(Duration.ofSeconds(1))
    );
    assertThat(result).isEmpty();
  }

  private PhysicalMeterEntity newPhysicalMeterEntity() {
    UUID uuid = UUID.randomUUID();
    return physicalMeterJpaRepository.save(new PhysicalMeterEntity(
      uuid,
      context().organisationEntity,
      "",
      uuid.toString(),
      "",
      "",
      null,
      0
    ));
  }

  private void newMeasurement(
    PhysicalMeterEntity meter,
    OffsetDateTime when,
    double value,
    String unit,
    String quantity
  ) {
    measurementJpaRepository.save(new MeasurementEntity(
      when.toZonedDateTime(),
      quantity,
      value,
      unit,
      meter
    ));
  }

  private void newMeasurement(
    PhysicalMeterEntity meter,
    OffsetDateTime when,
    double value,
    String unit
  ) {
    newMeasurement(meter, when, value, unit, "Energy");
  }

  private void generateSeries(
    PhysicalMeterEntity meter,
    int count,
    OffsetDateTime startDate,
    Duration interval,
    double value,
    String unit
  ) {
    OffsetDateTime when = startDate;
    for (int i = 0; i < count; i++) {
      newMeasurement(meter, when, value, unit);
      when = when.plus(interval);
    }
  }
}
