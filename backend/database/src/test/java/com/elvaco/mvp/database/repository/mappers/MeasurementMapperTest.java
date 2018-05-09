package com.elvaco.mvp.database.repository.mappers;

import java.time.ZonedDateTime;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.MeasurementUnit;
import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.Quantity.VOLUME;
import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static com.elvaco.mvp.database.repository.mappers.MeasurementMapper.toDomainModel;
import static org.assertj.core.api.Assertions.assertThat;

public class MeasurementMapperTest {

  @Test
  public void mapping() {
    Measurement measurement = Measurement.builder()
      .created(ZonedDateTime.now())
      .value(2.0)
      .quantity(VOLUME.name)
      .unit(VOLUME.presentationUnit())
      .physicalMeter(PhysicalMeter.builder()
        .address("123-123")
        .externalId("external-id")
        .medium("Hot water")
        .manufacturer("ELV")
        .organisation(ELVACO)
        .readIntervalMinutes(15)
        .build()
      )
      .build();

    MeasurementEntity entity = MeasurementMapper.toEntity(measurement);
    assertThat(entity.quantity).isEqualTo("Volume");
    assertThat(entity.value).isEqualTo(MeasurementUnit.from("2.0 m³"));
    assertThat(toDomainModel(entity)).isEqualTo(measurement);
  }
}
