package com.elvaco.mvp.database;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.exception.NoSuchQuantity;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.database.repository.mappers.QuantityEntityMapper;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@Transactional
public class MeasurementJpaRepositoryTest extends IntegrationTest {

  private static final OffsetDateTime START_TIME =
    OffsetDateTime.parse("2018-01-01T00:00:00+00:00");

  @Autowired
  private MeasurementJpaRepository measurementJpaRepository;

  @Autowired
  private QuantityProvider quantityProvider;

  @Autowired
  private QuantityEntityMapper quantityEntityMapper;

  @After
  public void tearDown() {
    measurementJpaRepository.deleteAll();
  }

  @Test
  public void findFirstReadoutWithinRange() {
    var meter = newPhysicalMeterEntity();
    newMeasurement(meter, START_TIME.plusHours(2), 3.0, "Energy");
    newMeasurement(meter, START_TIME, 1.0, "Energy");
    newMeasurement(meter, START_TIME.plusHours(1), 2.0, "Energy");

    var firstEnergy = measurementJpaRepository
      .firstForPhysicalMeter(
        meter.getOrganisationId(),
        meter.id,
        START_TIME.minusDays(1).toZonedDateTime(),
        START_TIME.plusHours(3).toZonedDateTime()
      ).get();

    assertThat(firstEnergy.id.readoutTime.toInstant()).isEqualTo(START_TIME.toInstant());
    assertThat(firstEnergy.value).isEqualTo(1.0);

    firstEnergy = measurementJpaRepository
      .firstForPhysicalMeter(
        meter.getOrganisationId(),
        meter.id,
        START_TIME.plusHours(1).plusMinutes(59).toZonedDateTime(),
        START_TIME.plusHours(2).toZonedDateTime()
      ).get();

    assertThat(firstEnergy.id.readoutTime.toInstant())
      .isEqualTo(START_TIME.plusHours(2).toInstant());
    assertThat(firstEnergy.value).isEqualTo(3.0);
  }

  private PhysicalMeterEntity newPhysicalMeterEntity() {
    UUID uuid = UUID.randomUUID();
    return physicalMeterJpaRepository.save(new PhysicalMeterEntity(
      uuid,
      context().organisationId(),
      "",
      uuid.toString(),
      "",
      "",
      null,
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
    String quantity
  ) {
    measurementJpaRepository.save(new MeasurementEntity(
      when.toZonedDateTime(),
      when.toZonedDateTime(),
      null,
      quantityEntityMapper.toEntity(quantityProvider.getByName(quantity)
        .orElseThrow(() -> new NoSuchQuantity(quantity))),
      value,
      meter
    ));
  }
}
