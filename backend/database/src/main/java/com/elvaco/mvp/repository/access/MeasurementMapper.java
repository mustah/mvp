package com.elvaco.mvp.repository.access;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.entity.user.OrganisationEntity;
import org.modelmapper.ModelMapper;

public class MeasurementMapper implements DomainEntityMapper<Measurement, MeasurementEntity> {
  private final ModelMapper modelMapper;

  public MeasurementMapper(ModelMapper modelMapper) {
    this.modelMapper = modelMapper;
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
    return new PhysicalMeter(physicalMeter.id,
      newOrganisation(physicalMeter.organisation),
      physicalMeter.identity,
      physicalMeter.medium);
  }

  private Organisation newOrganisation(OrganisationEntity organisation) {
    return new Organisation(organisation.id, organisation.name, organisation.code);
  }
}
