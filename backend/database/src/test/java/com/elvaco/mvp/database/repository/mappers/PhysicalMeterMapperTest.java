package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import org.junit.Test;

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class PhysicalMeterMapperTest {

  private final PhysicalMeterMapper physicalMeterMapper = new PhysicalMeterMapper(
    new OrganisationMapper()
  );

  @Test
  public void mapping() {
    PhysicalMeter physicalMeter = new PhysicalMeter(
      randomUUID(),
      "567890",
      "external-id",
      "My Medium",
      "ELV",
      ELVACO,
      15
    );

    PhysicalMeterEntity physicalMeterEntity = physicalMeterMapper.toEntity(physicalMeter);

    assertThat(physicalMeterEntity.address).isEqualTo("567890");
    assertThat(physicalMeterEntity.externalId).isEqualTo("external-id");
    assertThat(physicalMeterEntity.medium).isEqualTo("My Medium");
    assertThat(physicalMeterEntity.manufacturer).isEqualTo("ELV");
    assertThat(physicalMeterMapper.toDomainModel(physicalMeterEntity)).isEqualTo(physicalMeter);
  }
}
