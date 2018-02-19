package com.elvaco.mvp.database;

import java.util.Date;

import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterJpaRepositoryTest extends IntegrationTest {

  @Autowired
  private LogicalMeterJpaRepository logicalMeterJpaRepository;

  @Autowired
  private PhysicalMeterJpaRepository physicalMeterJpaRepository;

  @Autowired
  private OrganisationJpaRepository organisationRepository;

  private Long logicalMeterId;

  @Before
  public void setUp() {
    LogicalMeterEntity mp = new LogicalMeterEntity();
    mp.created = new Date();
    LocationEntity locationEntity = new LocationEntity();
    locationEntity.confidence = 1.0;
    locationEntity.latitude = 1.0;
    locationEntity.longitude = 2.0;
    mp.setLocation(locationEntity);

    mp = logicalMeterJpaRepository.save(mp);
    PhysicalMeterEntity physicalMeterEntity = new PhysicalMeterEntity(
      organisationRepository.findOne(0L),
      "123123",
      "Some external ID",
      "Some medium",
      "ELV"
    );
    physicalMeterEntity.logicalMeterId = mp.id;
    physicalMeterJpaRepository.save(physicalMeterEntity);
    logicalMeterId = mp.id;
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
