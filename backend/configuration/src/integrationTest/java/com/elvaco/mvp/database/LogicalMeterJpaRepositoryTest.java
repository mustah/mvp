package com.elvaco.mvp.database;

import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterJpaRepositoryTest extends IntegrationTest {

  @Test
  public void locationIsPersisted() {
    LogicalMeter logicalMeter = given(
      logicalMeter().location(
        new LocationBuilder().latitude(1.0).longitude(2.0).confidence(1.0).build()
      )
    );

    LogicalMeterEntity foundEntity = logicalMeterJpaRepository.findById(logicalMeter.id).get();

    assertThat(foundEntity.location.confidence).isEqualTo(1.0);
    assertThat(foundEntity.location.latitude).isEqualTo(1.0);
    assertThat(foundEntity.location.longitude).isEqualTo(2.0);
  }

  @Test
  public void physicalMetersAreFetched() {
    LogicalMeter logicalMeter = given(logicalMeter(), physicalMeter());

    assertThat(logicalMeterJpaRepository.findById(logicalMeter.id).get().physicalMeters)
      .isNotEmpty();
  }
}
