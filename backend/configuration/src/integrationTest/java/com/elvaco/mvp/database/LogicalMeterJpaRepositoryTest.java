package com.elvaco.mvp.database;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.MeterDefinitionType;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QuantityEntity;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeterDefinitionJpaRepository;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterJpaRepositoryTest extends IntegrationTest {

  @Autowired
  private LogicalMeterJpaRepository logicalMeterJpaRepository;

  @Autowired
  private PhysicalMeterJpaRepository physicalMeterJpaRepository;

  @Autowired
  private OrganisationJpaRepository organisationRepository;

  @Autowired
  private MeterDefinitionJpaRepository meterDefinitionJpaRepository;

  private UUID logicalMeterId;

  @Before
  public void setUp() {
    logicalMeterId = randomUUID();

    MeterDefinitionEntity meterDefinitionEntity = meterDefinitionJpaRepository.save(
      new MeterDefinitionEntity(
        MeterDefinitionType.UNKNOWN_METER_TYPE,
        singleton(new QuantityEntity(null, "Speed", "m/s")),
        "My meter definition",
        false
      ));

    LogicalMeterEntity logicalMeterEntity = new LogicalMeterEntity(
      logicalMeterId,
      "Some external ID",
      context().getOrganisationId(),
      ZonedDateTime.now(),
      meterDefinitionEntity
    );
    logicalMeterEntity.location = new LocationEntity(logicalMeterId, 1.0, 2.0, 1.0);

    logicalMeterJpaRepository.save(logicalMeterEntity);

    PhysicalMeterEntity physicalMeterEntity = new PhysicalMeterEntity(
      randomUUID(),
      organisationRepository.findOne(context().getOrganisationId()),
      "123123",
      "Some external ID",
      "Some medium",
      "ELV",
      logicalMeterId,
      15
    );
    physicalMeterJpaRepository.save(physicalMeterEntity);
  }

  @After
  public void tearDown() {
    physicalMeterJpaRepository.deleteAll();
    logicalMeterJpaRepository.deleteAll();
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
