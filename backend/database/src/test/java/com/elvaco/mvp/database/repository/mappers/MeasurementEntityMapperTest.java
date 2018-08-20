package com.elvaco.mvp.database.repository.mappers;

import java.time.ZonedDateTime;
import java.util.Arrays;

import com.elvaco.mvp.core.access.QuantityAccess;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.MeasurementUnit;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.Quantity.VOLUME;
import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static com.elvaco.mvp.database.repository.mappers.MeasurementEntityMapper.toDomainModel;
import static org.assertj.core.api.Assertions.assertThat;

public class MeasurementEntityMapperTest {

  @BeforeClass
  public static void setup() {
    QuantityAccess.singleton().loadAll(Arrays.asList(
      new Quantity(1,"Volume",VOLUME.getPresentationInformation())
    ));
  }

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

    MeasurementEntity entity = MeasurementEntityMapper.toEntity(measurement);
    assertThat(entity.quantity.name).isEqualTo("Volume");
    assertThat(entity.value).isEqualTo(MeasurementUnit.from("2.0 m³"));
    assertThat(toDomainModel(entity)).isEqualTo(measurement);
  }
}
