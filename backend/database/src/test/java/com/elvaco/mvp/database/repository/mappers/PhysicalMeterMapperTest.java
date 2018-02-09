package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import org.junit.Test;

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static org.assertj.core.api.Assertions.assertThat;

public class PhysicalMeterMapperTest {
  private final PhysicalMeterMapper physicalMeterMapper = new PhysicalMeterMapper(
    new OrganisationMapper()
  );

  @Test
  public void mapping() {
    PhysicalMeter physicalMeter = new PhysicalMeter(ELVACO, "567890", "My Medium", "ELV");
    PhysicalMeterEntity physicalMeterEntity = physicalMeterMapper.toEntity(physicalMeter);
    assertThat(physicalMeterEntity.identity).isEqualTo("567890");
    assertThat(physicalMeterEntity.medium).isEqualTo("My Medium");
    assertThat(physicalMeterEntity.manufacturer).isEqualTo("ELV");
    assertThat(physicalMeterMapper.toDomainModel(physicalMeterEntity)).isEqualTo(physicalMeter);
  }

}
