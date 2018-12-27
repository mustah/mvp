package com.elvaco.mvp.database;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinitionType;
import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.database.entity.meter.EntityPk;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.repository.jpa.MeterDefinitionJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterJpaRepositoryTest extends IntegrationTest {

  @Autowired
  private MeterDefinitionJpaRepository meterDefinitionJpaRepository;

  private UUID logicalMeterId;

  @Before
  public void setUp() {
    logicalMeterId = randomUUID();

    EntityPk pk = new EntityPk(logicalMeterId, context().organisationId());

    LogicalMeterEntity logicalMeterEntity = new LogicalMeterEntity(
      pk,
      "Some external ID",
      ZonedDateTime.now(),
      meterDefinitionJpaRepository.getOne(MeterDefinitionType.UNKNOWN_METER_TYPE),
      LogicalMeter.UTC_OFFSET
    );
    logicalMeterEntity.location = LocationEntity.builder()
      .pk(pk)
      .latitude(1.0)
      .longitude(2.0)
      .confidence(1.0)
      .build();

    logicalMeterJpaRepository.save(logicalMeterEntity);

    PhysicalMeterEntity physicalMeterEntity = new PhysicalMeterEntity(
      randomUUID(),
      context().organisationId(),
      "123123",
      "Some external ID",
      "Some medium",
      "ELV",
      logicalMeterId,
      PeriodRange.empty(),
      15,
      1,
      1,
      emptySet(),
      emptySet()
    );
    physicalMeterJpaRepository.save(physicalMeterEntity);
  }

  @Test
  public void locationIsPersisted() {
    LogicalMeterEntity foundEntity = logicalMeterJpaRepository.findById(logicalMeterId).get();
    assertThat(foundEntity.location.confidence).isEqualTo(1.0);
    assertThat(foundEntity.location.latitude).isEqualTo(1.0);
    assertThat(foundEntity.location.longitude).isEqualTo(2.0);
  }

  @Test
  public void physicalMetersAreFetched() {
    assertThat(logicalMeterJpaRepository.findById(logicalMeterId).get().physicalMeters)
      .isNotEmpty();
  }
}
