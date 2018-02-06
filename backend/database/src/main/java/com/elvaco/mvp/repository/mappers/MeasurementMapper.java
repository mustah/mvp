package com.elvaco.mvp.repository.mappers;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.entity.meter.PhysicalMeterEntity;
import org.modelmapper.ModelMapper;

public class MeasurementMapper implements DomainEntityMapper<Measurement, MeasurementEntity> {

  private final ModelMapper modelMapper;
  private final OrganisationMapper organisationMapper;

  public MeasurementMapper(ModelMapper modelMapper, OrganisationMapper organisationMapper) {
    this.modelMapper = modelMapper;
    this.organisationMapper = organisationMapper;
  }

  @Override
  public Measurement toDomainModel(MeasurementEntity entity) {
    return new Measurement(
      entity.id,
      entity.created,
      entity.quantity,
      entity.value.getValue(),
      entity.value.getUnit(),
      newPhysicalMeter(entity.physicalMeter)
    );
  }

  @Override
  public MeasurementEntity toEntity(Measurement domainModel) {
    return modelMapper.map(domainModel, MeasurementEntity.class);
  }

  private PhysicalMeter newPhysicalMeter(PhysicalMeterEntity physicalMeter) {
    return new PhysicalMeter(
      physicalMeter.id,
      organisationMapper.toDomainModel(physicalMeter.organisation),
      physicalMeter.identity,
      physicalMeter.medium
    );
  }
}
