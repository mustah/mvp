package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.MeasurementUnit;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MeasurementEntityMapper {

  public static Measurement toDomainModel(MeasurementEntity entity) {
    return new Measurement(
      entity.id,
      entity.created,
      entity.quantity,
      entity.value.getValue(),
      entity.value.getUnit(),
      PhysicalMeterEntityMapper.toDomainModel(entity.physicalMeter)
    );
  }

  public static MeasurementEntity toEntity(Measurement domainModel) {
    return new MeasurementEntity(
      domainModel.id,
      domainModel.created,
      domainModel.quantity,
      new MeasurementUnit(domainModel.unit, domainModel.value),
      PhysicalMeterEntityMapper.toEntity(domainModel.physicalMeter)
    );
  }
}