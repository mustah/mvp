package com.elvaco.mvp.database;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.MeasurementUnit;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeasurementValueProjection;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
public class MeasurementJpaRepositoryTest extends IntegrationTest {

  private static final OffsetDateTime START_TIME =
    OffsetDateTime.parse("2018-01-01T00:00:00+00:00");

  @Autowired
  MeasurementJpaRepository measurementJpaRepository;
  @Autowired
  PhysicalMeterJpaRepository physicalMeterJpaRepository;
  @Autowired
  private OrganisationJpaRepository organisationJpaRepository;

  @Before
  public void setUp() {
    Assume.assumeTrue(isPostgresDialect());
  }

  @After
  public void tearDown() {
    try {
      physicalMeterJpaRepository.deleteAll();
      measurementJpaRepository.deleteAll();
    } catch (org.hibernate.AssertionFailure ex) {
      //mixedDimensionsAreRejected will rollback the transaction, trying to delete stuff
      // after that will cause org.hibernate.AssertionFailure caught here
    }
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
        "default",
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
        singletonList(meter.id), "hour", "Energy", "W", "default", START_TIME, oneHourLater);

    assertThat(results).hasSize(2);
    assertThat(results).allMatch(v -> v.getDoubleValue() == 2.0);
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
        "default",
        START_TIME,
        START_TIME.plus(Duration.ofSeconds(1))
      );

    assertThat(results).hasSize(1);
    assertThat(results.get(0).getDoubleValue()).isEqualTo(0.002);
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
        "default",
        START_TIME,
        dayTwo
      );

    assertThat(results).hasSize(2);
    assertThat(results.get(0).getDoubleValue()).isEqualTo(2.0);
    assertThat(results.get(1).getDoubleValue()).isEqualTo(4.0);
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
        "default",
        START_TIME,
        nextMonth
      );

    assertThat(results).hasSize(2);
    assertThat(results.get(0).getDoubleValue()).isEqualTo(2.0);
    assertThat(results.get(1).getDoubleValue()).isEqualTo(4.0);
  }

  @Test
  public void missingIntervalValuesAreRepresentedAsNull() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME, 2.0, "W");
    OffsetDateTime twoHoursLater = START_TIME.plus(Duration.ofHours(2));
    newMeasurement(meter, twoHoursLater, 2.0, "W");

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        singletonList(meter.id), "hour", "Energy", "W", "default", START_TIME, twoHoursLater);

    assertThat(results).hasSize(3);
    assertThat(results.get(0).getValue()).isEqualTo("2 W");
    assertThat(results.get(1).getValue()).isEqualTo(null);
    assertThat(results.get(2).getValue()).isEqualTo("2 W");
  }

  @Test
  public void averageValueIsCorrectForPeriod() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME, 2.0, "W");
    newMeasurement(meter, START_TIME.plusMinutes(1), 100.0, "W");

    OffsetDateTime oneHourLater = START_TIME.plus(Duration.ofHours(1));
    newMeasurement(meter, oneHourLater, 1.0, "W");
    newMeasurement(meter, oneHourLater.plusMinutes(1), 9.0, "W");

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        singletonList(meter.id),
        "hour",
        "Energy",
        "W",
        "default",
        START_TIME,
        oneHourLater.plusMinutes(1)
      );

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
        "default",
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
        "default",
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
        "default",
        START_TIME,
        START_TIME.plus(Duration.ofSeconds(1))
      );

    List<MeasurementValueProjection> resultsWithDayResolution = measurementJpaRepository
      .getAverageForPeriod(
        singletonList(meter.id),
        "day",
        "Energy",
        "W",
        "default",
        START_TIME,
        START_TIME.plus(Duration.ofSeconds(1))
      );

    List<MeasurementValueProjection> resultsWithMonthResolution = measurementJpaRepository
      .getAverageForPeriod(
        singletonList(meter.id),
        "month",
        "Energy",
        "W",
        "default",
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
        "default",
        START_TIME,
        START_TIME.plus(Duration.ofSeconds(1))
      );

    assertThat(results).hasSize(1);
    assertThat(results.get(0).getUnit()).isEqualTo("W");
  }

  @Test
  public void findLatestReadoutWithDisplayInformation() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME, 1.0, "kWh", "Energy");
    newMeasurement(meter, START_TIME.plusHours(1), 2.0, "kWh", "Energy");
    newMeasurement(meter, START_TIME.plusHours(2), 3.0, "kWh", "Energy");

    MeasurementEntity latestEnergy = measurementJpaRepository
      .findLatestReadout(meter.id, "Energy", "kWh").get();

    assertThat(latestEnergy.created.toInstant()).isEqualTo(START_TIME.plusHours(2).toInstant());
    assertThat(latestEnergy.value.getValue()).isEqualTo(3.0);
  }

  @Test
  @Ignore(value = "The postgres-unit system does not work so well with JPA")
  public void findLatestReadoutWithUnitAdjusted() {

    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME, 1.0, "kWh", "Energy");

    MeasurementEntity latestEnergy = measurementJpaRepository
      .findLatestReadout(meter.id, "Energy", "MWh").get();
    assertThat(latestEnergy.created.toInstant()).isEqualTo(START_TIME.toInstant());
    assertThat(latestEnergy.value.getValue()).isEqualTo(0.001);
    assertThat(latestEnergy.value.getUnit()).isEqualTo("MWh");
  }

  @Test
  public void findLatestReadout_None() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();

    Optional<MeasurementEntity> latestEnergy = measurementJpaRepository
      .findLatestReadout(meter.id, "Energy", "MWh");

    assertThat(latestEnergy.isPresent()).isFalse();
  }

  @Test
  public void findLatestReadout_MultipleQuantities() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME, 1.0, "kWh", "Energy");
    newMeasurement(meter, START_TIME.plusHours(1), 2.0, "kWh", "Energy");

    newMeasurement(meter, START_TIME, 10.0, "m³", "Volume");
    newMeasurement(meter, START_TIME.plusHours(1), 20.0, "m³", "Volume");

    MeasurementEntity latestEnergy = measurementJpaRepository
      .findLatestReadout(meter.id, "Energy", "kWh").get();

    assertThat(latestEnergy.created.toInstant()).isEqualTo(START_TIME.plusHours(1).toInstant());
    assertThat(latestEnergy.value.getValue()).isEqualTo(2.0);
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
      START_TIME.plusMinutes(1),
      "minute"
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
      START_TIME.plusMinutes(1),
      "minute"
    );

    assertThat(result).hasSize(3);
    assertThat(result.get(0).getValue()).isNull();
    assertThat(result.get(1).getDoubleValue()).isEqualTo(0.001);
    assertThat(result.get(2).getDoubleValue()).isEqualTo(0.001);
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
      START_TIME.plus(Duration.ofSeconds(1)),
      "minute"
    );
    assertThat(result).isEmpty();
  }

  @Test
  public void mixedDimensionsAreRejected() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME, 1.0, "m³", "Volume");

    assertThatThrownBy(() -> {
        newMeasurement(meter, START_TIME.plusMinutes(1), 2.0, "m³/s", "Volume");
      }
    ).isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  public void averageForConsumptionSeries() {
    PhysicalMeterEntity firstMeter = newPhysicalMeterEntity();

    newMeasurement(firstMeter, START_TIME.plusHours(1), 0.0, "kWh", "Energy");
    newMeasurement(firstMeter, START_TIME.plusHours(2), 1.0, "kWh", "Energy");
    newMeasurement(firstMeter, START_TIME.plusHours(3), 5.0, "kWh", "Energy");

    PhysicalMeterEntity secondMeter = newPhysicalMeterEntity();
    newMeasurement(secondMeter, START_TIME.plusHours(1), 1.0, "kWh", "Energy");
    newMeasurement(secondMeter, START_TIME.plusHours(2), 2.0, "kWh", "Energy");
    newMeasurement(secondMeter, START_TIME.plusHours(3), 3.0, "kWh", "Energy");

    List<MeasurementValueProjection> result = measurementJpaRepository.getAverageForPeriod(
      Arrays.asList(firstMeter.id, secondMeter.id),
      "hour",
      "Energy",
      "kWh",
      "consumption",
      START_TIME.plusHours(1),
      START_TIME.plusHours(3)
    );

    assertThat(result).hasSize(3);
    assertThat(result.get(0).getValue()).isNull();
    assertThat(result.get(1).getDoubleValue()).isEqualTo(1.0); // ((1.0 - 0.0) + (2.0 - 1.0)) / 2
    assertThat(result.get(2).getDoubleValue()).isEqualTo(2.5); // ((5.0 - 1.0) + (3.0 - 2.0)) / 2
  }

  @Test
  public void averageForConsumptionSeries_firstIsNotNullWhenPreviousValueExistOutsidePeriod() {
    PhysicalMeterEntity firstMeter = newPhysicalMeterEntity();

    newMeasurement(firstMeter, START_TIME.plusHours(1), 0.0, "kWh", "Energy");
    newMeasurement(firstMeter, START_TIME.plusHours(2), 1.0, "kWh", "Energy");
    newMeasurement(firstMeter, START_TIME.plusHours(3), 5.0, "kWh", "Energy");

    PhysicalMeterEntity secondMeter = newPhysicalMeterEntity();
    newMeasurement(secondMeter, START_TIME.plusHours(1), 1.0, "kWh", "Energy");
    newMeasurement(secondMeter, START_TIME.plusHours(2), 2.0, "kWh", "Energy");
    newMeasurement(secondMeter, START_TIME.plusHours(3), 3.0, "kWh", "Energy");

    List<MeasurementValueProjection> result = measurementJpaRepository.getAverageForPeriod(
      Arrays.asList(firstMeter.id, secondMeter.id),
      "hour",
      "Energy",
      "kWh",
      "consumption",
      START_TIME.plusHours(2),
      START_TIME.plusHours(3)
    );

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getDoubleValue()).isEqualTo(1.0); // ((1.0 - 0.0) + (2.0 - 1.0)) / 2
    assertThat(result.get(1).getDoubleValue()).isEqualTo(2.5); // ((5.0 - 1.0) + (3.0 - 2.0)) / 2
  }

  @Test
  public void averageForMissingMeasurements() {
    PhysicalMeterEntity firstMeter = newPhysicalMeterEntity();

    newMeasurement(firstMeter, START_TIME.plusHours(2), 1.0, "kWh", "Energy");
    newMeasurement(firstMeter, START_TIME.plusHours(3), 5.0, "kWh", "Energy");

    PhysicalMeterEntity secondMeter = newPhysicalMeterEntity();
    newMeasurement(secondMeter, START_TIME.plusHours(1), 7.0, "kWh", "Energy");
    newMeasurement(secondMeter, START_TIME.plusHours(2), 2.0, "kWh", "Energy");
    newMeasurement(secondMeter, START_TIME.plusHours(3), 3.0, "kWh", "Energy");

    List<MeasurementValueProjection> result = measurementJpaRepository.getAverageForPeriod(
      Arrays.asList(firstMeter.id, secondMeter.id),
      "hour",
      "Energy",
      "kWh",
      "default",
      START_TIME.plusHours(1),
      START_TIME.plusHours(3)
    );

    assertThat(result).hasSize(3);
    // note: we average only over present measurement count for an interval
    assertThat(result.get(0).getDoubleValue()).isEqualTo(7.0);
    assertThat(result.get(1).getDoubleValue()).isEqualTo(1.5);
    assertThat(result.get(2).getDoubleValue()).isEqualTo(4.0);
  }

  @Test
  public void averageForConsumptionSeries_missingMeasurementsForOneMeter() {
    PhysicalMeterEntity firstMeter = newPhysicalMeterEntity();

    newMeasurement(firstMeter, START_TIME.plusHours(2), 1.0, "kWh", "Energy");
    newMeasurement(firstMeter, START_TIME.plusHours(3), 5.0, "kWh", "Energy");

    PhysicalMeterEntity secondMeter = newPhysicalMeterEntity();
    newMeasurement(secondMeter, START_TIME.plusHours(1), 0.0, "kWh", "Energy");
    newMeasurement(secondMeter, START_TIME.plusHours(2), 2.0, "kWh", "Energy");
    newMeasurement(secondMeter, START_TIME.plusHours(3), 3.0, "kWh", "Energy");

    List<MeasurementValueProjection> result = measurementJpaRepository.getAverageForPeriod(
      Arrays.asList(firstMeter.id, secondMeter.id),
      "hour",
      "Energy",
      "kWh",
      "consumption",
      START_TIME.plusHours(2),
      START_TIME.plusHours(3)
    );

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getDoubleValue()).isEqualTo(2.0); // (2.0 - 0.0) / 1
    assertThat(result.get(1).getDoubleValue()).isEqualTo(2.5); // ((5.0 - 1.0) + (3.0 - 2.0)) / 2
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
      0,
      emptySet()
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
