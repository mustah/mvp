package com.elvaco.mvp.database;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeasurementValueProjection;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.IntegrationTestFixtureContext;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class MeasurementJpaRepositoryTest extends IntegrationTest {

  private static final ZonedDateTime START_TIME = ZonedDateTime.parse("2018-01-01T00:00:00+00:00");

  @Autowired
  MeasurementJpaRepository measurementJpaRepository;
  @Autowired
  PhysicalMeterJpaRepository physicalMeterJpaRepository;
  @Autowired
  OrganisationJpaRepository organisationJpaRepository;

  private IntegrationTestFixtureContext context;

  @Before
  public void setUp() {
    Assume.assumeTrue(isPostgresDialect());
    context = newContext();
  }

  @After
  public void tearDown() {
    physicalMeterJpaRepository.deleteAll();
    destroyContext(context);
  }

  @Test
  public void correctNumberOfValuesAreReturnedRelativeToStart() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    generateSeries(meter, 10, START_TIME, Duration.ofHours(1), 2.0, "W");
    ZonedDateTime fiveHoursIn = START_TIME.plus(Duration.ofHours(5));
    ZonedDateTime lastHourWithMeasurements = START_TIME.plus(Duration.ofHours(9));

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        Collections.singletonList(meter.id),
        "hour",
        "Energy",
        fiveHoursIn,
        lastHourWithMeasurements
      );

    assertThat(results).hasSize(5);
  }

  @Test
  public void allValuesHaveSameScale() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME, 2.0, "W");
    ZonedDateTime oneHourLater = START_TIME.plus(Duration.ofHours(1));
    newMeasurement(meter, oneHourLater, 0.002, "kW");

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        Collections.singletonList(meter.id), "hour", "Energy", START_TIME, oneHourLater);

    assertThat(results).hasSize(2);
    assertThat(results).allMatch(v -> v.getValue() == 2.0);
  }

  @Test
  public void missingIntervalValuesAreRepresentedAsNull() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME, 2.0, "W");
    ZonedDateTime twoHoursLater = START_TIME.plus(Duration.ofHours(2));
    newMeasurement(meter, twoHoursLater, 2.0, "W");

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        Collections.singletonList(meter.id), "hour", "Energy", START_TIME, twoHoursLater);

    assertThat(results).hasSize(3);
    assertThat(results.get(0).getValue()).isEqualTo(2.0);
    assertThat(results.get(1).getValue()).isEqualTo(null);
    assertThat(results.get(2).getValue()).isEqualTo(2.0);
  }

  @Test
  public void averageValueIsCorrectForPeriod() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME, 2.0, "W");
    newMeasurement(meter, START_TIME, 100.0, "W");

    ZonedDateTime oneHourLater = START_TIME.plus(Duration.ofHours(1));
    newMeasurement(meter, oneHourLater, 1.0, "W");
    newMeasurement(meter, oneHourLater, 9.0, "W");


    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        Collections.singletonList(meter.id), "hour", "Energy", START_TIME, oneHourLater);

    assertThat(results).hasSize(2);
    assertThat(results.get(0).getValue()).isEqualTo(51.0);
    assertThat(results.get(1).getValue()).isEqualTo(5.0);
  }

  @Test
  public void unspecifiedMetersAreNotIncluded() {
    PhysicalMeterEntity firstMeter = newPhysicalMeterEntity();
    PhysicalMeterEntity secondMeter = newPhysicalMeterEntity();
    newMeasurement(firstMeter, START_TIME, 12, "W");
    newMeasurement(secondMeter, START_TIME, 99.8, "W");

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        Collections.singletonList(firstMeter.id),
        "hour",
        "Energy", START_TIME,
        START_TIME.plus(Duration.ofSeconds(1))
      );

    assertThat(results).hasSize(1);
    assertThat(results.get(0).getValue()).isEqualTo(12);
  }

  @Test
  public void valuesAreFilteredByQuantity() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME, 2.0, "W", "A");
    newMeasurement(meter, START_TIME, 6.0, "W", "B");

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        Collections.singletonList(meter.id),
        "hour",
        "A",
        START_TIME,
        START_TIME.plus(Duration.ofSeconds(1))
      );

    assertThat(results).hasSize(1);
    assertThat(results.get(0).getValue()).isEqualTo(2.0);
  }

  @Test
  public void timesAreCorrect() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME, 2.0, "W");

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        Collections.singletonList(meter.id),
        "hour",
        "Energy",
        START_TIME,
        START_TIME.plus(Duration.ofSeconds(1))
      );

    assertThat(results).hasSize(1);
    assertThat(results.get(0).getWhen().toInstant()).isEqualTo(START_TIME.toInstant());
  }

  @Test
  public void correctScaleIsReturned() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME, 2.0, "W");

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        Collections.singletonList(meter.id),
        "hour",
        "Energy",
        START_TIME,
        START_TIME.plus(Duration.ofSeconds(1))
      );

    assertThat(results).hasSize(1);
    assertThat(results.get(0).getUnit()).isEqualTo("W");
  }

  private PhysicalMeterEntity newPhysicalMeterEntity() {
    UUID uuid = UUID.randomUUID();
    return physicalMeterJpaRepository.save(new PhysicalMeterEntity(
      uuid,
      context.organisationEntity,
      "",
      uuid.toString(),
      "",
      "",
      null
    ));
  }

  private void newMeasurement(
    PhysicalMeterEntity meter,
    ZonedDateTime when,
    double value,
    String unit,
    String quantity
  ) {
    measurementJpaRepository.save(new MeasurementEntity(
      when,
      quantity,
      value,
      unit,
      meter
    ));
  }

  private void newMeasurement(
    PhysicalMeterEntity meter,
    ZonedDateTime when,
    double value,
    String unit
  ) {
    newMeasurement(meter, when, value, unit, "Energy");
  }

  private void generateSeries(
    PhysicalMeterEntity meter,
    int count,
    ZonedDateTime startDate,
    Duration interval,
    double value,
    String unit
  ) {
    ZonedDateTime when = startDate;
    for (int i = 0; i < count; i++) {
      newMeasurement(meter, when, value, unit);
      when = when.plus(interval);
    }
  }
}
