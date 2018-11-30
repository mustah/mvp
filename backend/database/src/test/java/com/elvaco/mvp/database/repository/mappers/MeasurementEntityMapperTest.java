package com.elvaco.mvp.database.repository.mappers;

import java.time.ZonedDateTime;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementUnit;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.unitconverter.UnitConverter;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import org.junit.Before;
import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.Quantity.VOLUME;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.ELVACO;
import static org.assertj.core.api.Assertions.assertThat;

public class MeasurementEntityMapperTest {

  private MeasurementEntityMapper entityMapper;

  @Before
  public void setUp() {
    entityMapper = new MeasurementEntityMapper(
      new UnitConverter() {
        @Override
        public MeasurementUnit convert(
          MeasurementUnit measurementUnit, String targetUnit
        ) {
          return measurementUnit;
        }

        @Override
        public boolean isSameDimension(String firstUnit, String secondUnit) {
          return false;
        }
      },
      (name) -> new Quantity(
        1,
        "Volume",
        VOLUME.getPresentationInformation(),
        VOLUME.storageUnit
      )
    );
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

    MeasurementEntity entity = entityMapper.toEntity(measurement);
    assertThat(entity.id.quantity.name).isEqualTo("Volume");
    assertThat(entity.value).isEqualTo(2.0);
    assertThat(entityMapper.toDomainModel(entity)).isEqualTo(measurement);
  }
}
