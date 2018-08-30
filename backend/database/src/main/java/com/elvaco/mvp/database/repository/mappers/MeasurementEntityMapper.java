package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.access.QuantityAccess;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.MeasurementPk;
import com.elvaco.mvp.database.entity.measurement.MeasurementUnit;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MeasurementEntityMapper {

  public static Measurement toDomainModel(MeasurementEntity entity) {
    return new Measurement(
      entity.id.created,
      entity.id.quantity.name,
      entity.value.getValue(),
      entity.value.getUnit(),
      PhysicalMeterEntityMapper.toDomainModelWithoutStatusLogs(entity.id.physicalMeter)
    );
  }

  public static MeasurementEntity toEntity(Measurement domainModel) {
    return new MeasurementEntity(
      new MeasurementPk(
        domainModel.created,
        QuantityEntityMapper.toEntity(QuantityAccess.singleton().getByName(domainModel.quantity)),
        PhysicalMeterEntityMapper.toEntity(domainModel.physicalMeter)
      ),
      new MeasurementUnit(domainModel.unit, domainModel.value)
    );

  }
}
