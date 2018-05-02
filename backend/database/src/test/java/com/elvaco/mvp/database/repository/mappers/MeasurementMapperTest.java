package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.MeasurementUnit;
import org.junit.Test;

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static org.assertj.core.api.Assertions.assertThat;

public class MeasurementMapperTest {

  @Test
  public void mapping() {
    Measurement measurement = new Measurement(
      Quantity.VOLUME,
      2.0,
      PhysicalMeter.builder()
        .address("123-123")
        .externalId("external-id")
        .medium("Hot water")
        .manufacturer("ELV")
        .organisation(ELVACO)
        .readIntervalMinutes(15)
        .build()
    );

    MeasurementEntity entity = MeasurementMapper.toEntity(measurement);
    assertThat(entity.quantity).isEqualTo("Volume");
    assertThat(entity.value).isEqualTo(MeasurementUnit.from("2.0 m³"));
    Measurement actual = MeasurementMapper.toDomainModel(entity);
    assertThat(actual).isEqualTo(measurement);
  }
}
