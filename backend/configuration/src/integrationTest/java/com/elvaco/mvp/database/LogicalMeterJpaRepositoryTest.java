package com.elvaco.mvp.database;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.core.access.QuantityAccess;
import com.elvaco.mvp.core.domainmodels.MeterDefinitionType;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.database.entity.meter.EntityPrimaryKey;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.repository.jpa.MeterDefinitionJpaRepository;
import com.elvaco.mvp.database.repository.mappers.QuantityEntityMapper;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterJpaRepositoryTest extends IntegrationTest {

  @Autowired
  private MeterDefinitionJpaRepository meterDefinitionJpaRepository;

  private UUID logicalMeterId;

  @Before
  public void setUp() {
    logicalMeterId = randomUUID();

    Quantity power = QuantityAccess.singleton().getByName(Quantity.POWER.name);

    MeterDefinitionEntity meterDefinitionEntity = meterDefinitionJpaRepository.save(
      new MeterDefinitionEntity(
        MeterDefinitionType.UNKNOWN_METER_TYPE,
        singleton(QuantityEntityMapper.toEntity(power)),
        "My meter definition",
        false
      ));

    LogicalMeterEntity logicalMeterEntity = new LogicalMeterEntity(
      logicalMeterId,
      "Some external ID",
      context().organisationId(),
      ZonedDateTime.now(),
      meterDefinitionEntity,
      DEFAULT_UTC_OFFSET
    );
    logicalMeterEntity.location = LocationEntity.builder()
      .pk(new EntityPrimaryKey(logicalMeterId, context().organisationId()))
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
