package com.elvaco.mvp.database;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeasurementValueProjection;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("postgresql")
public class MeasurementJpaRepositoryTest extends IntegrationTest {

  private static int FIFTEEN_MINUTE_INTERVAL = 1000 * 15 * 60;
  private static int HOUR_INTERVAL = 1000 * 60 * 60;
  @Autowired
  MeasurementJpaRepository measurementJpaRepository;
  @Autowired
  PhysicalMeterJpaRepository physicalMeterJpaRepository;
  @Autowired
  OrganisationJpaRepository organisationJpaRepository;

  private OrganisationEntity organisationEntity;

  @Before
  public void setUp() {
    organisationEntity = organisationJpaRepository.save(new OrganisationEntity(
      UUID.randomUUID(),
      "organisationen",
      "organisationen"
    ));
  }

  @After
  public void tearDown() {
    physicalMeterJpaRepository.deleteAll();
    organisationJpaRepository.delete(organisationEntity.id);
  }

  @Test
  public void correctNumberOfValuesAreReturnedRelativeToStart() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    Date start = new Date(118, 0, 1, 0, 0);
    generateSeries(meter, 10, start, HOUR_INTERVAL, 2.0, "W");
    Date since = new Date(start.getTime() + (HOUR_INTERVAL * 5));
    Date end = new Date(start.getTime() + (HOUR_INTERVAL * 9));

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        Collections.singletonList(meter.id), "hour", since, end);

    assertThat(results).hasSize(5);
  }

  @Test
  public void allValuesHaveSameScale() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    Date first = new Date(118, 0, 1, 0, 0);
    newMeasurement(meter, first, 2.0, "W");
    Date second = new Date(118, 0, 1, 1, 0);
    newMeasurement(meter, second, 0.002, "kW");

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        Collections.singletonList(meter.id), "hour", first, second);

    assertThat(results).hasSize(2);
    assertThat(results).allMatch(v -> v.getValue() == 2.0);
  }

  @Test
  public void missingIntervalValuesAreRepresentedAsNull() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    Date first = new Date(118, 0, 1, 1, 0);
    newMeasurement(meter, first, 2.0, "W");
    Date second = new Date(118, 0, 1, 3, 0);
    newMeasurement(meter, second, 2.0, "W");

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        Collections.singletonList(meter.id), "hour", first, second);

    assertThat(results).hasSize(3);
    assertThat(results.get(0).getValue()).isEqualTo(2.0);
    assertThat(results.get(1).getValue()).isEqualTo(null);
    assertThat(results.get(2).getValue()).isEqualTo(2.0);
  }

  @Test
  public void averageValueIsCorrectForPeriod() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    Date first = new Date(118, 0, 1, 0, 0);
    newMeasurement(meter, first, 2.0, "W");
    newMeasurement(meter, first, 100.0, "W");

    Date second = new Date(118, 0, 1, 1, 1);
    newMeasurement(meter, second, 1.0, "W");
    newMeasurement(meter, second, 9.0, "W");


    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        Collections.singletonList(meter.id), "hour", first, second);

    assertThat(results).hasSize(2);
    assertThat(results.get(0).getValue()).isEqualTo(51.0);
    assertThat(results.get(1).getValue()).isEqualTo(5.0);
  }

  @Test
  public void unspecifiedMetersAreNotIncluded() {
    PhysicalMeterEntity firstMeter = newPhysicalMeterEntity();
    PhysicalMeterEntity secondMeter = newPhysicalMeterEntity();
    Date januaryFirst2018 = new Date(118, 0, 1, 0, 0);
    newMeasurement(firstMeter, januaryFirst2018, 12, "W");
    newMeasurement(secondMeter, januaryFirst2018, 99.8, "W");

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        Collections.singletonList(firstMeter.id), "hour", januaryFirst2018, januaryFirst2018);

    assertThat(results).hasSize(1);
    assertThat(results.get(0).getValue()).isEqualTo(12);
  }

  @Test
  public void correctScaleIsReturned() {
    PhysicalMeterEntity meter = newPhysicalMeterEntity();
    Date start = new Date(118, 0, 1, 0, 0);
    generateSeries(meter, 100, start, FIFTEEN_MINUTE_INTERVAL, 200, "W");

    List<MeasurementValueProjection> results = measurementJpaRepository
      .getAverageForPeriod(
        Collections.singletonList(meter.id), 15, start, "kW");

    assertThat(results).hasSize(100);
    assertThat(results).allMatch(v -> v.getValue() == 0.2);
  }

  private PhysicalMeterEntity newPhysicalMeterEntity() {
    UUID uuid = UUID.randomUUID();
    return physicalMeterJpaRepository.save(new PhysicalMeterEntity(
      uuid,
      organisationEntity,
      "",
      uuid.toString(),
      "",
      "",
      null
    ));
  }

  private void newMeasurement(PhysicalMeterEntity meter, Date when, double value, String unit) {
    measurementJpaRepository.save(new MeasurementEntity(
      when,
      "Energy",
      value,
      unit,
      meter
    ));
  }

  private void generateSeries(
    PhysicalMeterEntity meter,
    int count,
    Date startDate,
    int interval,
    double value,
    String unit
  ) {

    for (int i = 0; i < count; i++) {
      Date when = new Date();
      when.setTime(startDate.getTime() + (interval * i));
      newMeasurement(meter, when, value, unit);
    }
  }
}
