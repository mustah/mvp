package com.elvaco.mvp.repository.mappers;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.entity.measurement.MeasurementEntity;
import org.modelmapper.ModelMapper;

public class MeasurementMapper implements DomainEntityMapper<Measurement, MeasurementEntity> {

  private final ModelMapper modelMapper;
  private final PhysicalMeterMapper physicalMeterMapper;

  public MeasurementMapper(ModelMapper modelMapper, PhysicalMeterMapper physicalMeterMapper) {
    this.modelMapper = modelMapper;
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
    return modelMapper.map(domainModel, MeasurementEntity.class);
  }
}
