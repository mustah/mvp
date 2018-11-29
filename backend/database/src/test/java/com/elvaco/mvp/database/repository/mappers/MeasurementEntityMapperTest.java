package com.elvaco.mvp.database.repository.mappers;

import java.time.ZonedDateTime;
import java.util.List;

import com.elvaco.mvp.core.access.QuantityAccess;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.Quantity.VOLUME;
import static com.elvaco.mvp.database.repository.mappers.MeasurementEntityMapper.toDomainModel;
import static com.elvaco.mvp.database.repository.mappers.MeasurementEntityMapper.toEntity;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.ELVACO;
import static org.assertj.core.api.Assertions.assertThat;

public class MeasurementEntityMapperTest {

  @BeforeClass
  public static void setup() {
    QuantityAccess.singleton()
      .loadAll(List.of(new Quantity(
        1,
        "Volume",
        VOLUME.getPresentationInformation(),
        VOLUME.storageUnit
      )));
  }

  @Test
  public void toEntity_HandlesQuantity() {
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
        .organisationId(ELVACO.id)
        .readIntervalMinutes(15)
        .build()
      )
      .build();

    MeasurementEntity entity = toEntity(measurement);
    assertThat(entity.id.quantity.name).isEqualTo("Volume");
    assertThat(entity.value).isEqualTo(2.0);
    assertThat(toDomainModel(entity)).isEqualTo(measurement);
  }
}
