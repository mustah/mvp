package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import org.junit.Test;

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static org.assertj.core.api.Assertions.assertThat;

public class PhysicalMeterMapperTest {

  @Test
  public void mapToEntity() {
    PhysicalMeter physicalMeter = newPhysicalMeter();

    PhysicalMeterEntity entity = PhysicalMeterMapper.toEntity(physicalMeter);

    assertThat(entity.address).isEqualTo("567890");
    assertThat(entity.externalId).isEqualTo("external-id");
    assertThat(entity.medium).isEqualTo("My Medium");
    assertThat(entity.manufacturer).isEqualTo("ELV");
  }

  @Test
  public void mapToDomainModel() {
    PhysicalMeter physicalMeter = newPhysicalMeter();
    PhysicalMeterEntity entity = PhysicalMeterMapper.toEntity(physicalMeter);

    assertThat(PhysicalMeterMapper.toDomainModel(entity)).isEqualTo(physicalMeter);
  }

  private static PhysicalMeter newPhysicalMeter() {
    return PhysicalMeter.builder()
      .address("567890")
      .externalId("external-id")
      .medium("My Medium")
      .manufacturer("ELV")
      .organisation(ELVACO)
      .readIntervalMinutes(15)
      .build();
  }
}
