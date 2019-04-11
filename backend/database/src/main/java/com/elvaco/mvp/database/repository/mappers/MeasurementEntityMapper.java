package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeasurementUnit;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.unitconverter.UnitConverter;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.MeasurementPk;

import lombok.RequiredArgsConstructor;

import static com.elvaco.mvp.database.repository.mappers.PhysicalMeterEntityMapper.toDomainModelWithoutStatusLogs;

@RequiredArgsConstructor
public class MeasurementEntityMapper {

  private final UnitConverter unitConverter;
  private final QuantityProvider quantityProvider;
  private final QuantityEntityMapper quantityEntityMapper;

  public Measurement toDomainModel(MeasurementEntity entity) {
    return Measurement.builder()
      .readoutTime(entity.id.readoutTime)
      .receivedTime(entity.receivedTime)
      .expectedTime(entity.expectedTime)
      .quantity(entity.id.quantity.name)
      .value(entity.value)
      .unit(entity.id.quantity.storageUnit)
      .physicalMeter(toDomainModelWithoutStatusLogs(entity.id.physicalMeter))
      .build();
  }

  public MeasurementEntity toEntity(Measurement domainModel) {
    Quantity quantity = quantityProvider.getByNameOrThrow(domainModel.quantity);
    return new MeasurementEntity(
      new MeasurementPk(
        domainModel.readoutTime,
        quantityEntityMapper.toEntity(quantity),
        PhysicalMeterEntityMapper.toEntity(domainModel.physicalMeter),
        domainModel.physicalMeter.organisationId
      ),
      domainModel.receivedTime,
      domainModel.expectedTime,
      unitConverter.convert(
        new MeasurementUnit(domainModel.unit, domainModel.value),
        quantity.storageUnit
      ).getValue()
    );
  }
}
