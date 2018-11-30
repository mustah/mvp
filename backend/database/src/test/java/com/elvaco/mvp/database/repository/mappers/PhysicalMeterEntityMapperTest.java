package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import org.junit.Test;

import static com.elvaco.mvp.testing.fixture.OrganisationTestData.ELVACO;
import static org.assertj.core.api.Assertions.assertThat;

public class PhysicalMeterEntityMapperTest {

  @Test
  public void mapToEntity() {
    PhysicalMeter physicalMeter = newPhysicalMeter();

    PhysicalMeterEntity entity = PhysicalMeterEntityMapper.toEntity(physicalMeter);

    assertThat(entity.address).isEqualTo("567890");
    assertThat(entity.externalId).isEqualTo("external-id");
    assertThat(entity.medium).isEqualTo("My Medium");
    assertThat(entity.manufacturer).isEqualTo("ELV");
    assertThat(entity.revision).isEqualTo(5);
    assertThat(entity.mbusDeviceType).isEqualTo(9);
  }

  @Test
  public void mapToDomainModel() {
    PhysicalMeter physicalMeter = newPhysicalMeter();
    PhysicalMeterEntity entity = PhysicalMeterEntityMapper.toEntity(physicalMeter);

    assertThat(PhysicalMeterEntityMapper.toDomainModel(entity)).isEqualTo(physicalMeter);
  }

  private static PhysicalMeter newPhysicalMeter() {
    return PhysicalMeter.builder()
      .address("567890")
      .externalId("external-id")
      .medium("My Medium")
      .manufacturer("ELV")
      .organisationId(ELVACO.id)
      .readIntervalMinutes(15)
      .revision(5)
      .mbusDeviceType(9)
      .build();
  }
}
