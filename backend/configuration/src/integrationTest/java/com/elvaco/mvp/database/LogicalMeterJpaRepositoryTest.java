package com.elvaco.mvp.database;

import java.util.Date;

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

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static java.util.Collections.singleton;
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

  private Long logicalMeterId;

  @Before
  public void setUp() {
    MeterDefinitionEntity meterDefinitionEntity = meterDefinitionJpaRepository.save(
      new MeterDefinitionEntity(
        null,
        singleton(new QuantityEntity(null, "Speed", "m/s")),
        "My meter definition",
        false
      ));

    LogicalMeterEntity logicalMeterEntity = new LogicalMeterEntity(
      null,
      "Some external ID",
      ELVACO.id,
      new Date(),
      meterDefinitionEntity
    );
    LocationEntity locationEntity = new LocationEntity();
    locationEntity.confidence = 1.0;
    locationEntity.latitude = 1.0;
    locationEntity.longitude = 2.0;
    logicalMeterEntity.setLocation(locationEntity);

    logicalMeterEntity = logicalMeterJpaRepository.save(logicalMeterEntity);
    PhysicalMeterEntity physicalMeterEntity = new PhysicalMeterEntity(
      organisationRepository.findOne(ELVACO.id),
      "123123",
      "Some external ID",
      "Some medium",
      "ELV"
    );
    physicalMeterEntity.logicalMeterId = logicalMeterEntity.id;
    physicalMeterJpaRepository.save(physicalMeterEntity);
    logicalMeterId = logicalMeterEntity.id;
  }

  @After
  public void tearDown() {
    physicalMeterJpaRepository.deleteAll();
    logicalMeterJpaRepository.deleteAll();
  }

  @Test
  public void locationIsPersisted() {
    LogicalMeterEntity foundEntity = logicalMeterJpaRepository.findOne(logicalMeterId);
    assertThat(foundEntity.getLocation().confidence).isEqualTo(1.0);
    assertThat(foundEntity.getLocation().latitude).isEqualTo(1.0);
    assertThat(foundEntity.getLocation().longitude).isEqualTo(2.0);
  }

  @Test
  public void physicalMetersAreFetched() {
    assertThat(logicalMeterJpaRepository.findOne(logicalMeterId).physicalMeters).isNotEmpty();
  }
}
