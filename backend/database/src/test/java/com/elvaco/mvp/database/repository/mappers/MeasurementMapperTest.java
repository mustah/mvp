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

  private final MeasurementMapper measurementMapper = new MeasurementMapper(
    new PhysicalMeterMapper(
      new OrganisationMapper()
    )
  );

  @Test
  public void mapping() {
    PhysicalMeter physicalMeter = new PhysicalMeter(ELVACO, "123-123", "Some medium");
    Measurement measurement = new Measurement(Quantity.VOLUME, 2.0, "m3", physicalMeter);
    MeasurementEntity entity = measurementMapper.toEntity(measurement);
    assertThat(entity.quantity).isEqualTo("Volume");
    assertThat(entity.value).isEqualTo(new MeasurementUnit("2.0 m3"));
    Measurement actual = measurementMapper.toDomainModel(entity);
    assertThat(actual).isEqualTo(measurement);
  }
}
