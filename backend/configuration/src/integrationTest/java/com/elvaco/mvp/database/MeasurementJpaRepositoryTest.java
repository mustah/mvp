package com.elvaco.mvp.database;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.access.QuantityAccess;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.MeasurementUnit;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeasurementValueProjection;
import com.elvaco.mvp.database.repository.mappers.QuantityEntityMapper;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@Transactional
public class MeasurementJpaRepositoryTest extends IntegrationTest {

  private static final OffsetDateTime START_TIME = OffsetDateTime.parse(
    "2018-01-01T00:00:00+00:00");

  @Autowired
  private MeasurementJpaRepository measurementJpaRepository;

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
    var meter = newPhysicalMeterEntity();
    generateSeries(meter, 10, START_TIME, Duration.ofHours(1), 2.0, "W");
    var fiveHoursIn = START_TIME.plusHours(5);
    var lastHourWithMeasurements = START_TIME.plusHours(9);

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
    var meter = newPhysicalMeterEntity();
    var oneHourLater = START_TIME.plusHours(1);

    newMeasurement(meter, START_TIME, 2.0, "W");
    newMeasurement(meter, oneHourLater, 0.002, "kW");

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        singletonList(meter.id), "hour", "Energy", "W", START_TIME, oneHourLater);

    assertThat(results).hasSize(2);
    assertThat(results).allMatch(v -> v.getDoubleValue() == 2.0);
  }

  @Test
  public void valuesAreScaledAccordingToSpecifiedUnit() {
    var meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME, 2.0, "W");

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        singletonList(meter.id),
        "hour",
        "Energy",
        "kW",
        START_TIME,
        START_TIME.plusSeconds(1)
      );

    assertThat(results).extracting(MeasurementValueProjection::getDoubleValue)
      .containsExactly(0.002);
  }

  @Test
  public void resultsCanBeFetchedWithDayResolution() {
    var meter = newPhysicalMeterEntity();
    var dayTwo = START_TIME.plus(Period.ofDays(1));

    newMeasurement(meter, START_TIME, 2.0, "W");
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

    assertThat(results).extracting(MeasurementValueProjection::getDoubleValue).containsExactly(
      2.0,
      4.0
    );
  }

  @Test
  public void resultsCanBeFetchedWithMonthResolution() {
    var meter = newPhysicalMeterEntity();
    var nextMonth = START_TIME.plus(Period.ofMonths(1));

    newMeasurement(meter, START_TIME, 2.0, "W");
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

    assertThat(results).extracting(MeasurementValueProjection::getDoubleValue).containsExactly(
      2.0,
      4.0
    );
  }

  @Test
  public void missingIntervalValuesAreRepresentedAsNull() {
    var meter = newPhysicalMeterEntity();
    var twoHoursLater = START_TIME.plusHours(2);

    newMeasurement(meter, START_TIME, 2.0, "W");
    newMeasurement(meter, twoHoursLater, 3.0, "W");

    List<MeasurementValueProjection> results = measurementJpaRepository.getAverageForPeriod(
      singletonList(meter.id),
      "hour",
      "Energy",
      "W",
      START_TIME,
      twoHoursLater
    );

    assertThat(results).extracting(MeasurementValueProjection::getValue).containsExactly(
      "2 W",
      null,
      "3 W"
    );
  }

  @Test
  public void averageShouldOnlyIncludeValuesAtResolutionPoints() {
    var meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME, 2.0, "W");
    newMeasurement(meter, START_TIME.plusMinutes(1), 100.0, "W");

    var oneHourLater = START_TIME.plusHours(1);
    newMeasurement(meter, oneHourLater, 1.0, "W");
    newMeasurement(meter, oneHourLater.plusMinutes(1), 9.0, "W");

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        singletonList(meter.id),
        "hour",
        "Energy",
        "W",
        START_TIME,
        oneHourLater.plusMinutes(1)
      );

    assertThat(results).extracting(MeasurementValueProjection::getValue).containsExactly(
      "2 W",
      "1 W"
    );
  }

  @Test
  public void unspecifiedMetersAreNotIncluded() {
    var firstMeter = newPhysicalMeterEntity();
    var secondMeter = newPhysicalMeterEntity();
    newMeasurement(firstMeter, START_TIME, 12, "W");
    newMeasurement(secondMeter, START_TIME, 99.8, "W");

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        singletonList(firstMeter.id),
        "hour",
        "Energy",
        "W",
        START_TIME,
        START_TIME.plusSeconds(1)
      );

    assertThat(results).extracting(MeasurementValueProjection::getValue).containsExactly("12 W");
  }

  @Test
  public void valuesAreFilteredByQuantity() {
    var meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME, 2.0, "°C", Quantity.TEMPERATURE.name);
    newMeasurement(meter, START_TIME, 6.0, "°C", Quantity.RETURN_TEMPERATURE.name);

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        singletonList(meter.id),
        "hour",
        Quantity.TEMPERATURE.name,
        "°C",
        START_TIME,
        START_TIME.plusSeconds(1)
      );

    assertThat(results).extracting(MeasurementValueProjection::getValue).containsExactly("2 °C");
  }

  @Test
  public void timesAreCorrect() {
    var meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME, 2.0, "W");

    List<MeasurementValueProjection> resultsWithHourResolution = measurementJpaRepository
      .getAverageForPeriod(
        singletonList(meter.id),
        "hour",
        "Energy",
        "W",
        START_TIME,
        START_TIME.plusSeconds(1)
      );

    List<MeasurementValueProjection> resultsWithDayResolution = measurementJpaRepository
      .getAverageForPeriod(
        singletonList(meter.id),
        "day",
        "Energy",
        "W",
        START_TIME,
        START_TIME.plusSeconds(1)
      );

    List<MeasurementValueProjection> resultsWithMonthResolution = measurementJpaRepository
      .getAverageForPeriod(
        singletonList(meter.id),
        "month",
        "Energy",
        "W",
        START_TIME,
        START_TIME.plusSeconds(1)
      );

    assertThat(resultsWithDayResolution).hasSize(1);
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
    var meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME, 2.0, "W");

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        singletonList(meter.id),
        "hour",
        "Energy",
        "W",
        START_TIME,
        START_TIME.plusSeconds(1)
      );

    assertThat(results).extracting(MeasurementValueProjection::getUnit).containsExactly("W");
  }

  @Test
  public void seriesShouldIncludeEmptyResolutionPoints() {
    var meter = newPhysicalMeterEntity();

    newMeasurement(meter, START_TIME, 1.0, "kWh", "Energy");
    newMeasurement(meter, START_TIME.plusMinutes(1), 2.0, "kWh", "Energy");

    List<MeasurementValueProjection> result = measurementJpaRepository.getSeriesForPeriod(
      meter.id,
      "Energy",
      "MWh",
      START_TIME,
      START_TIME.plusMinutes(2),
      "minute"
    );

    assertThat(result).extracting(MeasurementValueProjection::getMeasurementUnit).containsExactly(
      Optional.of(new MeasurementUnit("MWh", 0.001)),
      Optional.of(new MeasurementUnit("MWh", 0.002)),
      Optional.empty()
    );
  }

  @Test
  public void seriesShouldNotIncludeValuesInBetweenResolutionPoints() {
    var meter = newPhysicalMeterEntity();

    newMeasurement(meter, START_TIME, 1.0, "kWh", "Energy");
    newMeasurement(meter, START_TIME.plusMinutes(30), 1.5, "kWh", "Energy");
    newMeasurement(meter, START_TIME.plusHours(1), 2.0, "kWh", "Energy");

    List<MeasurementValueProjection> result = measurementJpaRepository.getSeriesForPeriod(
      meter.id,
      "Energy",
      "kWh",
      START_TIME,
      START_TIME.plusHours(1),
      "hour"
    );

    assertThat(result).extracting(MeasurementValueProjection::getDoubleValue).containsExactly(
      1.0,
      2.0
    );
  }

  @Test
  public void getSeriesForConsumption_PutConsumptionOnStartOfInterval() {
    var meter = newPhysicalMeterEntity();

    newMeasurement(meter, START_TIME, 3.0, "m³", "Volume");
    newMeasurement(meter, START_TIME.plusHours(1), 6.0, "m³", "Volume");
    newMeasurement(meter, START_TIME.plusHours(2), 12.0, "m³", "Volume");
    newMeasurement(meter, START_TIME.plusHours(3), 24.0, "m³", "Volume");

    List<MeasurementValueProjection> result =
      measurementJpaRepository.getSeriesForPeriodConsumption(
        meter.id,
        "Volume",
        "m³",
        START_TIME,
        START_TIME.plusHours(2),
        "hour"
      );

    assertThat(result).extracting(MeasurementValueProjection::getDoubleValue).containsExactly(
      3.0,
      6.0,
      12.0
    );
  }

  @Test
  public void getSeriesForConsumption_PutSumOfMissingIntervalsOnStartOfInterval() {
    var meter = newPhysicalMeterEntity();

    newMeasurement(meter, START_TIME, 3.0, "m³", "Volume");
    newMeasurement(meter, START_TIME.plusHours(1), 6.0, "m³", "Volume");
    //missing measurement at START_TIME.plusHours(2)
    //missing measurement at START_TIME.plusHours(3)
    newMeasurement(meter, START_TIME.plusHours(4), 48.0, "m³", "Volume");
    newMeasurement(meter, START_TIME.plusHours(5), 96.0, "m³", "Volume");

    List<MeasurementValueProjection> result =
      measurementJpaRepository.getSeriesForPeriodConsumption(
        meter.id,
        "Volume",
        "m³",
        START_TIME,
        START_TIME.plusHours(4),
        "hour"
      );

    assertThat(result).extracting(MeasurementValueProjection::getDoubleValue).containsExactly(
      3.0,
      42.0,
      null,
      null,
      48.0
    );
  }

  @Test
  public void getSeriesForConsumption_MissingMeasurementAtStartOfIntervalAndAfterInterval() {
    var meter = newPhysicalMeterEntity();

    newMeasurement(meter, START_TIME.minusHours(1), 1.0, "m³", "Volume");
    // missing measurement for START_TIME
    newMeasurement(meter, START_TIME.plusHours(1), 6.0, "m³", "Volume");
    newMeasurement(meter, START_TIME.plusHours(2), 12.0, "m³", "Volume");
    // missing measurement after interval

    List<MeasurementValueProjection> result =
      measurementJpaRepository.getSeriesForPeriodConsumption(
        meter.id,
        "Volume",
        "m³",
        START_TIME,
        START_TIME.plusHours(2),
        "hour"
      );

    assertThat(result).extracting(MeasurementValueProjection::getDoubleValue).containsExactly(
      null,
      6.0,
      null
    );
  }

  @Test
  public void getSeriesForConsumption_MissingMeasurementAtEndOfInterval() {
    var meter = newPhysicalMeterEntity();

    newMeasurement(meter, START_TIME, 3.0, "m³", "Volume");
    newMeasurement(meter, START_TIME.plusHours(1), 6.0, "m³", "Volume");
    // missing measurement at end of interval

    List<MeasurementValueProjection> result =
      measurementJpaRepository.getSeriesForPeriodConsumption(
        meter.id,
        "Volume",
        "m³",
        START_TIME,
        START_TIME.plusHours(2),
        "hour"
      );

    assertThat(result).extracting(MeasurementValueProjection::getDoubleValue).containsExactly(
      3.0,
      null,
      null
    );
  }

  @Test
  public void getSeriesForConsumption_MissingMeasurementAtEndOfIntervalButLaterExists() {
    var meter = newPhysicalMeterEntity();

    newMeasurement(meter, START_TIME, 3.0, "m³", "Volume");
    newMeasurement(meter, START_TIME.plusHours(1), 6.0, "m³", "Volume");
    // missing measurement at end of interval
    newMeasurement(meter, START_TIME.plusHours(4), 24.0, "m³", "Volume");

    List<MeasurementValueProjection> result =
      measurementJpaRepository.getSeriesForPeriodConsumption(
        meter.id,
        "Volume",
        "m³",
        START_TIME,
        START_TIME.plusHours(2),
        "hour"
      );

    assertThat(result).extracting(MeasurementValueProjection::getDoubleValue).containsExactly(
      3.0,
      null,
      null
    );
  }

  @Test
  public void getSeriesForConsumption_MissingMeasurementAfterInterval() {
    var meter = newPhysicalMeterEntity();

    newMeasurement(meter, START_TIME, 3.0, "m³", "Volume");
    newMeasurement(meter, START_TIME.plusHours(1), 6.0, "m³", "Volume");
    newMeasurement(meter, START_TIME.plusHours(2), 12.0, "m³", "Volume");
    // missing measurement at START_TIME.plusHours(3)

    List<MeasurementValueProjection> result =
      measurementJpaRepository.getSeriesForPeriodConsumption(
        meter.id,
        "Volume",
        "m³",
        START_TIME,
        START_TIME.plusHours(2),
        "hour"
      );

    assertThat(result).extracting(MeasurementValueProjection::getDoubleValue).containsExactly(
      3.0,
      6.0,
      null
    );
  }

  @Test
  @Ignore("The transaction is commited after testmethod has returned, hence we get the "
    + "exception too late")
  public void mixedDimensionsAreRejected() {
    var meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME, 1.0, "m³", "Volume");
    assertThatThrownBy(() ->
      newMeasurement(meter, START_TIME.plusMinutes(1), 2.0, "m³/s", "Volume")
    ).isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  public void averageForConsumptionSeries() {
    var firstMeter = newPhysicalMeterEntity();

    newMeasurement(firstMeter, START_TIME.plusHours(1), 0.0, "kWh", "Energy");
    newMeasurement(firstMeter, START_TIME.plusHours(2), 1.0, "kWh", "Energy");
    newMeasurement(firstMeter, START_TIME.plusHours(3), 5.0, "kWh", "Energy");

    var secondMeter = newPhysicalMeterEntity();
    newMeasurement(secondMeter, START_TIME.plusHours(1), 1.0, "kWh", "Energy");
    newMeasurement(secondMeter, START_TIME.plusHours(2), 2.0, "kWh", "Energy");
    newMeasurement(secondMeter, START_TIME.plusHours(3), 3.0, "kWh", "Energy");

    List<MeasurementValueProjection> result =
      measurementJpaRepository.getAverageForPeriodConsumption(
        Arrays.asList(firstMeter.id, secondMeter.id),
        "hour",
        "Energy",
        "kWh",
        START_TIME.plusHours(1),
        START_TIME.plusHours(3)
      );

    assertThat(result).extracting(MeasurementValueProjection::getDoubleValue).containsExactly(
      1.0, // ((1.0 - 0.0) + (2.0 - 1.0)) / 2
      2.5, // ((5.0 - 1.0) + (3.0 - 2.0)) / 2
      null
    );
  }

  @Test
  public void averageForConsumptionSeries_lastIsNotNullWhenValueExistAfterPeriod() {
    var firstMeter = newPhysicalMeterEntity();
    newMeasurement(firstMeter, START_TIME.plusHours(1), 0.0, "kWh", "Energy");
    newMeasurement(firstMeter, START_TIME.plusHours(2), 1.0, "kWh", "Energy");
    newMeasurement(firstMeter, START_TIME.plusHours(3), 5.0, "kWh", "Energy");

    var secondMeter = newPhysicalMeterEntity();
    newMeasurement(secondMeter, START_TIME.plusHours(1), 1.0, "kWh", "Energy");
    newMeasurement(secondMeter, START_TIME.plusHours(2), 2.0, "kWh", "Energy");
    newMeasurement(secondMeter, START_TIME.plusHours(3), 3.0, "kWh", "Energy");

    List<MeasurementValueProjection> result = measurementJpaRepository
      .getAverageForPeriodConsumption(
        Arrays.asList(firstMeter.id, secondMeter.id),
        "hour",
        "Energy",
        "kWh",
        START_TIME.plusHours(1),
        START_TIME.plusHours(2)
      );

    assertThat(result).extracting(MeasurementValueProjection::getDoubleValue).containsExactly(
      1.0,  // ((1.0 - 0.0) + (2.0 - 1.0)) / 2
      2.5  // ((5.0 - 1.0) + (3.0 - 2.0)) / 2
    );
  }

  @Test
  public void averageForMissingMeasurements() {
    var firstMeter = newPhysicalMeterEntity();

    newMeasurement(firstMeter, START_TIME.plusHours(2), 1.0, "kWh", "Energy");
    newMeasurement(firstMeter, START_TIME.plusHours(3), 5.0, "kWh", "Energy");

    var secondMeter = newPhysicalMeterEntity();
    newMeasurement(secondMeter, START_TIME.plusHours(1), 7.0, "kWh", "Energy");
    newMeasurement(secondMeter, START_TIME.plusHours(2), 2.0, "kWh", "Energy");
    newMeasurement(secondMeter, START_TIME.plusHours(3), 3.0, "kWh", "Energy");

    List<MeasurementValueProjection> result = measurementJpaRepository.getAverageForPeriod(
      Arrays.asList(firstMeter.id, secondMeter.id),
      "hour",
      "Energy",
      "kWh",
      START_TIME.plusHours(1),
      START_TIME.plusHours(4)
    );

    // note: we average only over present measurement count for an interval
    assertThat(result).extracting(MeasurementValueProjection::getDoubleValue).containsExactly(
      7.0,
      1.5,
      4.0,
      null
    );
  }

  @Test
  public void averageForConsumptionSeries_missingMeasurementsForOneMeter() {
    var firstMeter = newPhysicalMeterEntity();
    newMeasurement(firstMeter, START_TIME.plusHours(2), 1.0, "kWh", "Energy");
    newMeasurement(firstMeter, START_TIME.plusHours(3), 5.0, "kWh", "Energy");

    var secondMeter = newPhysicalMeterEntity();
    newMeasurement(secondMeter, START_TIME.plusHours(1), 0.0, "kWh", "Energy");
    newMeasurement(secondMeter, START_TIME.plusHours(2), 2.0, "kWh", "Energy");
    newMeasurement(secondMeter, START_TIME.plusHours(3), 3.0, "kWh", "Energy");

    List<MeasurementValueProjection> result =
      measurementJpaRepository.getAverageForPeriodConsumption(
        Arrays.asList(firstMeter.id, secondMeter.id),
        "hour",
        "Energy",
        "kWh",
        START_TIME.plusHours(1),
        START_TIME.plusHours(2)
      );

    assertThat(result).extracting(MeasurementValueProjection::getDoubleValue).containsExactly(
      2.0, // (2.0 - 0.0) / 1
      2.5 // ((5.0 - 1.0) + (3.0 - 2.0)) / 2
    );
  }

  @Test
  public void findFirstReadoutWithinRange() {
    var meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME.plusHours(2), 3.0, "kWh", "Energy");
    newMeasurement(meter, START_TIME, 1.0, "kWh", "Energy");
    newMeasurement(meter, START_TIME.plusHours(1), 2.0, "kWh", "Energy");

    var firstEnergy = measurementJpaRepository
      .firstForPhysicalMeter(
        meter.id,
        START_TIME.minusDays(1).toZonedDateTime(),
        START_TIME.plusHours(3).toZonedDateTime()
      ).get();

    assertThat(firstEnergy.id.created.toInstant()).isEqualTo(START_TIME.toInstant());
    assertThat(firstEnergy.value.getValue()).isEqualTo(1.0);

    firstEnergy = measurementJpaRepository
      .firstForPhysicalMeter(
        meter.id,
        START_TIME.plusHours(1).plusMinutes(59).toZonedDateTime(),
        START_TIME.plusHours(2).toZonedDateTime()
      ).get();

    assertThat(firstEnergy.id.created.toInstant()).isEqualTo(START_TIME.plusHours(2).toInstant());
    assertThat(firstEnergy.value.getValue()).isEqualTo(3.0);
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
      1,
      1,
      emptySet(),
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
      QuantityEntityMapper.toEntity(QuantityAccess.singleton().getByName(quantity)),
      new MeasurementUnit(unit, value),
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
