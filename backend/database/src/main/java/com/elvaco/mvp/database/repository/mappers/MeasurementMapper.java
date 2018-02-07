package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.MeasurementUnit;

public class MeasurementMapper implements DomainEntityMapper<Measurement, MeasurementEntity> {

  private final PhysicalMeterMapper physicalMeterMapper;

  public MeasurementMapper(PhysicalMeterMapper physicalMeterMapper) {
    this.physicalMeterMapper = physicalMeterMapper;
  }

  @Override
  public Measurement toDomainModel(MeasurementEntity entity) {
    return new Measurement(
      entity.id,
      entity.created,
      entity.quantity,
      entity.value.getValue(),
      entity.value.getUnit(),
      physicalMeterMapper.toDomainModel(entity.physicalMeter)
    );
  }

  @Override
  public MeasurementEntity toEntity(Measurement domainModel) {
    return new MeasurementEntity(
      domainModel.id,
      domainModel.created,
      domainModel.quantity,
      new MeasurementUnit(domainModel.unit, domainModel.value),
      physicalMeterMapper.toEntity(domainModel.physicalMeter)
    );
  }
}
